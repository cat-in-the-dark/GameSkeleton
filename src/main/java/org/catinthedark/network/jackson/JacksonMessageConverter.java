package org.catinthedark.network.jackson;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.catinthedark.network.CommonMessage;
import org.catinthedark.network.MessageConverter;

import java.io.IOException;

public final class JacksonMessageConverter implements MessageConverter {
    private final ObjectMapper objectMapper;
    
    public JacksonMessageConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    
    @Override
    public <T extends CommonMessage> T readValue(String data, Class<T> clazz) {
        try {
            return objectMapper.readValue(data, clazz);
        } catch (IOException e) {
            e.printStackTrace(System.err);
            return null;
        }
    }

    @Override
    public <T extends CommonMessage> String writeValue(T data) {
        try {
            return objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            return null;
        }
    }
}
