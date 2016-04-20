package com.catinthedark.lib.network.example;

import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketConfig;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.catinthedark.lib.network.JacksonConverter;
import com.catinthedark.lib.network.NetworkTransport;
import com.catinthedark.lib.network.messages.DisconnectedMessage;
import com.catinthedark.lib.network.messages.GameStartedMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class SocketIOServerExample {
    public static Long MAX_PLAYERS = 2L;

    public static void main(String[] args) throws IOException {
        final List<Room> rooms = new ArrayList<>();
        final List<Player> playerList = new ArrayList<>();

        final ObjectMapper mapper = new ObjectMapper();
        final JacksonConverter converter = new JacksonConverter(mapper);

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

            Player player = new Player(room, socketIOClient);
            playerList.add(player);
            room.connect(player);

            room.doIfReady((players) -> players.forEach(p -> {
                GameStartedMessage gameStartedMessage = new GameStartedMessage();
                gameStartedMessage.setRole(p.getStatus());
                gameStartedMessage.setClientID(p.getSocket().getSessionId().toString());
                try {
                    String msg = converter.toJson(gameStartedMessage);
                    p.socket.sendEvent("message", msg);
                } catch (NetworkTransport.ConverterException e) {
                    e.printStackTrace(System.err);
                }
            }));
        });

        server.addEventListener("message", String.class, (client, data, ackSender) -> {
            System.out.println("Message " + client.getSessionId() + " " + data);
            JacksonConverter.Wrapper wrapper = mapper.readValue(data, JacksonConverter.Wrapper.class);
            wrapper.setSender(client.getSessionId().toString());
            String msg = mapper.writeValueAsString(wrapper);
            
            playerList
                    .stream()
                    .filter(p -> p.isEqual(client))
                    .flatMap(Player::getPlayerMatesStream)
                    .forEach(p -> p.getSocket().sendEvent("message", msg));
        });

        server.addDisconnectListener(client -> {
            System.out.println("Disconnected " + client.getSessionId());
            playerList.removeIf(p -> p.isEqual(client));
            rooms.stream()
                    .filter(room -> room.disconnect(client))
                    .forEach(r -> {
                        DisconnectedMessage msg = new DisconnectedMessage();
                        msg.setClientID(client.getSessionId());
                        try {
                            String json = converter.toJson(msg);
                            r.getPlayers().forEach(p -> p.getSocket().sendEvent("message", json));
                        } catch (NetworkTransport.ConverterException e) {
                            e.printStackTrace(System.err);
                        }
                    });
        });

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
                if (players.size() == 0) player.setStatus("admin");
                players.add(player);
            }
        }

        public boolean disconnect(SocketIOClient client) {
            return players.removeIf(player -> player.getSocket().getSessionId() == client.getSessionId());
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
        private String status;
        private final Room room;
        private final SocketIOClient socket;

        public Player(Room room, SocketIOClient client) {
            this.room = room;
            this.socket = client;
            this.status = "player";
        }

        public Room getRoom() {
            return room;
        }
        
        public Stream<Player> getPlayerMatesStream() {
            return room
                    .getPlayers()
                    .parallelStream()
                    .filter(p -> p.getSocket().getSessionId().compareTo(socket.getSessionId()) != 0);
        }
        
        public Boolean isEqual(SocketIOClient client) {
            return client.getSessionId().compareTo(socket.getSessionId()) == 0;
        }

        public SocketIOClient getSocket() {
            return socket;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }
}
