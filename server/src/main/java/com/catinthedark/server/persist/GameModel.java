package com.catinthedark.server.persist;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public final class GameModel {
    private Date startedAt = null;
    private Long maxPlayers = 0L;
    private String name = null;
    private List<PlayerModel> players = new ArrayList<>();
    private Boolean played = false;

    public Date getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Date startedAt) {
        this.startedAt = startedAt;
    }

    public Long getMaxPlayers() {
        return maxPlayers;
    }

    public void setMaxPlayers(Long maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<PlayerModel> getPlayers() {
        return players;
    }

    public void setPlayers(List<PlayerModel> players) {
        this.players = players;
    }

    public Boolean getPlayed() {
        return played;
    }

    public void setPlayed(Boolean played) {
        this.played = played;
    }

    @Override
    public String toString() {
        return "GameModel{" +
                "startedAt=" + startedAt +
                ", maxPlayers=" + maxPlayers +
                ", name='" + name + '\'' +
                ", players=" + players +
                ", played=" + played +
                '}';
    }
}
