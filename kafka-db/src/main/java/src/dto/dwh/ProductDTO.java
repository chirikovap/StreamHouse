package src.dto.dwh;

import java.time.LocalDateTime;

public class ProductDTO {
    private Long productId;
    private String productName;
    private String productDescription;
    private String productType;
    private Long productPrice;
    private LocalDateTime loadDttm;

    // Getters and Setters
    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductDescription() {
        return productDescription;
    }

    public void setProductDescription(String productDescription) {
        this.productDescription = productDescription;
    }

    public String getProductType() {
        return productType;
    }

    public void setProductType(String productType) {
        this.productType = productType;
    }

    public Long getProductPrice() {
        return productPrice;
    }

    public void setProductPrice(Long productPrice) {
        this.productPrice = productPrice;
    }

    public LocalDateTime getLoadDttm() {
        return loadDttm;
    }

    public void setLoadDttm(LocalDateTime loadDttm) {
        this.loadDttm = loadDttm;
    }
}