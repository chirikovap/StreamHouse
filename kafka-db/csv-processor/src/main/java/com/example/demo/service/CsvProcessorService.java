package com.example.demo.service;

import com.example.demo.dto.CraftMarketMastersProducts;
import com.example.demo.dto.CraftMarketOrdersCustomers;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

@Service
public class CsvProcessorService {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @PostConstruct
    public void init() {
        String filePath1 = "complete_craft_market_orders_customers_202305071535.csv";
        String topic1 = "sources_table.source2.craft_market_orders_customers";

        String filePath2 = "complete_craft_market_masters_products_202305071535.csv";
        String topic2 = "sources_table.source2.craft_market_masters_products";

        processCsvFiles(filePath1, topic1, filePath2, topic2);
    }

    public void processCsvFiles(String filePath1, String topic1, String filePath2, String topic2) {
        processCsvFile(filePath1, topic1, true);
        processCsvFile(filePath2, topic2, false);
    }

    private void processCsvFile(String filePath, String topic, boolean isOrdersCustomers) {
        try (InputStream inputStream = new ClassPathResource(filePath).getInputStream();
             Reader reader = new InputStreamReader(inputStream);
             CSVParser parser = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(reader)) {

            for (CSVRecord record : parser) {
                String json;
                if (isOrdersCustomers) {
                    json = processOrdersCustomers(record);
                } else {
                    json = processMastersProducts(record);
                }

                kafkaTemplate.send(topic, json);
                System.out.println("Отправлено в топик " + topic + ": " + json); // Логирование для отладки
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String processOrdersCustomers(CSVRecord record) throws IOException {
        CraftMarketOrdersCustomers order = new CraftMarketOrdersCustomers();
        order.setOrderId(Long.parseLong(record.get("order_id")));
        order.setCraftsmanId(Long.parseLong(record.get("craftsman_id")));
        order.setProductId(Long.parseLong(record.get("product_id")));
        order.setOrderCreatedDate(convertDateToEpochDays(record.get("order_created_date")));
        order.setOrderCompletionDate(convertDateToEpochDays(record.get("order_completion_date")));
        order.setOrderStatus(record.get("order_status"));
        order.setCustomerId(Long.parseLong(record.get("customer_id")));
        order.setCustomerName(record.get("customer_name"));
        order.setCustomerAddress(record.get("customer_address"));
        order.setCustomerBirthday(convertDateToEpochDays(record.get("customer_birthday")));
        order.setCustomerEmail(record.get("customer_email"));

        return objectMapper.writeValueAsString(order);
    }

    private String processMastersProducts(CSVRecord record) throws IOException {
        CraftMarketMastersProducts product = new CraftMarketMastersProducts();
        product.setCraftsmanId(Long.parseLong(record.get("craftsman_id")));
        product.setCraftsmanName(record.get("craftsman_name"));
        product.setCraftsmanAddress(record.get("craftsman_address"));
        product.setCraftsmanBirthday(convertDateToEpochDays(record.get("craftsman_birthday")));
        product.setCraftsmanEmail(record.get("craftsman_email"));
        product.setProductId(Long.parseLong(record.get("product_id")));
        product.setProductName(record.get("product_name"));
        product.setProductDescription(record.get("product_description"));
        product.setProductType(record.get("product_type"));

        // Обработка product_price
        try {
            product.setProductPrice(Long.parseLong(record.get("product_price")));
        } catch (NumberFormatException e) {
            System.err.println("Некорректное значение product_price: " + record.get("product_price") + ". Установлено значение по умолчанию: 0.");
            product.setProductPrice(0L); // Значение по умолчанию
        }

        return objectMapper.writeValueAsString(product);
    }

    private String convertDateToEpochDays(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return null;
        }
        LocalDate date = LocalDate.parse(dateStr, dateFormatter);
        return String.valueOf(date.toEpochDay()); // Преобразуем дату в количество дней с 1970-01-01
    }
}