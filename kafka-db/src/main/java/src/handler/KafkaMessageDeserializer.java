package src.handler;

import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import src.dto.kafkaMessage.KafkaMessage;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class KafkaMessageDeserializer implements DeserializationSchema<KafkaMessage> {

    @Override
    public KafkaMessage deserialize(byte[] message) throws IOException {
        // Преобразуем байты в строку
        String value = new String(message, StandardCharsets.UTF_8);

        // Создаём объект KafkaMessage и заполняем значение
        KafkaMessage kafkaMessage = new KafkaMessage();
        kafkaMessage.setMessage(value);

        // Возвращаем объект KafkaMessage
        return kafkaMessage;
    }

    @Override
    public boolean isEndOfStream(KafkaMessage nextElement) {
        return false; // Поток данных из Kafka бесконечный
    }

    @Override
    public TypeInformation<KafkaMessage> getProducedType() {
        return TypeInformation.of(KafkaMessage.class);
    }
}
