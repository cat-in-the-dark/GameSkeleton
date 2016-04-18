package org.catinthedark.network;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class MessageBus implements IMessageBus {
    private final MessageConverter messageConverter;
    private final List<Subscriber> subscribers = new ArrayList<>();
    private Transport transport;

    public MessageBus(Transport transport, MessageConverter messageConverter) {
        this.transport = transport;
        this.messageConverter = messageConverter;

        Transport.Receiver receiver = data -> {
            CommonMessage payload = messageConverter.readValue(data, CommonMessage.class);
            subscribers.stream()
                    .filter(sub -> Objects.equals(sub.className, payload.getClass().getCanonicalName()))
                    .forEach(sub -> sub.callback.apply(payload));
        };
        transport.setReceiver(receiver);
    }

    public void send(CommonMessage message) {
        String json = messageConverter.writeValue(message);
        transport.send(json);
    }

    public <T extends CommonMessage> void subscribe(Class<T> clazz, Callback<T> callback) {
        subscribers.add(new Subscriber<>(clazz.getCanonicalName(), callback));
    }

    private static class Subscriber<T extends CommonMessage> {
        String className;
        Callback<T> callback;

        Subscriber(String className, Callback<T> callback) {
            this.className = className;
            this.callback = callback;
        }
    }
}
