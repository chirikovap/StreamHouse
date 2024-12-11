package src.dto.sources.source1;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDate;

@Getter
@Setter
public class CraftMarketWide implements Serializable {
    private Long id; // идентификатор записи
    private Long craftsmanId; // идентификатор мастера
    private String craftsmanName; // ФИО мастера
    private String craftsmanAddress; // адрес мастера
    private LocalDate craftsmanBirthday; // дата рождения мастера
    private String craftsmanEmail; // электронная почта мастера
    private Long productId; // идентификатор товара ручной работы
    private String productName; // название товара ручной работы
    private String productDescription; // описание товара ручной работы
    private String productType; // тип товара ручной работы
    private Long productPrice; // цена товара ручной работы
    private Long orderId; // идентификатор заказа
    private LocalDate orderCreatedDate; // дата создания заказа
    private LocalDate orderCompletionDate; // дата выполнения заказа
    private String orderStatus; // статус выполнения заказа (created, in progress, delivery, done)
    private Long customerId; // идентификатор заказчика
    private String customerName; // ФИО заказчика
    private String customerAddress; // адрес заказчика
    private LocalDate customerBirthday; // дата рождения заказчика
    private String customerEmail; // электронная почта заказчика
}
