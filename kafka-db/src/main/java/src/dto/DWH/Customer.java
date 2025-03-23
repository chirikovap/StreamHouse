package src.dto.DWH;

import lombok.*;

import java.sql.Date;
import java.sql.Timestamp;

@Setter
@Getter
@AllArgsConstructor
@RequiredArgsConstructor
@NoArgsConstructor
public class Customer {
    public Long customerId;
    public String customerName;
    public String customerAddress;
    public Date customerBirthday;
    public String customerEmail;
    public Timestamp loadDttm;

}