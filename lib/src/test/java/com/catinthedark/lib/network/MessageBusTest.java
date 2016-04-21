package com.catinthedark.lib.network;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

public class MessageBusTest {
    private MessageBus messageBus;
    private NetworkTransport networkTransport;
    private JacksonConverter jacksonConverter;
    
    public static class A {
        private String a;

        public String getA() {
            return a;
        }

        public void setA(String a) {
            this.a = a;
        }

        @Override
        public String toString() {
            return "A{" +
                    "a='" + a + '\'' +
                    '}';
        }
    }
    
    @Before
    public void setup() {
        ObjectMapper objectMapper = new ObjectMapper();
        jacksonConverter = new JacksonConverter(objectMapper);
        networkTransport = new EmptyNetworkTransport(jacksonConverter);
        messageBus = new MessageBus(networkTransport);
        
        jacksonConverter.registerConverter(A.class.getCanonicalName(), (JacksonConverter.CustomConverter<A>) data -> {
            A obj = new A();
            obj.setA((String)data.get("a"));
            return obj;
        });
    }
    
    @Test
    public void testOnReceive() throws NetworkTransport.ConverterException {
        A a = new A();
        a.setA("Hello world");
        AtomicInteger integer = new AtomicInteger(0);
        
        messageBus.subscribe(A.class, (message, sender) -> {
            System.out.println(message);
            if (Objects.equals(message.getA(), a.getA())) {
                integer.incrementAndGet();
            }
        });
        
        networkTransport.onReceive(jacksonConverter.toJson(a));
        assertEquals(integer.get(), 1);
    }
    
    @Test
    public void testSend() {
        A a = new A();
        a.setA("Hello world");
        messageBus.send(a);
    }
}
