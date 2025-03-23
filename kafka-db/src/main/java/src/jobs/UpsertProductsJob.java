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
import src.dto.dwh.ProductDTO;
import src.dto.kafkaMessage.KafkaMessage;
import src.dto.sources.source1.CraftMarketWide;
import src.dto.sources.source2.CraftMarketMastersProducts;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class UpsertProductsJob {

    private static final Logger logger = LoggerFactory.getLogger(UpsertProductsJob.class);

    public static void main(String[] args) throws Exception {
        logger.info("Starting UpsertProductsJob");
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.enableCheckpointing(60000);

        KafkaSource<KafkaMessage> kafkaSource = KafkaSource.<KafkaMessage>builder()
                .setBootstrapServers("kafka:9092")
                .setTopics(
                        "sources_table.source1.craft_market_wide",
                        "sources_table.source2.craft_market_masters_products"
                )
                .setGroupId("upsert-products-group")
                .setStartingOffsets(OffsetsInitializer.earliest())
                .setDeserializer(new MyKafkaDeserializationSchema())
                .build();

        DataStream<KafkaMessage> kafkaStream = env.fromSource(
                kafkaSource,
                WatermarkStrategy.noWatermarks(),
                "Kafka Products Source"
        );

        ObjectMapper objectMapper = new ObjectMapper();

        DataStream<ProductDTO> streamWide = kafkaStream
                .filter(message -> "sources_table.source1.craft_market_wide".equals(message.getTopicName()))
                .map(msg -> {
                    logger.info("Processing craft_market_wide message: {}", msg.getMessage());
                    CraftMarketWide wide = objectMapper.readValue(msg.getMessage(), CraftMarketWide.class);
                    return convertWideToProductDTO(wide);
                })
                .filter(dto -> filterInvalidProduct(dto, "craft_market_wide"));

        DataStream<ProductDTO> streamMastersProducts = kafkaStream
                .filter(message -> "sources_table.source2.craft_market_masters_products".equals(message.getTopicName()))
                .map(msg -> {
                    logger.info("Processing craft_market_masters_products message: {}", msg.getMessage());
                    CraftMarketMastersProducts mp = objectMapper.readValue(msg.getMessage(), CraftMarketMastersProducts.class);
                    return convertMastersProductsToProductDTO(mp);
                })
                .filter(dto -> filterInvalidProduct(dto, "craft_market_masters_products"));

        DataStream<ProductDTO> productsUnion = streamWide.union(streamMastersProducts);

        KeyedStream<ProductDTO, Long> keyedProducts = productsUnion
                .filter(dto -> {
                    if (dto.getProductId() == null) {
                        logger.warn("Skipping record with null productId: {}", dto);
                        return false;
                    }
                    return true;
                })
                .keyBy(ProductDTO::getProductId);

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
        TableIdentifier tableId = TableIdentifier.of("dwh", "d_products");
        TableLoader tableLoader = TableLoader.fromCatalog(catalogLoader, tableId);

        DataStream<RowData> rowDataStream = keyedProducts.map(new ProductDtoToRowDataMapper());
        logger.info("Writing data to Iceberg table");

        FlinkSink.forRowData(rowDataStream)
                .tableLoader(tableLoader)
                .append();

        logger.info("Starting Flink job execution...");
        env.execute("Upsert Products to Iceberg");
        logger.info("Flink job execution completed.");
    }

    private static ProductDTO convertWideToProductDTO(CraftMarketWide wide) {
        if (wide.getProductId() == null) {
            logger.warn("Received craft_market_wide with null productId: {}", wide);
            return null;
        }
        ProductDTO dto = new ProductDTO();
        dto.setProductId(wide.getProductId());
        dto.setProductName(wide.getProductName());
        dto.setProductDescription(wide.getProductDescription());
        dto.setProductType(wide.getProductType());
        dto.setProductPrice(wide.getProductPrice());
        dto.setLoadDttm(LocalDateTime.now());
        return dto;
    }

    private static ProductDTO convertMastersProductsToProductDTO(CraftMarketMastersProducts mp) {
        if (mp.getProductId() == null) {
            logger.warn("Received craft_market_masters_products with null productId: {}", mp);
            return null;
        }
        ProductDTO dto = new ProductDTO();
        dto.setProductId(mp.getProductId());
        dto.setProductName(mp.getProductName());
        dto.setProductDescription(mp.getProductDescription());
        dto.setProductType(mp.getProductType());
        dto.setProductPrice(mp.getProductPrice());
        dto.setLoadDttm(LocalDateTime.now());
        return dto;
    }

    private static boolean filterInvalidProduct(ProductDTO dto, String source) {
        if (dto == null || dto.getProductId() == null) {
            logger.warn("Skipping invalid product record from {}: {}", source, dto);
            return false;
        }
        return true;
    }

    public static class ProductDtoToRowDataMapper implements MapFunction<ProductDTO, RowData> {
        @Override
        public RowData map(ProductDTO value) {
            GenericRowData row = new GenericRowData(6);
            row.setField(0, value.getProductId());
            row.setField(1, StringData.fromString(value.getProductName()));
            row.setField(2, StringData.fromString(value.getProductDescription()));
            row.setField(3, StringData.fromString(value.getProductType()));
            row.setField(4, value.getProductPrice());
            row.setField(5, TimestampData.fromLocalDateTime(value.getLoadDttm()));

            logger.info("Mapped RowData: productId={}, name={}, type={}",
                    value.getProductId(), value.getProductName(), value.getProductType());

            return row;
        }
    }
}