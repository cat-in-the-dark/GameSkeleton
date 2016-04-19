package org.catinthedark.network;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MessageBus implements IMessageBus {
    private final List<Subscriber<?>> subscribers = new ArrayList<>();
    private final Transport transport;
    
    public MessageBus(Transport transport) {
        this.transport = transport;
        this.transport.setReceiver(data -> subscribers
                .stream()
                .filter(sub -> Objects.equals(sub.className, data.getClass().getCanonicalName()))
                .forEach(sub -> sub.send(data)));
    }
    
    @Override
    public void send(Object message) {
        transport.send(message);
    }
    
    @Override
    public <T> void subscribe(Class<T> clazz, Callback<T> callback) {
        Subscriber<T> subscriber = new Subscriber<>(clazz.getCanonicalName(), callback);
        subscribers.add(subscriber);
    }

    static class Subscriber<T> {
        final String className;
        final Callback<T> callback;

        Subscriber(String className, Callback<T> callback) {
            this.className = className;
            this.callback = callback;
        }
        
        void send(Object data) {
            callback.apply((T)data);
        }
    }
}
