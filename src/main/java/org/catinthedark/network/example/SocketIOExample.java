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
        jacksonConverter.registerConverter(Move.class.getCanonicalName(), data -> {
            Move message = new Move();
            message.setX((Double) data.get("x"));
            message.setY((Double)data.get("y"));
            return message;
        });
        SocketIOTransport transport =  new SocketIOTransport(jacksonConverter, uri);
        final MessageBus messageBus = new MessageBus(transport);

        messageBus.subscribe(GameStartedMessage.class, message -> {
            System.out.println(message.getRole());
            Move move = new Move();
            move.setX(Math.random());
            move.setY(Math.random());
            messageBus.send(move);
        });

        messageBus.subscribe(Move.class, message -> {
            System.out.println("Move x=" + message.getX() + " y=" + message.getY());
        });

        transport.connect();
        System.in.read();
    }

    public static class Move {
        private Double x;
        private Double y;

        public Double getX() {
            return x;
        }

        public void setX(Double x) {
            this.x = x;
        }

        public Double getY() {
            return y;
        }

        public void setY(Double y) {
            this.y = y;
        }
    }
}
