package src.dto.dwh;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;


/**
 * DTO для таблицы dwh.f_orders
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderFactDTO implements Serializable {
    
    @JsonProperty("order_id")
    private Long orderId; // идентификатор заказа
    
    @JsonProperty("product_id")
    private Long productId; // идентификатор товара
    
    @JsonProperty("craftsman_id")
    private Long craftsmanId; // идентификатор мастера
    
    @JsonProperty("customer_id")
    private Long customerId; // идентификатор заказчика
    
    @JsonProperty("order_created_date")
    private LocalDate orderCreatedDate; // дата создания заказа
    
    @JsonProperty("order_completion_date")
    private LocalDate orderCompletionDate; // дата выполнения заказа
    
    @JsonProperty("order_status")
    private String orderStatus; // статус выполнения заказа (created, in progress, delivery, done)
    
    @JsonProperty("load_dttm")
    private LocalDateTime loadDttm; // дата и время загрузки
}
