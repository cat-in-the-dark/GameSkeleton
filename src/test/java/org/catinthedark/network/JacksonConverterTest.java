package org.catinthedark.network;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class JacksonConverterTest {
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

        jacksonConverter.registerConverter(A.class.getCanonicalName(), (JacksonConverter.CustomConverter<A>) data -> {
            A obj = new A();
            obj.setA((String)data.get("a"));
            return obj;
        });
    }

    @Test
    public void testConvertToJson() {
        A a = new A();
        a.setA("Hello world");
        String json = jacksonConverter.toJson(a);
        System.out.println(json);
        Assert.assertNotNull(json);
    }

    @Test
    public void testConvertFromJson() {
        A a = new A();
        a.setA("Hello world");
        String json = jacksonConverter.toJson(a);
        System.out.println(json);

        Object data = jacksonConverter.fromJson(json);
        System.out.println(data);
        Assert.assertNotNull(data);
    }
}
