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
import src.dto.dwh.CustomerDTO;
import src.dto.kafkaMessage.KafkaMessage;
import src.dto.sources.source1.CraftMarketWide;
import src.dto.sources.source2.CraftMarketOrdersCustomers;
import src.dto.sources.source3.CraftMarketCustomers;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

public class UpsertCustomersJob {

    private static final Logger logger = LoggerFactory.getLogger(UpsertCustomersJob.class);

    public static void main(String[] args) throws Exception {
        logger.info("Starting UpsertCustomersJob");
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.enableCheckpointing(60000);

        KafkaSource<KafkaMessage> kafkaSource = KafkaSource.<KafkaMessage>builder()
                .setBootstrapServers("kafka:9092")
                .setTopics("sources_table.source1.craft_market_wide", "sources_table.source2.craft_market_orders_customers", "sources_table.source3.craft_market_customers")
                .setGroupId("upsert-customers-group")
                .setStartingOffsets(OffsetsInitializer.earliest())
                .setDeserializer(new MyKafkaDeserializationSchema())
                .build();

        DataStream<KafkaMessage> kafkaStream = env.fromSource(
                kafkaSource,
                WatermarkStrategy.noWatermarks(),
                "Kafka Customers Source"
        );

        ObjectMapper objectMapper = new ObjectMapper();

        DataStream<CustomerDTO> streamWide = kafkaStream
                .filter(message -> "sources_table.source1.craft_market_wide".equals(message.getTopicName()))
                .map(msg -> {
                    logger.info("Processing craft_market_wide message: {}", msg.getMessage());
                    CraftMarketWide wide = objectMapper.readValue(msg.getMessage(), CraftMarketWide.class);
                    return convertWideToCustomerDTO(wide);
                })
                .filter(dto -> filterInvalidCustomer(dto, "craft_market_wide"));

        DataStream<CustomerDTO> streamOrdersCustomers = kafkaStream
                .filter(message -> "sources_table.source2.craft_market_orders_customers".equals(message.getTopicName()))
                .map(msg -> {
                    logger.info("Processing craft_market_orders_customers message: {}", msg.getMessage());
                    CraftMarketOrdersCustomers oc = objectMapper.readValue(msg.getMessage(), CraftMarketOrdersCustomers.class);
                    return convertOrdersCustomersToCustomerDTO(oc);
                })
                .filter(dto -> filterInvalidCustomer(dto, "craft_market_orders_customers"));

        DataStream<CustomerDTO> streamCustomers = kafkaStream
                .filter(message -> "sources_table.source3.craft_market_customers".equals(message.getTopicName()))
                .map(msg -> {
                    logger.info("Processing craft_market_customers message: {}", msg.getMessage());
                    CraftMarketCustomers c = objectMapper.readValue(msg.getMessage(), CraftMarketCustomers.class);
                    return convertCraftMarketCustomersToCustomerDTO(c);
                })
                .filter(dto -> filterInvalidCustomer(dto, "craft_market_customers"));

        DataStream<CustomerDTO> customersUnion = streamWide.union(streamOrdersCustomers, streamCustomers);

        KeyedStream<CustomerDTO, Long> keyedCustomers = customersUnion
                .filter(dto -> {
                    if (dto.getCustomerId() == null) {
                        logger.warn("Skipping record with null customerId: {}", dto);
                        return false;
                    }
                    return true;
                })
                .keyBy(CustomerDTO::getCustomerId);

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
        TableIdentifier tableId = TableIdentifier.of("dwh", "d_customers");
        TableLoader tableLoader = TableLoader.fromCatalog(catalogLoader, tableId);

        DataStream<RowData> rowDataStream = keyedCustomers.map(new CustomerDtoToRowDataMapper());
        logger.info("Writing data to Iceberg table");

        FlinkSink.forRowData(rowDataStream)
                .tableLoader(tableLoader)
                .append();

        logger.info("Starting Flink job execution...");
        env.execute("Upsert Customers to Iceberg");
        logger.info("Flink job execution completed.");
    }

    private static CustomerDTO convertWideToCustomerDTO(CraftMarketWide wide) {
        if (wide.getCustomerId() == null) {
            logger.warn("Received craft_market_wide with null customerId: {}", wide);
            return null;
        }
        return new CustomerDTO(wide.getCustomerId(), wide.getCustomerName(), wide.getCustomerAddress(),
                parseDate(wide.getCustomerBirthday()), wide.getCustomerEmail(), java.time.LocalDateTime.now());
    }

    private static CustomerDTO convertOrdersCustomersToCustomerDTO(CraftMarketOrdersCustomers oc) {
        if (oc.getCustomerId() == null) {
            logger.warn("Received craft_market_orders_customers with null customerId: {}", oc);
            return null;
        }
        return new CustomerDTO(oc.getCustomerId(), oc.getCustomerName(), oc.getCustomerAddress(),
                parseDate(oc.getCustomerBirthday()), oc.getCustomerEmail(), java.time.LocalDateTime.now());
    }

    private static CustomerDTO convertCraftMarketCustomersToCustomerDTO(CraftMarketCustomers c) {
        if (c.getCustomerId() == null) {
            logger.warn("Received craft_market_customers with null customerId: {}", c);
            return null;
        }
        return new CustomerDTO(c.getCustomerId(), c.getCustomerName(), c.getCustomerAddress(),
                parseDate(c.getCustomerBirthday()), c.getCustomerEmail(), java.time.LocalDateTime.now());
    }

    private static boolean filterInvalidCustomer(CustomerDTO dto, String source) {
        if (dto == null || dto.getCustomerId() == null) {
            logger.warn("Skipping invalid customer record from {}: {}", source, dto);
            return false;
        }
        return true;
    }

    public static class CustomerDtoToRowDataMapper implements MapFunction<CustomerDTO, RowData> {
        @Override
        public RowData map(CustomerDTO value) {
            GenericRowData row = new GenericRowData(6);
            row.setField(0, value.getCustomerId());
            row.setField(1, StringData.fromString(value.getCustomerName()));
            row.setField(2, StringData.fromString(value.getCustomerAddress()));
            row.setField(3, value.getCustomerBirthday() == null ? null : (int) value.getCustomerBirthday().toEpochDay());
            row.setField(4, StringData.fromString(value.getCustomerEmail()));
            row.setField(5, TimestampData.fromLocalDateTime(value.getLoadDttm()));

            logger.info("Mapped RowData: customerId={}, name={}, email={}",
                    value.getCustomerId(), value.getCustomerName(), value.getCustomerEmail());

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
