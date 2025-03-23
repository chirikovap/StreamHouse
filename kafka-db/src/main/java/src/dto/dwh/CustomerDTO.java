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
 * DTO для таблицы dwh.d_customers
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomerDTO implements Serializable {
    
    @JsonProperty("customer_id")
    private Long customerId; // идентификатор заказчика
    
    @JsonProperty("customer_name")
    private String customerName; // ФИО заказчика
    
    @JsonProperty("customer_address")
    private String customerAddress; // адрес заказчика
    
    @JsonProperty("customer_birthday")
    private LocalDate customerBirthday; // дата рождения заказчика
    
    @JsonProperty("customer_email")
    private String customerEmail; // электронная почта заказчика
    
    @JsonProperty("load_dttm")
    private LocalDateTime loadDttm; // дата и время загрузки

    public CustomerDTO(Long customerId, String customerName, String customerAddress, LocalDate customerBirthday, String customerEmail, LocalDateTime loadDttm) {
        this.customerId = customerId;
        this.customerName = customerName;
        this.customerAddress = customerAddress;
        this.customerBirthday = customerBirthday;
        this.customerEmail = customerEmail;
        this.loadDttm = loadDttm;
    }
}
