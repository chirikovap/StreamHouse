package src.jobs;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.*;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.data.*;
import org.apache.hadoop.conf.Configuration;
import org.apache.iceberg.CatalogProperties;
import org.apache.iceberg.catalog.*;
import org.apache.iceberg.flink.CatalogLoader;
import org.apache.iceberg.flink.TableLoader;
import org.apache.iceberg.flink.sink.FlinkSink;
import org.apache.iceberg.hadoop.HadoopCatalog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import src.deserialization.MyKafkaDeserializationSchema;
import src.dto.dwh.OrderFactDTO;
import src.dto.kafkaMessage.KafkaMessage;
import src.dto.sources.source1.CraftMarketWide;
import src.dto.sources.source2.CraftMarketOrdersCustomers;
import src.dto.sources.source3.CraftMarketOrders;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

public class UpsertOrdersFactJob {

    private static final Logger logger = LoggerFactory.getLogger(UpsertOrdersFactJob.class);

    public static void main(String[] args) throws Exception {
        logger.info("Starting UpsertOrdersFactJob");
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.enableCheckpointing(60000);

        KafkaSource<KafkaMessage> kafkaSource = KafkaSource.<KafkaMessage>builder()
                .setBootstrapServers("kafka:9092")
                .setTopics(
                        "sources_table.source1.craft_market_wide",
                        "sources_table.source2.craft_market_orders_customers",
                        "sources_table.source3.craft_market_orders"
                )
                .setGroupId("upsert-orders-group")
                .setStartingOffsets(OffsetsInitializer.earliest())
                .setDeserializer(new MyKafkaDeserializationSchema())
                .build();

        DataStream<KafkaMessage> kafkaStream = env.fromSource(
                kafkaSource,
                WatermarkStrategy.noWatermarks(),
                "Kafka Orders Source"
        );

        ObjectMapper objectMapper = new ObjectMapper();

        DataStream<OrderFactDTO> streamWide = kafkaStream
                .filter(message -> "sources_table.source1.craft_market_wide".equals(message.getTopicName()))
                .map(msg -> {
                    logger.info("Processing craft_market_wide message: {}", msg.getMessage());
                    CraftMarketWide wide = objectMapper.readValue(msg.getMessage(), CraftMarketWide.class);
                    return convertWideToOrderFactDTO(wide);
                })
                .filter(dto -> filterInvalidOrder(dto, "craft_market_wide"));

        DataStream<OrderFactDTO> streamOrdersCustomers = kafkaStream
                .filter(message -> "sources_table.source2.craft_market_orders_customers".equals(message.getTopicName()))
                .map(msg -> {
                    logger.info("Processing craft_market_orders_customers message: {}", msg.getMessage());
                    CraftMarketOrdersCustomers oc = objectMapper.readValue(msg.getMessage(), CraftMarketOrdersCustomers.class);
                    return convertOrdersCustomersToOrderFactDTO(oc);
                })
                .filter(dto -> filterInvalidOrder(dto, "craft_market_orders_customers"));

        DataStream<OrderFactDTO> streamOrders = kafkaStream
                .filter(message -> "sources_table.source3.craft_market_orders".equals(message.getTopicName()))
                .map(msg -> {
                    logger.info("Processing craft_market_orders message: {}", msg.getMessage());
                    CraftMarketOrders o = objectMapper.readValue(msg.getMessage(), CraftMarketOrders.class);
                    return convertCraftMarketOrdersToOrderFactDTO(o);
                })
                .filter(dto -> filterInvalidOrder(dto, "craft_market_orders"));

        DataStream<OrderFactDTO> ordersUnion = streamWide.union(streamOrdersCustomers, streamOrders);

        KeyedStream<OrderFactDTO, Long> keyedOrders = ordersUnion
                .filter(dto -> {
                    if (dto.getOrderId() == null) {
                        logger.warn("Skipping record with null orderId: {}", dto);
                        return false;
                    }
                    return true;
                })
                .keyBy(OrderFactDTO::getOrderId);

        Configuration hadoopConf = new Configuration();
        hadoopConf.set("fs.s3a.access.key", "minioadmin");
        hadoopConf.set("fs.s3a.secret.key", "minioadmin");
        hadoopConf.set("fs.s3a.endpoint", "http://minio:9000");
        hadoopConf.set("fs.s3a.path.style.access", "true");
        hadoopConf.set("fs.s3a.region", "us-east-1");

        String warehousePath = "s3a://craftmarket/warehouse";
        HadoopCatalog catalog = new HadoopCatalog(hadoopConf, warehousePath);

        Map<String, String> catalogProperties = new HashMap<>();
        catalogProperties.put(CatalogProperties.WAREHOUSE_LOCATION, warehousePath);

        CatalogLoader catalogLoader = CatalogLoader.hadoop("hadoop", hadoopConf, catalogProperties);
        TableIdentifier tableId = TableIdentifier.of("dwh", "f_orders");
        TableLoader tableLoader = TableLoader.fromCatalog(catalogLoader, tableId);

        DataStream<RowData> rowDataStream = keyedOrders.map(new OrderFactDtoToRowDataMapper());
        logger.info("Writing data to Iceberg table");

        FlinkSink.forRowData(rowDataStream)
                .tableLoader(tableLoader)
                .append();

        logger.info("Starting Flink job execution...");
        env.execute("Upsert Orders to Iceberg");
        logger.info("Flink job execution completed.");
    }

