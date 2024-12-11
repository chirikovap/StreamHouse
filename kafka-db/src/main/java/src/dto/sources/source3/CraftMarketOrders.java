package src.dto.sources.source3;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDate;

@Getter
@Setter
public class CraftMarketOrders implements Serializable {
    private Long orderId; // идентификатор заказа
    private Long productId; // идентификатор товара
    private Long craftsmanId; // идентификатор мастера
    private Long customerId; // идентификатор заказчика
    private LocalDate orderCreatedDate; // дата создания заказа
    private LocalDate orderCompletionDate; // дата выполнения заказа
    private String orderStatus; // статус выполнения заказа (created, in progress, delivery, done)
    private String productName; // название товара ручной работы
    private String productDescription; // описание товара ручной работы
    private String productType; // тип товара ручной работы
    private Long productPrice; // цена товара ручной работы
}