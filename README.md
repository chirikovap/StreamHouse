# Выпускная квалификационная работа 
# студента 4 курса группы М8О-401Б-21
# Чириковой Полины Сергеевны
# на тему "StreamHouse - корпоративное хранилище данных на основе streaming processing"

Описание основной структуры проекта

- kafka-db/debezium-curl.txt - хранит в себе основные команды, которые нужны для запуска Debezium, подключения kafka connector, его удаления и настройки
- kafka-db/docker-compose.yml - система сборки, запуска и управления множеством контейнеров, хранит image всех необходимых в проекте контейнеров
- kafka-db/api-processor/ реализация API, которое считаывает CSV файл, парсит и отправляет в kafka топик
- kafka-db/csv-processor/ реализация API, которое по scheduller считывает новый CSV файл, парсит и отправляет в kafka топик

- kafka-db/src/main/java/src/jobs/UpsertCraftsmanJob.java
- kafka-db/src/main/java/src/jobs/UpsertCustomersJob.java
- kafka-db/src/main/java/src/jobs/UpsertOrdersFactJob.java
- kafka-db/src/main/java/src/jobs/UpsertProductsJob.java - Flink jobs, которые обрабатывают в real time данные из kafka топиков по заказчикам, продавцам, товарам и заказам

- kafka-db/src/main/java/src/services/RunnerService.java - сервис для запуска IcebergCreator (который в свою очередь создаст таблицы в S3 хранилища, где потом будут храниться parquet файлы в табличном формате
- kafka-db/src/main/java/src/bdCreator/IcebergTableCreator.java - создатель iceberg таблиц в minio S3
- kafka-db/src/main/java/src/dto/ - хранит структуру всех data transfer objects
- kafka-db/src/main/java/src/deserialization/MyKafkaDeserializationSchema.java - kafka десереализатор 
