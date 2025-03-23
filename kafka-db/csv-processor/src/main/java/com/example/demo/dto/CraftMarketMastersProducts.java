package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class CraftMarketMastersProducts implements Serializable {

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

    @JsonProperty("product_id")
    private Long productId; // идентификатор товара

    @JsonProperty("product_name")
    private String productName; // название товара

    @JsonProperty("product_description")
    private String productDescription; // описание товара

    @JsonProperty("product_type")
    private String productType; // тип товара

    @JsonProperty("product_price")
    private Long productPrice; // цена товара
}