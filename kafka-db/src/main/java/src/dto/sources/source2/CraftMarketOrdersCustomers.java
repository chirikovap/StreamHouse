package src.dto.sources.source2;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class CraftMarketOrdersCustomers implements Serializable {

    private Long orderId; // идентификатор заказа
    private Long craftsmanId; // идентификатор мастера
    private Long productId; // идентификатор товара
    private String orderCreatedDate; // дата создания заказа
    private String orderCompletionDate; // дата выполнения заказа
    private String orderStatus; // статус выполнения заказа
    private Long customerId; // идентификатор заказчика
    private String customerName; // ФИО заказчика
    private String customerAddress; // адрес заказчика
    private String customerBirthday; // дата рождения заказчика
    private String customerEmail; // электронная почта заказчика
}