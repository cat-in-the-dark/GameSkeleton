package org.catinthedark.network;

public interface MessageConverter {
    <T extends CommonMessage> T readValue(String data, Class<T> clazz);
    <T extends CommonMessage> String writeValue(T data);
}
