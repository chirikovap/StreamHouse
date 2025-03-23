package src.dto.DWH;

import lombok.*;

import java.math.BigDecimal;
import java.sql.Date;

@Setter
@Getter
@AllArgsConstructor
@RequiredArgsConstructor
@NoArgsConstructor
public class CraftsmanReport {
    public Long craftsmanId;
    public String craftsmanName;
    public String craftsmanAddress;
    public Date craftsmanBirthday;
    public String craftsmanEmail;
    public BigDecimal craftsmanMoney;
    public Long platformMoney;
    public Long countOrder;
    public BigDecimal avgPriceOrder;
    public BigDecimal avgAgeCustomer;
    public BigDecimal medianTimeOrderCompleted;
    public String topProductCategory;
    public Long countOrderCreated;
    public Long countOrderInProgress;
    public Long countOrderDelivery;
    public Long countOrderDone;
    public Long countOrderNotDone;
    public String reportPeriod;
}