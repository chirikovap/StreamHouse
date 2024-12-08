package src.dto.sources.source1;

import lombok.*;

import java.sql.Date;

@Setter
@Getter
@AllArgsConstructor
@RequiredArgsConstructor
@NoArgsConstructor
public class CraftMarketWide {
    public Long id;
    public Long craftsmanId;
    public String craftsmanName;
    public String craftsmanAddress;
    public Date craftsmanBirthday;
    public String craftsmanEmail;
    public Long productId;
    public String productName;
    public String productDescription;
    public String productType;
    public Integer productPrice;
    public Long orderId;
    public Date orderCreatedDate;
    public Date orderCompletionDate;
    public String orderStatus;
    public Long customerId;
    public String customerName;
    public String customerAddress;
    public Date customerBirthday;
    public String customerEmail;
}
