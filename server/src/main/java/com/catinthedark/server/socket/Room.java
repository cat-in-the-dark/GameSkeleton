package com.catinthedark.server.socket;

import com.corundumstudio.socketio.SocketIOClient;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public final class Room {
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
