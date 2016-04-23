package com.catinthedark.server.socket;

import com.catinthedark.lib.network.JacksonConverter;
import com.catinthedark.lib.network.NetworkTransport;
import com.catinthedark.lib.network.messages.DisconnectedMessage;
import com.catinthedark.lib.network.messages.GameStartedMessage;
import com.catinthedark.server.Configs;
import com.catinthedark.server.persist.GameModel;
import com.catinthedark.server.persist.PlayerModel;
import com.catinthedark.server.persist.RoomRepository;
import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketConfig;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class SocketIOService {
    private static final Logger log = LoggerFactory.getLogger(SocketIOService.class);
    private static Long MAX_PLAYERS = 2L;
    private static String MESSAGE = "message";

    private final List<Room> rooms = new ArrayList<>();
    private final Map<UUID, Player> players = new ConcurrentHashMap<>();
    
    private final SocketIOServer server;

    private final RoomRepository repository;
    private final JacksonConverter converter;
    private final ObjectMapper mapper;
    
    public SocketIOService(
            final RoomRepository repository, 
            final JacksonConverter converter,
            final ObjectMapper mapper
    ) {
        Configuration config = new Configuration();
        config.setPort(Configs.getPort());
        SocketConfig socketConfig = new SocketConfig();
        socketConfig.setReuseAddress(true);
        config.setSocketConfig(socketConfig);
        
        this.server = new SocketIOServer(config);
        this.converter = converter;
        this.repository = repository;
        this.mapper = mapper;
        
        setup();
    }
    
    private void setup() {
        server.addConnectListener(socketIOClient -> {
            log.info("New connection "+socketIOClient.getSessionId().toString()+ " " + server.getAllClients().size());

            final Room room = findFreeOrCreateAndConnect(socketIOClient);

            room.doIfReady((players) -> {
                repository.startGame(room.getName().toString());
                log.info("Game started in room " + room.getName() + " " + players.stream().map(Player::getIP).collect(Collectors.joining(",")));
                players.parallelStream().forEach(p -> {
                    GameStartedMessage gameStartedMessage = new GameStartedMessage();
                    gameStartedMessage.setRole(p.getStatus());
                    gameStartedMessage.setClientID(p.getSocket().getSessionId().toString());
                    try {
                        String msg = converter.toJson(gameStartedMessage);
                        p.getSocket().sendEvent(MESSAGE, msg);
                    } catch (NetworkTransport.ConverterException e) {
                        e.printStackTrace(System.err);
                    }
                });
            });
            log.info("User serviced " + socketIOClient.getSessionId().toString());
        });

        server.addEventListener(MESSAGE, String.class, (client, data, ackSender) -> {
            final JacksonConverter.Wrapper wrapper = mapper.readValue(data, JacksonConverter.Wrapper.class);
            wrapper.setSender(client.getSessionId().toString());
            final String msg = mapper.writeValueAsString(wrapper);

            final Player player = players.get(client.getSessionId());
            if (player != null) {
                player.getPlayerMatesStream()
                        .forEach(p -> p.getSocket().sendEvent(MESSAGE, msg));
            }
        });

        server.addDisconnectListener(client -> {
            log.info("Disconnected " + client.getSessionId());
            final Player player = players.remove(client.getSessionId());
            if (player != null && player.getRoom().disconnect(client)) {
                final DisconnectedMessage msg = new DisconnectedMessage();
                msg.setClientID(client.getSessionId().toString());
                try {
                    final String json = converter.toJson(msg);
                    player.getPlayerMatesStream().forEach(p -> p.getSocket().sendEvent(MESSAGE, json));
                } catch (NetworkTransport.ConverterException e) {
                    e.printStackTrace(System.err);
                }
            }
        });
    }
    
    public void start() {
        server.start();
        Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
    }

    private synchronized Room findFreeOrCreateAndConnect(SocketIOClient socketIOClient) {
        Room room = rooms
                .parallelStream()
                .filter(Room::hasFreePlace)
                .findAny().orElseGet(() -> {
                    Room newRoom = new Room(MAX_PLAYERS, UUID.randomUUID());
                    rooms.add(newRoom);
                    return newRoom;
                });

        Player player = new Player(room, socketIOClient);
        players.put(socketIOClient.getSessionId(), player);
        room.connect(player);
        repository.update(toModel(room));

        return room;
    }

    private GameModel toModel(final Room room) {
        GameModel game = new GameModel();
        game.setMaxPlayers(room.getMaxPlayers());
        game.setName(room.getName().toString());
        game.setPlayers(
                room.getPlayers()
                        .stream()
                        .map(this::toModel)
                        .collect(Collectors.toList()));
        return game;
    }

    private PlayerModel toModel(final Player player) {
        PlayerModel pm = new PlayerModel();
        pm.setStatus(player.getStatus());
        pm.setIp(player.getIP());
        pm.setUuid(player.getSocket().getSessionId().toString());
        return pm;
    }
}
