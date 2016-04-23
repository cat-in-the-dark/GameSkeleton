package com.catinthedark.server.socket;

import com.corundumstudio.socketio.SocketIOClient;
import com.sun.org.apache.xpath.internal.operations.Bool;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public final class Room {
    private final Map<UUID, Player> players;
    private final Long maxPlayers;
    private final UUID name;
    private Boolean played;

    public Room(Long maxPlayers, UUID name) {
        this.maxPlayers = maxPlayers;
        this.players = new ConcurrentHashMap<>();
        this.name = name;
        this.played = false;
    }

    public Boolean hasFreePlace() {
        return players.size() < maxPlayers;
    }
    
    public Boolean waitingForStart() {
        return hasFreePlace() && !played;
    }

    public synchronized boolean connect(Player player) {
        if (waitingForStart()) {
            if (players.size() == 0) player.setStatus("admin");
            players.put(player.getSocket().getSessionId(), player);
            return true;
        }
        return false;
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

    public void setPlayed(Boolean played) {
        this.played = played;
    }

    public Boolean getPlayed() {
        return played;
    }
}
