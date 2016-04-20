package org.catinthedark.network.example;

import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.catinthedark.network.JacksonConverter;
import org.catinthedark.network.NetworkTransport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class SocketIOServerExample {
    public static Long MAX_PLAYERS = 2L;

    public static void main(String[] args) throws IOException {
        final List<Room> rooms = new ArrayList<>();

        final JacksonConverter converter = new JacksonConverter(new ObjectMapper());

        final Configuration config = new Configuration();
        config.setHostname("localhost");
        config.setPort(9000);

        final SocketIOServer server = new SocketIOServer(config);
        server.addConnectListener(socketIOClient -> {
            System.out.println("New connection " + server.getAllClients().size());

            Room room = rooms
                    .stream()
                    .filter(Room::hasFreePlace)
                    .findAny().orElseGet(() -> {
                        Room newRoom = new Room(MAX_PLAYERS);
                        rooms.add(newRoom);
                        return newRoom;
                    });

            room.connect(socketIOClient);

            room.doIfReady((players) -> players.forEach(player -> {
                GameStartedMessage gameStartedMessage = new GameStartedMessage();
                gameStartedMessage.setRole(String.valueOf(Math.random()));
                try {
                    String msg = converter.toJson(gameStartedMessage);
                    player.sendEvent("message", msg);
                } catch (NetworkTransport.ConverterException e) {
                    e.printStackTrace(System.err);
                }
            }));
        });
        server.start();
        System.in.read();
        server.stop();
    }

    static class Room {
        private final List<SocketIOClient> players;
        private final Long maxPlayers;

        public Room(Long maxPlayers) {
            this.maxPlayers = maxPlayers;
            this.players = new ArrayList<>();
        }

        public Boolean hasFreePlace() {
            return players.size() < maxPlayers;
        }

        public void connect(SocketIOClient client) {
            if (hasFreePlace()) {
                players.add(client);
            }
        }

        public void doIfReady(Consumer<List<SocketIOClient>> action) {
            if (maxPlayers == players.size()) {
                action.accept(players);
            }
        }

        public List<SocketIOClient> getPlayers() {
            return players;
        }

        public Long getMaxPlayers() {
            return maxPlayers;
        }
    }
}
