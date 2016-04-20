package org.catinthedark.network.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.catinthedark.network.JacksonConverter;
import org.catinthedark.network.MessageBus;
import org.catinthedark.network.SocketIOTransport;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class SocketIOExample {
    public static void main(String[] args) throws URISyntaxException, IOException {
        URI uri = new URI("http://localhost:9000");
        final ObjectMapper objectMapper = new ObjectMapper();
        final JacksonConverter jacksonConverter = new JacksonConverter(objectMapper);
        jacksonConverter.registerConverter(GameStartedMessage.class.getCanonicalName(), data -> {
            GameStartedMessage message = new GameStartedMessage();
            message.setRole((String)data.get("role"));
            return message;
        });
        SocketIOTransport transport =  new SocketIOTransport(jacksonConverter, uri);
        final MessageBus messageBus = new MessageBus(transport);

        messageBus.subscribe(GameStartedMessage.class, message -> {
            System.out.println(message.getRole());
        });

        transport.connect();
        System.in.read();
    }
}
