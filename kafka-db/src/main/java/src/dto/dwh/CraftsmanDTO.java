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
 * DTO для таблицы dwh.d_craftsmans
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CraftsmanDTO implements Serializable {
    
    @JsonProperty("craftsman_id")
    private Long craftsmanId; // идентификатор мастера
    
    @JsonProperty("craftsman_name")
    private String craftsmanName; // ФИО мастера
    
    @JsonProperty("craftsman_address")
    private String craftsmanAddress; // адрес мастера
    
    @JsonProperty("craftsman_birthday")
    private LocalDate craftsmanBirthday; // дата рождения мастера
    
    @JsonProperty("craftsman_email")
    private String craftsmanEmail; // электронная почта мастера
    
    @JsonProperty("load_dttm")
    private LocalDateTime loadDttm; // дата и время загрузки

    public String getDeduplicationKey() {
        return craftsmanName + "|" + craftsmanAddress + "|" + craftsmanBirthday + "|" + craftsmanEmail;
    }
}
