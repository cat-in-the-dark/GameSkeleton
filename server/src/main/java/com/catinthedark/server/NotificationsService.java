package com.catinthedark.server;

import com.catinthedark.server.persist.GameModel;
import com.catinthedark.server.persist.PlayerModel;
import com.catinthedark.server.persist.RoomRepository;
import com.catinthedark.server.socket.Player;
import com.catinthedark.server.socket.Room;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

public class NotificationsService {
    private static final Logger LOG = LoggerFactory.getLogger(NotificationsService.class);
    private final ScheduledExecutorService executor;
    private final RoomRepository roomRepository;

    public NotificationsService(final RoomRepository roomRepository) {
        this.executor = Executors.newScheduledThreadPool(4);
        this.roomRepository = roomRepository;
    }
    
    public void sendNotification(final String baseMessage, final Map<UUID, Player> players, final Map<UUID, Room> rooms, final Room room) {
        executor.schedule(() -> {
            try {
                final GameModel game = roomRepository.find(room.getName()).get();
                if (game.getPlayers().size() == 1) {
                    final PlayerModel player = game.getPlayers().get(0);
                    if (player.getDisconnectedAt() == null) {
                        String location = "From unknown location.";
                        if (player.getGeo() != null) {
                            location = String.format("From country %s, City %s, Region %s. ",
                                    player.getGeo().getOrDefault("country", "unknown"),
                                    player.getGeo().getOrDefault("city", "unknown"),
                                    player.getGeo().getOrDefault("regionName", "unknown"));
                        }
                        final String message = baseMessage + location + "Players count on the server is " + players.size() + ". Rooms count is " + rooms.size();
                        final URL url = new URL(Configs.getNotificationUrl(message));
                        final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        LOG.info("Notification status " + message + ": " + connection.getResponseCode());
                        connection.disconnect();
                    } else {
                        LOG.info("Ups, player have already left the room");
                    }
                } else {
                    LOG.info("Ups, players have already started playing");
                }
            } catch (Exception e) {
                LOG.error("Can't send notification " + e.getMessage(), e);
            } 
        }, 5, TimeUnit.SECONDS);
    }
}
