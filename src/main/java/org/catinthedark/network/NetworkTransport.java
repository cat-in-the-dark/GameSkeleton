package org.catinthedark.network;

public abstract class NetworkTransport implements IMessageBus.Transport {
    private final Converter converter;
    private Receiver receiver;
    
    public NetworkTransport(Converter converter) {
        this.converter = converter;
    }
    
    protected abstract void sendToNetwork(String message);
    
    @Override
    public void send(Object message) {
        String json = converter.toJson(message);
        sendToNetwork(json);
    }

    /**
     * onReceive 
     * @param json
     */
    void onReceive(String json) {
        Object data = converter.fromJson(json);
        receiver.apply(data);
    }

    @Override
    public void setReceiver(Receiver receiver) {
        this.receiver = receiver;
    }
    
    public interface Converter {
        String toJson(Object data);
        Object fromJson(String json);
    }
}
