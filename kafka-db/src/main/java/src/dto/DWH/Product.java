package src.dto.DWH;

import lombok.*;

import java.sql.Timestamp;

@Setter
@Getter
@AllArgsConstructor
@RequiredArgsConstructor
@NoArgsConstructor
public class Product {
    public Long productId;
    public String productName;
    public String productDescription;
    public String productType;
    public Integer productPrice;
    public Timestamp loadDttm;

}