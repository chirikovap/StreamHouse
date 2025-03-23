package src.jobs;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.state.MapState;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.*;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;
import org.apache.hadoop.conf.Configuration;
import org.apache.iceberg.CatalogProperties;
import org.apache.iceberg.catalog.*;
import org.apache.iceberg.flink.CatalogLoader;
import org.apache.iceberg.flink.TableLoader;
import org.apache.iceberg.hadoop.HadoopCatalog;
import org.apache.iceberg.flink.sink.FlinkSink;
import org.apache.flink.table.data.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import src.deserialization.MyKafkaDeserializationSchema;
import src.dto.kafkaMessage.KafkaMessage;
import src.dto.dwh.CraftsmanDTO;
import src.dto.sources.source1.CraftMarketWide;
import src.dto.sources.source2.CraftMarketMastersProducts;
import src.dto.sources.source3.CraftMarketCraftsmans;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

public class UpsertCraftsmanJob {

    private static final Logger logger = LoggerFactory.getLogger(UpsertCraftsmanJob.class);

    public static void main(String[] args) throws Exception {
        logger.info("Starting UpsertCraftsmanJob");
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        env.enableCheckpointing(60000);

        KafkaSource<KafkaMessage> kafkaSource = KafkaSource.<KafkaMessage>builder()
                .setBootstrapServers("kafka:9092")
                .setTopics("sources_table.source1.craft_market_wide", "sources_table.source3.craft_market_craftsmans", "sources_table.source2.craft_market_masters_products")
                .setGroupId("upsert-craftsmans-group")
                .setStartingOffsets(OffsetsInitializer.earliest())
                .setDeserializer(new MyKafkaDeserializationSchema())
                .build();

        DataStream<KafkaMessage> kafkaStream = env.fromSource(
                kafkaSource,
                WatermarkStrategy.noWatermarks(),
                "Kafka Craftsman Source"
        );

        ObjectMapper objectMapper = new ObjectMapper();

        DataStream<CraftsmanDTO> streamWide = kafkaStream
                .filter(message -> "sources_table.source1.craft_market_wide".equals(message.getTopicName()))
                .map(msg -> {
                    CraftMarketWide wide = objectMapper.readValue(msg.getMessage(), CraftMarketWide.class);
                    return convertWideToCraftsmanDTO(wide);
                });

        DataStream<CraftsmanDTO> streamCraftsmans = kafkaStream
                .filter(message -> "sources_table.source3.craft_market_craftsmans".equals(message.getTopicName()))
                .map(msg -> {
                    CraftMarketCraftsmans c = objectMapper.readValue(msg.getMessage(), CraftMarketCraftsmans.class);
                    return convertCraftMarketCraftsmansToCraftsmanDTO(c);
                });

        DataStream<CraftsmanDTO> streamMastersProducts = kafkaStream
                .filter(message -> "sources_table.source2.craft_market_masters_products".equals(message.getTopicName()))
                .map(msg -> {
                    CraftMarketMastersProducts p = objectMapper.readValue(msg.getMessage(), CraftMarketMastersProducts.class);
                    return convertMastersProductsToCraftsmanDTO(p);
                });

// Объединяем потоки — они теперь действительно работают параллельно
        DataStream<CraftsmanDTO> craftsmansUnion = streamWide.union(streamCraftsmans, streamMastersProducts);

// Дедупликация по содержимому (не по ID)
        DataStream<CraftsmanDTO> deduplicatedStream = craftsmansUnion
                .keyBy(CraftsmanDTO::getDeduplicationKey)
                .process(new DeduplicationFunction());


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
        TableIdentifier tableId = TableIdentifier.of("dwh", "d_craftsmans");
        TableLoader tableLoader = TableLoader.fromCatalog(catalogLoader, tableId);

        DataStream<RowData> rowDataStream = deduplicatedStream.map(new CraftsmanDtoToRowDataMapper());
        logger.info("Writing data to Iceberg table");

        FlinkSink.forRowData(rowDataStream)
                .tableLoader(tableLoader)
                .append();

        env.executeAsync("Upsert Craftsman to Iceberg");
    }

    public static class DeduplicationFunction extends KeyedProcessFunction<String, CraftsmanDTO, CraftsmanDTO> {
        private transient MapState<String, Boolean> seenMessages;

