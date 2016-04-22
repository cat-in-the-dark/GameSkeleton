package com.catinthedark.server;

import com.catinthedark.lib.network.JacksonConverter;
import com.catinthedark.lib.network.NetworkTransport;
import com.catinthedark.lib.network.messages.DisconnectedMessage;
import com.catinthedark.lib.network.messages.GameStartedMessage;
import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketConfig;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SocketIOServerExample {
    private static final Logger log = LoggerFactory.getLogger(SocketIOServerExample.class);
    public static Long MAX_PLAYERS = 2L;

    static private String defaultPort = "9000";
    static private String MESSAGE = "message";

    public static void main(String[] args) {
        String portStr = null;
        if (args.length > 0) portStr = args[0];
        if (portStr == null || portStr.isEmpty()) portStr = defaultPort;
        final Integer port = Integer.valueOf(portStr);

        final List<Room> rooms = new ArrayList<>();
        final List<Player> playerList = new ArrayList<>();

        final ObjectMapper mapper = new ObjectMapper();
        final JacksonConverter converter = new JacksonConverter(mapper);

        final Configuration config = new Configuration();
        config.setPort(port);
        final SocketConfig socketConfig = new SocketConfig();
        socketConfig.setReuseAddress(true);
        config.setSocketConfig(socketConfig);

        final SocketIOServer server = new SocketIOServer(config);
        server.addConnectListener(socketIOClient -> {
            log.info("New connection "+socketIOClient.getSessionId().toString()+ " " + server.getAllClients().size());

            Room room = rooms
                    .stream()
                    .filter(Room::hasFreePlace)
                    .findAny().orElseGet(() -> {
                        Room newRoom = new Room(MAX_PLAYERS, UUID.randomUUID());
                        rooms.add(newRoom);
                        return newRoom;
                    });

            Player player = new Player(room, socketIOClient);
            playerList.add(player);
            room.connect(player);

            room.doIfReady((players) -> {
                log.info("Game started in room " + room.getName() + " " + players.stream().map(Player::getIP).collect(Collectors.joining(",")));
                players.forEach(p -> {
                    GameStartedMessage gameStartedMessage = new GameStartedMessage();
                    gameStartedMessage.setRole(p.getStatus());
                    gameStartedMessage.setClientID(p.getSocket().getSessionId().toString());
                    try {
                        String msg = converter.toJson(gameStartedMessage);
                        p.socket.sendEvent(MESSAGE, msg);
                    } catch (NetworkTransport.ConverterException e) {
                        e.printStackTrace(System.err);
                    }
                });
            });
            log.info("User serviced " + socketIOClient.getSessionId().toString());
        });

        server.addEventListener(MESSAGE, String.class, (client, data, ackSender) -> {
            JacksonConverter.Wrapper wrapper = mapper.readValue(data, JacksonConverter.Wrapper.class);
            wrapper.setSender(client.getSessionId().toString());
            String msg = mapper.writeValueAsString(wrapper);

            playerList
                    .stream()
                    .filter(p -> p.isEqual(client))
                    .flatMap(Player::getPlayerMatesStream)
                    .forEach(p -> p.getSocket().sendEvent(MESSAGE, msg));
        });

        server.addDisconnectListener(client -> {
            log.info("Disconnected " + client.getSessionId());
            playerList.removeIf(p -> p.isEqual(client));
            rooms.stream()
                    .filter(room -> room.disconnect(client))
                    .forEach(r -> {
                        DisconnectedMessage msg = new DisconnectedMessage();
                        msg.setClientID(client.getSessionId().toString());
                        try {
                            String json = converter.toJson(msg);
                            r.getPlayers().forEach(p -> p.getSocket().sendEvent(MESSAGE, json));
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
        private final UUID name;

        public Room(Long maxPlayers, UUID name) {
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

        public UUID getName() {
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

        public String getIP() {
            return socket.getRemoteAddress().toString();
        }
    }
}
