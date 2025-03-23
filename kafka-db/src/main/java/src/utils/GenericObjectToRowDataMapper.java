package src.utils;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.table.data.GenericRowData;
import org.apache.flink.table.data.RowData;

import java.lang.reflect.Field;

public class GenericObjectToRowDataMapper<T> implements MapFunction<T, RowData> {

    @Override
    public RowData map(T value) throws Exception {
        Field[] fields = value.getClass().getDeclaredFields();
        GenericRowData row = new GenericRowData(fields.length);

        for (int i = 0; i < fields.length; i++) {
            fields[i].setAccessible(true);
            Object fieldValue = fields[i].get(value);
            row.setField(i, fieldValue);
        }

        return row;
    }
}
