package cn.sparrowmini.bpm.server.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Converter
public class JsonArrayConverter implements AttributeConverter<List<Object>, String> {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<Object> map) {
        try {
            return mapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to convert Map to JSON string", e);
        }
    }

    @Override
    public List<Object> convertToEntityAttribute(String json) {
        if (json == null || json.isBlank()) {
            return new ArrayList<>();
        }
        try {
            return mapper.readValue(json, List.class);
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to convert JSON string to Map", e);
        }
    }
}