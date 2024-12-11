package src.dto.sources.source3;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDate;

@Getter
@Setter
public class CraftMarketCraftsmans implements Serializable {
    private Long craftsmanId; // идентификатор мастера
    private String craftsmanName; // ФИО мастера
    private String craftsmanAddress; // адрес мастера
    private LocalDate craftsmanBirthday; // дата рождения мастера
    private String craftsmanEmail; // электронная почта мастера
}