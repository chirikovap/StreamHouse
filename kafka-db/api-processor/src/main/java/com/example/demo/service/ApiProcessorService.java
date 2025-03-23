package com.example.demo.service;

import com.example.demo.dto.CraftMarketWide;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class ApiProcessorService {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public void processCsvFile(String filePath, String topic) {
        try (CSVReader reader = new CSVReader(new InputStreamReader(new ClassPathResource(filePath).getInputStream()))) {
            String[] headers = reader.readNext(); // Читаем заголовки
            String[] nextLine;
            while ((nextLine = reader.readNext()) != null) {
                CraftMarketWide craftMarketWide = new CraftMarketWide();
                craftMarketWide.setCraftsmanId(Long.parseLong(nextLine[1]));
                craftMarketWide.setCraftsmanName(nextLine[2]);
                craftMarketWide.setCraftsmanAddress(nextLine[3]);
                craftMarketWide.setCraftsmanBirthday(convertDateToEpochDays(nextLine[4]));
                craftMarketWide.setCraftsmanEmail(nextLine[5]);

                craftMarketWide.setProductId(Long.parseLong(nextLine[6]));
                craftMarketWide.setProductName(nextLine[7]);
                craftMarketWide.setProductDescription(nextLine[8]);
                craftMarketWide.setProductType(nextLine[9]);
                craftMarketWide.setProductPrice(Long.parseLong(nextLine[10]));

                craftMarketWide.setOrderId(Long.parseLong(nextLine[11]));
                craftMarketWide.setOrderCreatedDate(convertDateToEpochDays(nextLine[12]));
                craftMarketWide.setOrderCompletionDate(convertDateToEpochDays(nextLine[13]));
                craftMarketWide.setOrderStatus(nextLine[14]);

                craftMarketWide.setCustomerId(Long.parseLong(nextLine[15]));
                craftMarketWide.setCustomerName(nextLine[16]);
                craftMarketWide.setCustomerAddress(nextLine[17]);
                craftMarketWide.setCustomerBirthday(convertDateToEpochDays(nextLine[18]));
                craftMarketWide.setCustomerEmail(nextLine[19]);

                String json = objectMapper.writeValueAsString(craftMarketWide);
                kafkaTemplate.send(topic, json);
                System.out.println("Sent to topic " + topic + ": " + json); // Логирование
            }
        } catch (IOException | CsvValidationException e) {
            e.printStackTrace();
        }
    }

    private String convertDateToEpochDays(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return null;
        }
        LocalDate date = LocalDate.parse(dateStr, dateFormatter);
        return String.valueOf(date.toEpochDay()); // Преобразуем дату в количество дней с 1970-01-01
    }

    @Scheduled(fixedRate = 100000000)
    public void scheduledTask() {
        String csvFilePath = "complete_craft_market_wide_20230730.csv";
        String csvTopic = "sources_table.source1.craft_market_wide";

        processCsvFile(csvFilePath, csvTopic);
    }
}