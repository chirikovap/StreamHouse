package src.dto.sources.source3;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDate;

@Getter
@Setter
public class CraftMarketCustomers implements Serializable {
    private Long customerId; // идентификатор заказчика
    private String customerName; // ФИО заказчика
    private String customerAddress; // адрес заказчика
    private LocalDate customerBirthday; // дата рождения заказчика
    private String customerEmail; // электронная почта заказчика
}