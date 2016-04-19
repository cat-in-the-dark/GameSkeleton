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
        Wrapper wrapper = new Wrapper();
        wrapper.setData(message);
        wrapper.setClassName(message.getClass().getCanonicalName());
        String json = converter.toJson(wrapper);
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
    
    static class Wrapper {
        private Object data;
        private String className;
        
        public Object getData() {
            return data;
        }

        public void setData(Object data) {
            this.data = data;
        }

        public String getClassName() {
            return className;
        }

        public void setClassName(String className) {
            this.className = className;
        }
    }
    
    public interface Converter {
        String toJson(Object data);
        Object fromJson(String json);
    }
}
