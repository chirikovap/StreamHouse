package src.dto.kafkaMessage;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KafkaMessage {
    private String topicName;
    private String message;
}
