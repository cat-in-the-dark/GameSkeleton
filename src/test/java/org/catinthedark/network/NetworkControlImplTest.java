package org.catinthedark.network;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.catinthedark.network.jackson.JacksonMessageConverter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

public class NetworkControlImplTest {
    private static class Some extends CommonMessage {
        private Integer x;

        public Integer getX() {
            return x;
        }

        public void setX(Integer x) {
            this.x = x;
        }
    }

    private static class Another extends CommonMessage {
        private Integer y;
        private String msg;

        public Integer getY() {
            return y;
        }

        public void setY(Integer y) {
            this.y = y;
        }

        public String getMsg() {
            return msg;
        }

        public void setMsg(String msg) {
            this.msg = msg;
        }
    }
    
    private static class DummyTransport implements Transport {
        private Receiver receiver;
        
        @Override
        public void send(String msg) {
            System.out.println("SEND " + msg);
        }

        @Override
        public void setReceiver(Receiver receiver) {
            this.receiver = receiver;
        }
        
        public void onReceive(String data) {
            receiver.apply(data);
        }
    }

    private MessageBus messageBus;
    private DummyTransport dummyTransport;
    private MessageConverter messageConverter;
    
    @Before
    public void setup() {
        messageConverter = new JacksonMessageConverter(new ObjectMapper());
        dummyTransport = new DummyTransport();
        messageBus = new MessageBus(dummyTransport, messageConverter);
    }
    
    @Test
    public void testSend() {
        Some s = new Some();
        s.setX(1);
        messageBus.send(s);
    }
    
    @Test
    public void testSubscribe() throws IOException {
        AtomicInteger integer = new AtomicInteger(0);
        messageBus.<Some>subscribe(Some.class.getCanonicalName(), message -> {
            System.out.print(message.getX());
            integer.incrementAndGet();
        });
        messageBus.<Another>subscribe(Another.class.getCanonicalName(), message -> {
            System.out.println(message.getMsg());
            integer.incrementAndGet();
        });

        Some s = new Some();
        s.setX(1);
        String jsonSome = messageConverter.writeValue(s);

        Another a = new Another();
        a.setMsg("Hello world");
        String jsonAnother = messageConverter.writeValue(a);
        
        dummyTransport.onReceive(jsonAnother);
        dummyTransport.onReceive(jsonSome);
        
        assertEquals(integer.get(), 2);
    }
}
