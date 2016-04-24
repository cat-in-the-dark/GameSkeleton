package com.catinthedark.server;

import com.catinthedark.server.persist.GameModel;
import com.catinthedark.server.persist.PlayerModel;
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
    
    public NotificationsService() {
        this.executor = Executors.newScheduledThreadPool(4);
    }
    
    public void sendNotification(final String baseMessage, Map<UUID, Player> players, Map<UUID, Room> rooms, Future<GameModel> gameModelFuture) {
        executor.schedule(() -> {
            try {
                final GameModel game = gameModelFuture.get();
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
