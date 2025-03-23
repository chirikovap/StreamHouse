package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDate;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class CraftMarketWide implements Serializable {
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
    private Long productId; // идентификатор товара ручной работы

    @JsonProperty("product_name")
    private String productName; // название товара ручной работы

    @JsonProperty("product_description")
    private String productDescription; // описание товара ручной работы

    @JsonProperty("product_type")
    private String productType; // тип товара ручной работы

    @JsonProperty("product_price")
    private Long productPrice; // цена товара ручной работы

    @JsonProperty("order_id")
    private Long orderId; // идентификатор заказа

    @JsonProperty("order_created_date")
    private String orderCreatedDate; // дата создания заказа

    @JsonProperty("order_completion_date")
    private String orderCompletionDate; // дата выполнения заказа

    @JsonProperty("order_status")
    private String orderStatus; // статус выполнения заказа

    @JsonProperty("customer_id")
    private Long customerId; // идентификатор заказчика

    @JsonProperty("customer_name")
    private String customerName; // ФИО заказчика

    @JsonProperty("customer_address")
    private String customerAddress; // адрес заказчика

    @JsonProperty("customer_birthday")
    private String customerBirthday; // дата рождения заказчика

    @JsonProperty("customer_email")
    private String customerEmail; // электронная почта заказчика
}
