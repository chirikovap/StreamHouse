package src.bdCreator;

import org.apache.hadoop.conf.Configuration;
import org.apache.iceberg.Schema;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.catalog.Catalog;
import org.apache.iceberg.catalog.TableIdentifier;
import org.apache.iceberg.hadoop.HadoopCatalog;
import org.apache.iceberg.types.Types;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class IcebergTableCreator {

    private static final Logger logger = LoggerFactory.getLogger(IcebergTableCreator.class);

    private final Catalog catalog;

    public IcebergTableCreator() {
        // Настройки Hadoop для доступа к S3 (MinIO)
        Configuration hadoopConf = new Configuration();
        hadoopConf.set("fs.s3a.access.key", "minioadmin");
        hadoopConf.set("fs.s3a.secret.key", "minioadmin");
        hadoopConf.set("fs.s3a.endpoint",   "http://minio:9000");
        hadoopConf.set("fs.s3a.path.style.access", "true");
        hadoopConf.set("fs.s3a.region", "us-east-1");

        // Путь к warehouse (хранилищу метаданных Iceberg) на MinIO
        String warehousePath = "s3a://craftmarket/warehouse";
        this.catalog = new HadoopCatalog(hadoopConf, warehousePath);
    }

    public void createTablesIfNotExist() {
        createCustomersTable();
        createProductsTable();
        createCraftsmansTable();
        createOrdersTable();
    }

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

    private void createProductsTable() {
        TableIdentifier tableId = TableIdentifier.of("dwh", "d_products");
        if (!catalog.tableExists(tableId)) {
            logger.info("Creating table: {}", tableId);
            Schema schema = new Schema(
                    Types.NestedField.required(1, "product_id",         Types.LongType.get()),
                    Types.NestedField.required(2, "product_name",       Types.StringType.get()),
                    Types.NestedField.required(3, "product_description",Types.StringType.get()),
                    Types.NestedField.required(4, "product_type",       Types.StringType.get()),
                    Types.NestedField.required(5, "product_price",      Types.LongType.get()),
                    Types.NestedField.required(6, "load_dttm",          Types.TimestampType.withoutZone())
            );
            PartitionSpec spec = PartitionSpec.unpartitioned();
            catalog.createTable(tableId, schema, spec);
        } else {
            logger.info("Table already exists: {}", tableId);
        }
    }

    private void createCraftsmansTable() {
        TableIdentifier tableId = TableIdentifier.of("dwh", "d_craftsmans");
        if (!catalog.tableExists(tableId)) {
            logger.info("Creating table: {}", tableId);
            Schema schema = new Schema(
                    Types.NestedField.required(1, "craftsman_id",      Types.LongType.get()),
                    Types.NestedField.required(2, "craftsman_name",    Types.StringType.get()),
                    Types.NestedField.required(3, "craftsman_address", Types.StringType.get()),
                    Types.NestedField.required(4, "craftsman_birthday",Types.DateType.get()),
                    Types.NestedField.required(5, "craftsman_email",   Types.StringType.get()),
                    Types.NestedField.required(6, "load_dttm",         Types.TimestampType.withoutZone())
            );
            PartitionSpec spec = PartitionSpec.unpartitioned();
            catalog.createTable(tableId, schema, spec);
        } else {
            logger.info("Table already exists: {}", tableId);
        }
    }

    private void createOrdersTable() {
        TableIdentifier tableId = TableIdentifier.of("dwh", "f_orders");
        if (!catalog.tableExists(tableId)) {
            logger.info("Creating table: {}", tableId);
            Schema schema = new Schema(
                    Types.NestedField.required(1, "order_id",            Types.LongType.get()),
                    Types.NestedField.required(2, "product_id",          Types.LongType.get()),
                    Types.NestedField.required(3, "craftsman_id",        Types.LongType.get()),
                    Types.NestedField.required(4, "customer_id",         Types.LongType.get()),
                    Types.NestedField.optional(5, "order_created_date",  Types.DateType.get()),
                    Types.NestedField.optional(6, "order_completion_date", Types.DateType.get()),
                    Types.NestedField.required(7, "order_status",        Types.StringType.get()),
                    Types.NestedField.required(8, "load_dttm",           Types.TimestampType.withoutZone())
            );
            PartitionSpec spec = PartitionSpec.unpartitioned();
            catalog.createTable(tableId, schema, spec);
        } else {
            logger.info("Table already exists: {}", tableId);
        }
    }
}
