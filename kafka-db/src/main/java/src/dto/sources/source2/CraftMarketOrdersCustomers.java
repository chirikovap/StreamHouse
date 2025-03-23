package src.dto.sources.source2;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class CraftMarketOrdersCustomers implements Serializable {

    @JsonProperty("order_id")
    private Long orderId; // идентификатор заказа

    @JsonProperty("craftsman_id")
    private Long craftsmanId; // идентификатор мастера

    @JsonProperty("product_id")
    private Long productId; // идентификатор товара

    @JsonProperty("order_created_date")
    private String orderCreatedDate; // дата создания заказа

    @JsonProperty("order_completion_date")
    private String orderCompletionDate; // дата выполнения заказа

    @JsonProperty("order_status")
    private String orderStatus; // статус выполнения заказа

    @JsonProperty("customer_id")
    private Long customerId; // идентификатор заказчика

    @JsonProperty("customer_name")
    private String customerName; // ФИО заказчика

    @JsonProperty("customer_address")
    private String customerAddress; // адрес заказчика

    @JsonProperty("customer_birthday")
    private String customerBirthday; // дата рождения заказчика

    @JsonProperty("customer_email")
    private String customerEmail; // электронная почта заказчика
}