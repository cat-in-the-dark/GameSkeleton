package org.catinthedark.network.example;

import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketConfig;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.catinthedark.network.JacksonConverter;
import org.catinthedark.network.NetworkTransport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class SocketIOServerExample {
    public static Long MAX_PLAYERS = 2L;

    public static void main(String[] args) throws IOException {
        final List<Room> rooms = new ArrayList<>();

        final JacksonConverter converter = new JacksonConverter(new ObjectMapper());

        final Configuration config = new Configuration();
        config.setHostname("localhost");
        config.setPort(9000);
        final SocketConfig socketConfig = new SocketConfig();
        socketConfig.setReuseAddress(true);
        config.setSocketConfig(socketConfig);

        final SocketIOServer server = new SocketIOServer(config);
        server.addConnectListener(socketIOClient -> {
            System.out.println("New connection " + server.getAllClients().size());

            Room room = rooms
                    .stream()
                    .filter(Room::hasFreePlace)
                    .findAny().orElseGet(() -> {
                        Room newRoom = new Room(MAX_PLAYERS, UUID.randomUUID().toString());
                        rooms.add(newRoom);
                        return newRoom;
                    });

            room.connect(new Player(room, socketIOClient));

            room.doIfReady((players) -> players.forEach(player -> {
                GameStartedMessage gameStartedMessage = new GameStartedMessage();
                gameStartedMessage.setRole(String.valueOf(Math.random()));
                try {
                    String msg = converter.toJson(gameStartedMessage);
                    player.socket.sendEvent("message", msg);
                } catch (NetworkTransport.ConverterException e) {
                    e.printStackTrace(System.err);
                }
            }));
        });

        server.addEventListener("message", String.class, (client, data, ackSender) -> {
            System.out.println("Message " + client + " " + data);
            client.getAllRooms().stream().forEach(room -> server.getRoomOperations(room).getClients().forEach(roomClient -> {
                if (roomClient.getSessionId() != client.getSessionId()) roomClient.sendEvent("message", data);
            }));
        });

        server.addDisconnectListener(client -> rooms.forEach(room -> room.disconnect(client)));

        server.start();
        Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
    }

    static class Room {
        private final List<Player> players;
        private final Long maxPlayers;
        private final String name;

        public Room(Long maxPlayers, String name) {
            this.maxPlayers = maxPlayers;
            this.players = new ArrayList<>();
            this.name = name;
        }

        public Boolean hasFreePlace() {
            return players.size() < maxPlayers;
        }

        public void connect(Player player) {
            if (hasFreePlace()) {
                players.add(player);
                player.getSocket().joinRoom(name);
            }
        }

        public void disconnect(SocketIOClient client) {
            players.removeIf(player -> player.getSocket().getSessionId() == client.getSessionId());
        }

        public void doIfReady(Consumer<List<Player>> action) {
            if (maxPlayers == players.size()) {
                action.accept(players);
            }
        }

        public List<Player> getPlayers() {
            return players;
        }

        public Long getMaxPlayers() {
            return maxPlayers;
        }

        public String getName() {
            return name;
        }
    }

    static class Player {
        private final Room room;
        private final SocketIOClient socket;

        public Player(Room room, SocketIOClient client) {
            this.room = room;
            this.socket = client;
        }

        public Room getRoom() {
            return room;
        }

        public SocketIOClient getSocket() {
            return socket;
        }
    }
}
