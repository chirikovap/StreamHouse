package src.deserialization;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.typeutils.TypeExtractor;
import org.apache.flink.connector.kafka.source.reader.deserializer.KafkaRecordDeserializationSchema;
import org.apache.flink.util.Collector;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import src.dto.kafkaMessage.KafkaMessage;

import java.nio.charset.StandardCharsets;

public class MyKafkaDeserializationSchema implements KafkaRecordDeserializationSchema<KafkaMessage> {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void deserialize(ConsumerRecord<byte[], byte[]> record, Collector<KafkaMessage> out) {
        KafkaMessage message = new KafkaMessage();
        message.setTopicName(record.topic());
        if (record.value() != null) {
            message.setMessage(new String(record.value(), StandardCharsets.UTF_8));
        } else {
            message.setMessage("");
        }
        out.collect(message);
    }

    public boolean isEndOfStream(KafkaMessage nextElement) {
        return false;
    }

    @Override
    public TypeInformation<KafkaMessage> getProducedType() {
        return TypeExtractor.getForClass(KafkaMessage.class);
    }
}
