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
public class CraftMarketCustomers implements Serializable {
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