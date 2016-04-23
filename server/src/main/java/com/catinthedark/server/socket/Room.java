package com.catinthedark.server.socket;

import com.corundumstudio.socketio.SocketIOClient;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public final class Room {
    private final Map<UUID, Player> players;
    private final Long maxPlayers;
    private final UUID name;

    public Room(Long maxPlayers, UUID name) {
        this.maxPlayers = maxPlayers;
        this.players = new ConcurrentHashMap<>();
        this.name = name;
    }

    public Boolean hasFreePlace() {
        return players.size() < maxPlayers;
    }

    public synchronized void connect(Player player) {
        if (hasFreePlace()) {
            if (players.size() == 0) player.setStatus("admin");
            players.put(player.getSocket().getSessionId(), player);
        }
    }

    /**
     * 
     * @param client socket object
     * @return true if player disconnected, false if this room hasn't got this player
     */
    public boolean disconnect(SocketIOClient client) {
        return players.remove(client.getSessionId()) != null;
    }

    public synchronized void doIfReady(Consumer<Collection<Player>> action) {
        if (maxPlayers == players.size()) {
            action.accept(players.values());
        }
    }

    public Collection<Player> getPlayers() {
        return players.values();
    }

    public Long getMaxPlayers() {
        return maxPlayers;
    }

    public UUID getName() {
        return name;
    }
}
