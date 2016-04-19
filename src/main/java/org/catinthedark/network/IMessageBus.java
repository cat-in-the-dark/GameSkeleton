package org.catinthedark.network;

public interface IMessageBus {
    void send(Object message);
    <T> void subscribe(Class<T> clazz, Callback<T> callback);
    
    interface Callback<T> {
        void apply(T message);
    }

    interface Transport {
        void send(Object message);
        void setReceiver(Receiver receiver);

        interface Receiver {
            void apply(Object data);
        }
    }
}
