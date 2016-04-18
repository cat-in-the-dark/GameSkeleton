package org.catinthedark.network;

public interface IMessageBus {
    <T extends CommonMessage> void send(T message);
    <T extends CommonMessage> void subscribe(Class<T> clazz, Callback<T> callback);
    
    interface Callback<T extends CommonMessage> {
        void apply(T message);
    }
}
