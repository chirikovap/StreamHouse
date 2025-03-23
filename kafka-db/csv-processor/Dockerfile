FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/csv-processor-0.0.1-SNAPSHOT.jar csv-processor.jar

COPY src/main/resources/complete_craft_market_orders_customers_202305071535.csv /app/complete_craft_market_orders_customers_202305071535.csv
COPY src/main/resources/complete_craft_market_masters_products_202305071535.csv /app/complete_craft_market_masters_products_202305071535.csv

ENTRYPOINT ["java", "-jar", "csv-processor.jar"]