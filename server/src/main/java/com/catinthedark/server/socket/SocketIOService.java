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
import com.corundumstudio.socketio.SocketIOServer;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class SocketIOService {
    private static final Logger log = LoggerFactory.getLogger(SocketIOService.class);
    private static Long MAX_PLAYERS = 2L;
    private static String MESSAGE = "message";

    private final List<Room> rooms = new ArrayList<>();
    private final List<Player> playerList = new ArrayList<>();
    
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

            Room room = rooms
                    .parallelStream()
                    .filter(Room::hasFreePlace)
                    .findAny().orElseGet(() -> {
                        Room newRoom = new Room(MAX_PLAYERS, UUID.randomUUID());
                        rooms.add(newRoom);
                        repository.create(toModel(newRoom));
                        return newRoom;
                    });

            Player player = new Player(room, socketIOClient);
            playerList.add(player);
            room.connect(player);
            repository.update(toModel(room));

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
            JacksonConverter.Wrapper wrapper = mapper.readValue(data, JacksonConverter.Wrapper.class);
            wrapper.setSender(client.getSessionId().toString());
            String msg = mapper.writeValueAsString(wrapper);

            playerList
                    .parallelStream()
                    .filter(p -> p.isEqual(client))
                    .flatMap(Player::getPlayerMatesStream)
                    .forEach(p -> p.getSocket().sendEvent(MESSAGE, msg));
        });

        server.addDisconnectListener(client -> {
            log.info("Disconnected " + client.getSessionId());
            playerList.removeIf(p -> p.isEqual(client));
            rooms.parallelStream()
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
    }
    
    public void start() {
        server.start();
        Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
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
