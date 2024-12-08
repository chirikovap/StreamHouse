package src.dto.DWH;


import lombok.*;

import java.sql.Date;
import java.sql.Timestamp;

@Setter
@Getter
@AllArgsConstructor
@RequiredArgsConstructor
@NoArgsConstructor
public class Craftsman {
    public Long craftsmanId;
    public String craftsmanName;
    public String craftsmanAddress;
    public Date craftsmanBirthday;
    public String craftsmanEmail;
    public Timestamp loadDttm;
}