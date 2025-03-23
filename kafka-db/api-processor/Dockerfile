FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/api-processor-0.0.1-SNAPSHOT.jar api-processor.jar

COPY src/main/resources/complete_craft_market_wide_20230730.csv /app/complete_craft_market_wide_20230730.csv

ENTRYPOINT ["java", "-jar", "api-processor.jar"]