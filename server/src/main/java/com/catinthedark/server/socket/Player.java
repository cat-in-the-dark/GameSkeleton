package com.catinthedark.server.socket;

import com.corundumstudio.socketio.SocketIOClient;

import java.util.stream.Stream;

public final class Player {
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
        try {
            String address = socket.getHandshakeData().getHttpHeaders().get("X-Forwarded-For");
            if (address == null) {
                address = socket.getHandshakeData().getAddress().getHostString();
            }
            return address;
        } catch (Exception e) {
            return null;
        }
    }
}
