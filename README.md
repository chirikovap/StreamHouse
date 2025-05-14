# Выпускная квалификационная работа 
# студента 4 курса группы М8О-401Б-21
# Чириковой Полины Сергеевны
# на тему "StreamHouse - корпоративное хранилище данных на основе streaming processing"

Описание основной структуры проекта

- kafka-db/debezium-curl.txt - хранит в себе основные команды, которые нужны для запуска Debezium, подключения kafka connector, его удаления и настройки
CURL для создания kafka connector (убирает метаданные из сообщений в kafka, говорит, какие отслеживаем таблицы и в каких схемах БД:
```
curl -X POST http://localhost:8083/connectors \
      -H "Content-Type: application/json" \
      -d '{
          "name": "postgres-connector",
          "config": {
              "connector.class": "io.debezium.connector.postgresql.PostgresConnector",
              "database.hostname": "db",
              "database.port": "5432",
              "database.user": "postgres",
              "database.password": "123456",
              "database.dbname": "sources_table",
              "database.server.name": "dbserver1",
              "table.include.list": "source3.craft_market_craftsmans,source3.craft_market_customers,source3.craft_market_orders",
              "plugin.name": "pgoutput",
              "database.history.kafka.bootstrap.servers": "kafka:9092",
              "database.history.kafka.topic": "schema-changes.sources_table",
              "transforms": "unwrap",
              "transforms.unwrap.type": "io.debezium.transforms.ExtractNewRecordState",
              "transforms.unwrap.drop.tombstones": "false",
              "transforms.unwrap.delete.handling.mode": "rewrite",
              "value.converter": "org.apache.kafka.connect.json.JsonConverter",
              "value.converter.schemas.enable": "false",
              "key.converter": "org.apache.kafka.connect.json.JsonConverter",
              "key.converter.schemas.enable": "false",
              "topic.prefix": "sources_table"
          }
      }'
```

- kafka-db/docker-compose.yml - система сборки, запуска и управления множеством контейнеров, хранит image всех необходимых в проекте контейнеров
Пример для kafka:
```
  kafka:
    image: bitnami/kafka:latest
    restart: always
    ports:
      - "9092:9092"
    environment:
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka:9092
      KAFKA_LISTENERS: PLAINTEXT://0.0.0.0:9092
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
    depends_on:
      - zookeeper
    networks:
      - flinknet
```

- kafka-db/api-processor/ реализация API, которое по scheduller считывает новый CSV файл, парсит и отправляет в kafka топик
Пример настройки scheduller для сервиса:
```
    @Scheduled(fixedRate = 100000000)
    public void scheduledTask() {
        String csvFilePath = "complete_craft_market_wide_20230730.csv";
        String csvTopic = "sources_table.source1.craft_market_wide";

        processCsvFile(csvFilePath, csvTopic);
    }
```
- kafka-db/csv-processor/ реализация API, которое считаывает CSV файл, парсит и отправляет в kafka топик

FLINK:
- kafka-db/src/main/java/src/jobs/UpsertCraftsmanJob.java
- kafka-db/src/main/java/src/jobs/UpsertCustomersJob.java
- kafka-db/src/main/java/src/jobs/UpsertOrdersFactJob.java
- kafka-db/src/main/java/src/jobs/UpsertProductsJob.java - Flink jobs, которые обрабатывают в real time данные из kafka топиков по заказчикам, продавцам, товарам и заказам

ICEBERG:
- kafka-db/src/main/java/src/services/RunnerService.java - сервис для запуска IcebergCreator (который в свою очередь создаст таблицы в S3 хранилища, где потом будут храниться parquet файлы в табличном формате
- kafka-db/src/main/java/src/bdCreator/IcebergTableCreator.java - создатель iceberg таблиц в minio S3
Пример для создания таблицы Customers в Iceberg:
```
    private void createCustomersTable() {
        TableIdentifier tableId = TableIdentifier.of("dwh", "d_customers");
        if (!catalog.tableExists(tableId)) {
            logger.info("Creating table: {}", tableId);
            Schema schema = new Schema(
                    Types.NestedField.required(1, "customer_id",       Types.LongType.get()),
                    Types.NestedField.optional(2, "customer_name",      Types.StringType.get()),
                    Types.NestedField.optional(3, "customer_address",   Types.StringType.get()),
                    Types.NestedField.optional(4, "customer_birthday",  Types.DateType.get()),
                    Types.NestedField.required(5, "customer_email",     Types.StringType.get()),
                    Types.NestedField.required(6, "load_dttm",          Types.TimestampType.withoutZone())
            );
            PartitionSpec spec = PartitionSpec.unpartitioned();
            catalog.createTable(tableId, schema, spec);
        } else {
            logger.info("Table already exists: {}", tableId);
        }
    }
```
ДРУГОЕ:
- kafka-db/src/main/java/src/dto/... - хранит структуру всех data transfer objects
- kafka-db/src/main/java/src/deserialization/MyKafkaDeserializationSchema.java - kafka десереализатор
- kafka-db/src/main/java/src/utils/GenericObjectToRowDataMapper.java - маппер (преобразователь), который конвертирует объект произвольного типа T в RowData (формат строки данных, часто используемый в обработке данных, например, в Apache Flink)
