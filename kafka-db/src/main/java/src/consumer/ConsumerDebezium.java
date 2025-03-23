package src.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.hadoop.conf.Configuration;
import org.apache.iceberg.Table;
import org.apache.iceberg.catalog.Catalog;
import org.apache.iceberg.catalog.TableIdentifier;
import org.apache.iceberg.hadoop.HadoopCatalog;
import org.apache.iceberg.flink.sink.FlinkSink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.stereotype.Component;
import src.deserialization.MyKafkaDeserializationSchema;
import src.dto.kafkaMessage.KafkaMessage;
import src.dto.sources.source1.CraftMarketWide;
import src.dto.sources.source2.CraftMarketMastersProducts;
import src.dto.sources.source2.CraftMarketOrdersCustomers;
import src.dto.sources.source3.CraftMarketCraftsmans;
import src.dto.sources.source3.CraftMarketCustomers;
import src.dto.sources.source3.CraftMarketOrders;
import src.utils.GenericObjectToRowDataMapper;

@EnableKafka
@Component
public class ConsumerDebezium {
    private static final Logger loggerConsumer = LoggerFactory.getLogger(ConsumerDebezium.class);

    public static void main(String[] args) throws Exception {
        try {
            // Создаём окружение Flink
            StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

            // Настраиваем Kafka-источник
            KafkaSource<KafkaMessage> kafkaSource = KafkaSource.<KafkaMessage>builder()
                    .setBootstrapServers("kafka:9092")
                    .setTopics(
                            "sources_table.source1.craft_market_wide",
                            "sources_table.source2.craft_market_masters_products",
                            "sources_table.source2.craft_market_orders_customers",
                            "sources_table.source3.craft_market_craftsmans",
                            "sources_table.source3.craft_market_customers",
                            "sources_table.source3.craft_market_orders"
                    )
                    .setGroupId("flink-group")
                    .setStartingOffsets(OffsetsInitializer.earliest())
                    .setDeserializer(new MyKafkaDeserializationSchema())
                    .build();

            // Kafka -> Flink Stream
            DataStream<KafkaMessage> kafkaStream = env.fromSource(
                    kafkaSource,
                    WatermarkStrategy.noWatermarks(),
                    "Kafka Source"
            );

            // JSON ObjectMapper для парсинга сообщений
            ObjectMapper objectMapper = new ObjectMapper();

            // 🛠️ Настроим Iceberg через HadoopCatalog
            Configuration hadoopConf = new Configuration();
            hadoopConf.set("fs.s3a.endpoint", "http://minio:9000");
            hadoopConf.set("fs.s3a.access.key", "minioadmin");
            hadoopConf.set("fs.s3a.secret.key", "minioadmin");
            hadoopConf.set("hive.metastore.uris", "thrift://hive-metastore:9083");

            Catalog catalog = new HadoopCatalog(hadoopConf, "s3://datalake/iceberg/");

            // Обрабатываем каждую таблицу и пишем в Iceberg

            // CraftMarketWide → Iceberg (dwh.d_craft_market_wide)
            DataStream<CraftMarketWide> craftMarketWideStream = kafkaStream
                    .filter(message -> "sources_table.source1.craft_market_wide".equals(message.getTopicName()))
                    .map(message -> {
                        loggerConsumer.info("Received CraftMarketWide message: {}", message.getMessage());
                        return objectMapper.readValue(message.getMessage(), CraftMarketWide.class);
                    });
            writeToIceberg(catalog, craftMarketWideStream, "dwh", "d_craft_market_wide");

            // CraftMarketMastersProducts → Iceberg (dwh.d_craft_market_masters_products)
            DataStream<CraftMarketMastersProducts> craftMarketMastersProductsStream = kafkaStream
                    .filter(message -> "sources_table.source2.craft_market_masters_products".equals(message.getTopicName()))
                    .map(message -> {
                        loggerConsumer.info("Received CraftMarketMastersProducts message: {}", message.getMessage());
                        return objectMapper.readValue(message.getMessage(), CraftMarketMastersProducts.class);
                    });
            writeToIceberg(catalog, craftMarketMastersProductsStream, "dwh", "d_craft_market_masters_products");

            // CraftMarketOrdersCustomers → Iceberg (dwh.d_craft_market_orders_customers)
            DataStream<CraftMarketOrdersCustomers> craftMarketOrdersCustomersStream = kafkaStream
                    .filter(message -> "sources_table.source2.craft_market_orders_customers".equals(message.getTopicName()))
                    .map(message -> {
                        loggerConsumer.info("Received CraftMarketOrdersCustomers message: {}", message.getMessage());
                        return objectMapper.readValue(message.getMessage(), CraftMarketOrdersCustomers.class);
                    });
            writeToIceberg(catalog, craftMarketOrdersCustomersStream, "dwh", "d_craft_market_orders_customers");

            // CraftMarketCraftsmans → Iceberg (dwh.d_craft_market_craftsmans)
            DataStream<CraftMarketCraftsmans> craftMarketCraftsmansStream = kafkaStream
                    .filter(message -> "sources_table.source3.craft_market_craftsmans".equals(message.getTopicName()))
                    .map(message -> {
                        loggerConsumer.info("Received CraftMarketCraftsmans message: {}", message.getMessage());
                        return objectMapper.readValue(message.getMessage(), CraftMarketCraftsmans.class);
                    });
            writeToIceberg(catalog, craftMarketCraftsmansStream, "dwh", "d_craft_market_craftsmans");

            // CraftMarketCustomers → Iceberg (dwh.d_customers)
            DataStream<CraftMarketCustomers> craftMarketCustomersStream = kafkaStream
                    .filter(message -> "sources_table.source3.craft_market_customers".equals(message.getTopicName()))
                    .map(message -> {
                        loggerConsumer.info("Received CraftMarketCustomers message: {}", message.getMessage());
                        return objectMapper.readValue(message.getMessage(), CraftMarketCustomers.class);
                    });
            writeToIceberg(catalog, craftMarketCustomersStream, "dwh", "d_customers");

            // CraftMarketOrders → Iceberg (dwh.f_orders)
            DataStream<CraftMarketOrders> craftMarketOrdersStream = kafkaStream
                    .filter(message -> "sources_table.source3.craft_market_orders".equals(message.getTopicName()))
                    .map(message -> {
                        loggerConsumer.info("Received CraftMarketOrders message: {}", message.getMessage());
                        return objectMapper.readValue(message.getMessage(), CraftMarketOrders.class);
                    });
            writeToIceberg(catalog, craftMarketOrdersStream, "dwh", "f_orders");

            env.execute("Consumer Debezium Flink → Iceberg");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Метод записи потока данных в Iceberg
    private static <T> void writeToIceberg(Catalog catalog, DataStream<T> dataStream, String schema, String tableName) {
        TableIdentifier tableId = TableIdentifier.of(schema, tableName);
        Table table = catalog.loadTable(tableId);

        FlinkSink.forRowData(dataStream.map(new GenericObjectToRowDataMapper<>()))
                .table(table)
                .append();
    }
}