        @Override
        public void open(org.apache.flink.configuration.Configuration parameters) {
            MapStateDescriptor<String, Boolean> descriptor = new MapStateDescriptor<>("seenMessages", String.class, Boolean.class);
            seenMessages = getRuntimeContext().getMapState(descriptor);
        }

        @Override
        public void processElement(CraftsmanDTO value, Context ctx, Collector<CraftsmanDTO> out) throws Exception {
            String deduplicationKey = value.getDeduplicationKey();
            if (!seenMessages.contains(deduplicationKey)) {
                seenMessages.put(deduplicationKey, true);
                out.collect(value);
            }
        }
    }

    private static CraftsmanDTO convertWideToCraftsmanDTO(CraftMarketWide wide) {
        logger.info("Converting CraftMarketWide to CraftsmanDTO: {}", wide);
        CraftsmanDTO dto = new CraftsmanDTO();
        dto.setCraftsmanId(wide.getCraftsmanId());
        dto.setCraftsmanName(wide.getCraftsmanName());
        dto.setCraftsmanAddress(wide.getCraftsmanAddress());
        dto.setCraftsmanBirthday(parseDate(wide.getCraftsmanBirthday()));
        dto.setCraftsmanEmail(wide.getCraftsmanEmail());
        dto.setLoadDttm(java.time.LocalDateTime.now());
        return dto;
    }

    private static CraftsmanDTO convertCraftMarketCraftsmansToCraftsmanDTO(CraftMarketCraftsmans c) {
        logger.info("Converting CraftMarketCraftsmans to CraftsmanDTO: {}", c);
        CraftsmanDTO dto = new CraftsmanDTO();
        dto.setCraftsmanId(c.getCraftsmanId());
        dto.setCraftsmanName(c.getCraftsmanName());
        dto.setCraftsmanAddress(c.getCraftsmanAddress());
        dto.setCraftsmanBirthday(parseDate(c.getCraftsmanBirthday()));
        dto.setCraftsmanEmail(c.getCraftsmanEmail());
        dto.setLoadDttm(java.time.LocalDateTime.now());
        return dto;
    }

    private static CraftsmanDTO convertMastersProductsToCraftsmanDTO(CraftMarketMastersProducts p) {
        logger.info("Converting CraftMarketMastersProducts to CraftsmanDTO: {}", p);
        CraftsmanDTO dto = new CraftsmanDTO();
        dto.setCraftsmanId(p.getCraftsmanId());
        dto.setCraftsmanName(p.getCraftsmanName());
        dto.setCraftsmanAddress(p.getCraftsmanAddress());
        dto.setCraftsmanBirthday(parseDate(p.getCraftsmanBirthday()));
        dto.setCraftsmanEmail(p.getCraftsmanEmail());
        dto.setLoadDttm(java.time.LocalDateTime.now());
        return dto;
    }

    public static class CraftsmanDtoToRowDataMapper implements MapFunction<CraftsmanDTO, RowData> {

        @Override
        public RowData map(CraftsmanDTO value) {
            // Порядок полей должен совпадать с порядком столбцов в Iceberg таблице dwh.d_craft_market_craftsmans
            GenericRowData row = new GenericRowData(6);
            row.setField(0, value.getCraftsmanId());
            row.setField(1, StringData.fromString(value.getCraftsmanName()));
            row.setField(2, StringData.fromString(value.getCraftsmanAddress()));
            row.setField(3, value.getCraftsmanBirthday() == null ? null : (int) value.getCraftsmanBirthday().toEpochDay());
            row.setField(4, StringData.fromString(value.getCraftsmanEmail()));
            // Преобразуем LocalDateTime в TimestampData
            row.setField(5, TimestampData.fromLocalDateTime(value.getLoadDttm()));

            logger.info("Mapped RowData: craftsmanId={}, name={}, email={}",
                    value.getCraftsmanId(),
                    value.getCraftsmanName(),
                    value.getCraftsmanEmail()
            );

            return row;
        }
    }

    private static LocalDate parseDate(String raw) {
        if (raw == null || raw.isEmpty()) {
            return null;
        }
        if (raw.matches("\\d+")) {
            return LocalDate.ofEpochDay(Long.parseLong(raw));
        } else {
            try {
                return LocalDate.parse(raw);
            } catch (DateTimeParseException e) {
                logger.warn("Failed to parse date string: {}", raw, e);
                return null;
            }
        }
    }
}