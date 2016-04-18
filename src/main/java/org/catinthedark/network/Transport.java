package org.catinthedark.network;

public interface Transport {
    void send(String msg);
    void setReceiver(Receiver receiver);
    
    interface Receiver {
        void apply(String data);
    }
}
