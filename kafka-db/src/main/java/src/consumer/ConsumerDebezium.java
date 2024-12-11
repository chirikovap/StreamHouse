package src.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.stereotype.Component;
import src.dto.kafkaMessage.KafkaMessage;
import src.dto.sources.source1.CraftMarketWide;
import src.dto.sources.source2.CraftMarketMastersProducts;
import src.dto.sources.source2.CraftMarketOrdersCustomers;
import src.dto.sources.source3.CraftMarketCraftsmans;
import src.dto.sources.source3.CraftMarketCustomers;
import src.dto.sources.source3.CraftMarketOrders;
import src.handler.KafkaMessageDeserializer;


@EnableKafka
@Component
public class ConsumerDebezium {
    private static Logger loggerConsumer = LoggerFactory.getLogger(ConsumerDebezium.class);
    @PostConstruct
    public void startFlinkJob() {
        try {
            final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

            KafkaSource<KafkaMessage> kafkaSource = KafkaSource.<KafkaMessage>builder()
                    .setBootstrapServers("kafka:9092")
                    .setTopics(
                            "dbserver1.source1.craft_market_wide",
                            "dbserver1.source2.craft_market_masters_products",
                            "dbserver1.source2.craft_market_orders_customers",
                            "dbserver1.source3.craft_market_craftsmans",
                            "dbserver1.source3.craft_market_customers",
                            "dbserver1.source3.craft_market_orders"
                    )
                    .setGroupId("flink-group")
                    .setStartingOffsets(OffsetsInitializer.earliest())
                    .setValueOnlyDeserializer(new KafkaMessageDeserializer())
                    .build();

            DataStream<KafkaMessage> kafkaStream = env.fromSource(
                    kafkaSource,
                    WatermarkStrategy.noWatermarks(),
                    "Kafka Source"
            );

            ObjectMapper objectMapper = new ObjectMapper();

            DataStream<CraftMarketWide> craftMarketWideStream = kafkaStream
                    .filter(message -> message.getTopicName().equals("dbserver1.source1.craft_market_wide"))
                    .map(message -> objectMapper.readValue(message.getMessage(), CraftMarketWide.class));

            DataStream<CraftMarketMastersProducts> craftMarketMastersProductsStream = kafkaStream
                    .filter(message -> message.getTopicName().equals("dbserver1.source2.craft_market_masters_products"))
                    .map(message -> objectMapper.readValue(message.getMessage(), CraftMarketMastersProducts.class));

            DataStream<CraftMarketOrdersCustomers> craftMarketOrdersCustomersStream = kafkaStream
                    .filter(message -> message.getTopicName().equals("dbserver1.source2.craft_market_orders_customers"))
                    .map(message -> objectMapper.readValue(message.getMessage(), CraftMarketOrdersCustomers.class));

            DataStream<CraftMarketCraftsmans> craftMarketCraftsmansStream = kafkaStream
                    .filter(message -> message.getTopicName().equals("dbserver1.source3.craft_market_craftsmans"))
                    .map(message -> objectMapper.readValue(message.getMessage(), CraftMarketCraftsmans.class));

            DataStream<CraftMarketCustomers> craftMarketCustomersStream = kafkaStream
                    .filter(message -> message.getTopicName().equals("dbserver1.source3.craft_market_customers"))
                    .map(message -> objectMapper.readValue(message.getMessage(), CraftMarketCustomers.class));

            DataStream<CraftMarketOrders> craftMarketOrdersStream = kafkaStream
                    .filter(message -> message.getTopicName().equals("dbserver1.source3.craft_market_orders"))
                    .map(message -> objectMapper.readValue(message.getMessage(), CraftMarketOrders.class));

            env.execute("Flink processing");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
