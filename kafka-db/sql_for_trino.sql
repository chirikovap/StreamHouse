CREATE SCHEMA IF NOT EXISTS iceberg.dwh
WITH (
  location = 's3://datalake/iceberg/dwh/'
);

DROP TABLE IF EXISTS iceberg.dwh.d_customers;

CREATE TABLE iceberg.dwh.d_customers (
  customer_id BIGINT,
  customer_name STRING,
  customer_address STRING,
  customer_birthday DATE,
  customer_email STRING,
  load_dttm TIMESTAMP
)
WITH (
  format = 'parquet',
  partitioning = ARRAY['load_dttm']
);

DROP TABLE IF EXISTS iceberg.dwh.d_products;

CREATE TABLE iceberg.dwh.d_products (
  product_id BIGINT,
  product_name STRING,
  product_description STRING,
  product_type STRING,
  product_price BIGINT,
  load_dttm TIMESTAMP
)
WITH (
  format = 'parquet',
  partitioning = ARRAY['load_dttm']
);

DROP TABLE IF EXISTS iceberg.dwh.d_craftsmans;

CREATE TABLE iceberg.dwh.d_craftsmans (
  craftsman_id BIGINT,
  craftsman_name STRING,
  craftsman_address STRING,
  craftsman_birthday DATE,
  craftsman_email STRING,
  load_dttm TIMESTAMP
)
WITH (
  format = 'parquet',
  partitioning = ARRAY['load_dttm']
);

DROP TABLE IF EXISTS iceberg.dwh.f_orders;

CREATE TABLE iceberg.dwh.f_orders (
  order_id BIGINT,
  product_id BIGINT,
  craftsman_id BIGINT,
  customer_id BIGINT,
  order_created_date DATE,
  order_completion_date DATE,
  order_status STRING,
  load_dttm TIMESTAMP
)
WITH (
  format = 'parquet',
  partitioning = ARRAY['load_dttm']
);
