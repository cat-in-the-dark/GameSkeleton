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
            System.out.println(data);
            CommonMessage payload = messageConverter.readValue(data, CommonMessage.class);
            payload.setTypeName(payload.getClass().getCanonicalName());
            subscribers.stream()
                    .filter(sub -> Objects.equals(sub.className, payload.getTypeName()))
                    .forEach(sub -> sub.callback.apply(payload));
        };
        transport.setReceiver(receiver);
    }

    public void send(CommonMessage message) {
        String json = messageConverter.writeValue(message);
        transport.send(json);
    }

    public <T extends CommonMessage> void subscribe(String handlerName, Callback<T> callback) {
        System.out.println("SUBSCRIBED " + handlerName);
        subscribers.add(new Subscriber<>(handlerName, callback));
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
