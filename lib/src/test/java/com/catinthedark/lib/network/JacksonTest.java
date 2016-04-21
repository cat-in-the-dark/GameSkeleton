package com.catinthedark.lib.network;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class JacksonTest {
    @JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include= JsonTypeInfo.As.PROPERTY, property="@class")
    private static class Base {}
    
    private static class A extends Base {
        private Integer x;

        public Integer getX() {
            return x;
        }

        public void setX(Integer x) {
            this.x = x;
        }
    }
    
    private ObjectMapper objectMapper;
    
    @Before
    public void setup() {
        objectMapper = new ObjectMapper();
    }
    
    @Test
    public void testJson() throws IOException {
        A a = new A();
        a.setX(1);
        
        String json = objectMapper.writeValueAsString(a);
        System.out.println(json);
        
        Base data = objectMapper.readValue(json, new TypeReference<Base>() {});
        System.out.println(data);
        
        assertEquals(data.getClass(), a.getClass());
    }
}
