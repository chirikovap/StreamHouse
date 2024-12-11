package src.handler;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonHandler {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static <T> T fromJson(String json, Class<T> clazz) throws Exception {
        return objectMapper.readValue(json, clazz);
    }
}