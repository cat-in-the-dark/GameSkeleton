package org.catinthedark.network;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

public class JacksonConverter implements NetworkTransport.Converter {
    private final ObjectMapper objectMapper;
    private final Map<String, CustomConverter<?>> converters;
    
    public JacksonConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.converters = new HashMap<>();
    }
    
    @Override
    public String toJson(Object data) {
        Wrapper wrapper = new Wrapper();
        wrapper.setData(data);
        wrapper.setClassName(data.getClass().getCanonicalName());
        try {
            return objectMapper.writeValueAsString(wrapper);
        } catch (JsonProcessingException e) {
            e.printStackTrace(System.err);
            return null;
        }
    }
    
    @Override
    public Object fromJson(String json) {
        try {
            Wrapper wrapper =  objectMapper.readValue(json, Wrapper.class);
            CustomConverter converter = converters.get(wrapper.getClassName());
            if (converter != null) {
                return converter.apply((Map<String, Object>)wrapper.getData());
            } else {
                System.err.println("There is no " + wrapper.getClassName() + " converter");
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace(System.err);
            return null;
        }
    }
    
    public void registerConverter(String className, CustomConverter converter) {
        converters.put(className, converter);
    }
    
    static class Wrapper {
        private Object data;
        private String className;

        public Object getData() {
            return data;
        }

        public void setData(Object data) {
            this.data = data;
        }

        public String getClassName() {
            return className;
        }

        public void setClassName(String className) {
            this.className = className;
        }
    }
    
    public interface CustomConverter<T> {
        T apply(Map<String, Object> data);
    }
}
