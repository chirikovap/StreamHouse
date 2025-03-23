package src.dto.sources.source3;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDate;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class CraftMarketOrders implements Serializable {
    @JsonProperty("order_id")
    private Long orderId; // идентификатор заказа

    @JsonProperty("product_id")
    private Long productId; // идентификатор товара

    @JsonProperty("craftsman_id")
    private Long craftsmanId; // идентификатор мастера

    @JsonProperty("customer_id")
    private Long customerId; // идентификатор заказчика

    @JsonProperty("order_created_date")
    private String orderCreatedDate; // дата создания заказа

    @JsonProperty("order_completion_date")
    private String orderCompletionDate; // дата выполнения заказа

    @JsonProperty("order_status")
    private String orderStatus; // статус выполнения заказа (created, in progress, delivery, done)

    @JsonProperty("product_name")
    private String productName; // название товара ручной работы

    @JsonProperty("product_description")
    private String productDescription; // описание товара ручной работы

    @JsonProperty("product_type")
    private String productType; // тип товара ручной работы

    @JsonProperty("product_price")
    private Long productPrice; // цена товара ручной работы
}