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
public class CraftMarketCraftsmans implements Serializable {
    @JsonProperty("craftsman_id")
    private Long craftsmanId; // идентификатор мастера

    @JsonProperty("craftsman_name")
    private String craftsmanName; // ФИО мастера

    @JsonProperty("craftsman_address")
    private String craftsmanAddress; // адрес мастера

    @JsonProperty("craftsman_birthday")
    private String craftsmanBirthday; // дата рождения мастера

    @JsonProperty("craftsman_email")
    private String craftsmanEmail; // электронная почта мастера
}