    private static OrderFactDTO convertWideToOrderFactDTO(CraftMarketWide wide) {
        if (wide.getOrderId() == null) {
            logger.warn("Received craft_market_wide with null orderId: {}", wide);
            return null;
        }
        OrderFactDTO dto = new OrderFactDTO();
        dto.setOrderId(wide.getOrderId());
        dto.setProductId(wide.getProductId());
        dto.setCraftsmanId(wide.getCraftsmanId());
        dto.setCustomerId(wide.getCustomerId());
        dto.setOrderCreatedDate(parseDate(wide.getOrderCreatedDate()));
        dto.setOrderCompletionDate(parseDate(wide.getOrderCompletionDate()));
        dto.setOrderStatus(wide.getOrderStatus());
        dto.setLoadDttm(java.time.LocalDateTime.now());
        return dto;
    }

    private static OrderFactDTO convertOrdersCustomersToOrderFactDTO(CraftMarketOrdersCustomers oc) {
        if (oc.getOrderId() == null) {
            logger.warn("Received craft_market_orders_customers with null orderId: {}", oc);
            return null;
        }
        OrderFactDTO dto = new OrderFactDTO();
        dto.setOrderId(oc.getOrderId());
        dto.setProductId(oc.getProductId());
        dto.setCraftsmanId(oc.getCraftsmanId());
        dto.setCustomerId(oc.getCustomerId());
        dto.setOrderCreatedDate(parseDate(oc.getOrderCreatedDate()));
        dto.setOrderCompletionDate(parseDate(oc.getOrderCompletionDate()));
        dto.setOrderStatus(oc.getOrderStatus());
        dto.setLoadDttm(java.time.LocalDateTime.now());
        return dto;
    }

    private static OrderFactDTO convertCraftMarketOrdersToOrderFactDTO(CraftMarketOrders o) {
        if (o.getOrderId() == null) {
            logger.warn("Received craft_market_orders with null orderId: {}", o);
            return null;
        }
        OrderFactDTO dto = new OrderFactDTO();
        dto.setOrderId(o.getOrderId());
        dto.setProductId(o.getProductId());
        dto.setCraftsmanId(o.getCraftsmanId());
        dto.setCustomerId(o.getCustomerId());
        dto.setOrderCreatedDate(parseDate(o.getOrderCreatedDate()));
        dto.setOrderCompletionDate(parseDate(o.getOrderCompletionDate()));
        dto.setOrderStatus(o.getOrderStatus());
        dto.setLoadDttm(java.time.LocalDateTime.now());
        return dto;
    }

    private static boolean filterInvalidOrder(OrderFactDTO dto, String source) {
        if (dto == null || dto.getOrderId() == null) {
            logger.warn("Skipping invalid order record from {}: {}", source, dto);
            return false;
        }
        return true;
    }

    public static class OrderFactDtoToRowDataMapper implements MapFunction<OrderFactDTO, RowData> {
        @Override
        public RowData map(OrderFactDTO value) {
            GenericRowData row = new GenericRowData(8);
            row.setField(0, value.getOrderId());
            row.setField(1, value.getProductId());
            row.setField(2, value.getCraftsmanId());
            row.setField(3, value.getCustomerId());
            row.setField(4, value.getOrderCreatedDate() == null ? null : (int) value.getOrderCreatedDate().toEpochDay());
            row.setField(5, value.getOrderCompletionDate() == null ? null : (int) value.getOrderCompletionDate().toEpochDay());
            row.setField(6, StringData.fromString(value.getOrderStatus()));
            row.setField(7, TimestampData.fromLocalDateTime(value.getLoadDttm()));

            logger.info("Mapped RowData: orderId={}, productId={}, status={}",
                    value.getOrderId(), value.getProductId(), value.getOrderStatus());

            return row;
        }
    }

    private static LocalDate parseDate(String raw) {
        if (raw == null || raw.isEmpty()) return null;
        if (raw.matches("\\d+")) return LocalDate.ofEpochDay(Long.parseLong(raw));
        try {
            return LocalDate.parse(raw);
        } catch (DateTimeParseException e) {
            logger.warn("Failed to parse date string: {}", raw, e);
            return null;
        }
    }
}