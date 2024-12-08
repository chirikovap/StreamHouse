package src.dto.DWH;

import lombok.*;

import java.sql.Date;
import java.sql.Timestamp;

@Setter
@Getter
@AllArgsConstructor
@RequiredArgsConstructor
@NoArgsConstructor
public class Order {
    public Long orderId;
    public Long productId;
    public Long craftsmanId;
    public Long customerId;
    public Date orderCreatedDate;
    public Date orderCompletionDate;
    public String orderStatus;
    public Timestamp loadDttm;

}