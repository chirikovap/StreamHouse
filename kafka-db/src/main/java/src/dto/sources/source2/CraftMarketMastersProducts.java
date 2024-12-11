package src.dto.sources.source2;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class CraftMarketMastersProducts implements Serializable {

    private Long craftsmanId; // идентификатор мастера
    private String craftsmanName; // ФИО мастера
    private String craftsmanAddress; // адрес мастера
    private String craftsmanBirthday; // дата рождения мастера
    private String craftsmanEmail; // электронная почта мастера
    private Long productId; // идентификатор товара
    private String productName; // название товара
    private String productDescription; // описание товара
    private String productType; // тип товара
    private Long productPrice; // цена товара
}