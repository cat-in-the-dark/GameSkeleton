package com.catinthedark.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.flywaydb.core.internal.util.logging.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sql2o.Connection;
import org.sql2o.ResultSetHandler;
import org.sql2o.Sql2o;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class RoomRepository {
    private final Sql2o sql;
    private final ObjectMapper objectMapper;
    private final static Logger LOG = LoggerFactory.getLogger(RoomRepository.class);
    
    private final ResultSetHandler<GameModel> resultHandler = new ResultSetHandler<GameModel>() {
        @Override
        public GameModel handle(ResultSet resultSet) throws SQLException {
            final String meta = resultSet.getString("meta");
            try {
                return objectMapper.readValue(meta, GameModel.class);
            } catch (IOException e) {
                LOG.error("Can't parse " + meta + " : " + e.getMessage(), e);
                return null;
            }
        }
    };

    public RoomRepository(final Sql2o sql2o, final ObjectMapper objectMapper) {
        this.sql = sql2o;
        this.objectMapper = objectMapper;
    }
    
    public void create(final GameModel model) {
        try(final Connection conn = sql.open()) {
            String json = objectMapper.writeValueAsString(model);
            LOG.info("Saving in db " + json);
            conn.createQuery("INSERT INTO game(meta) VALUES(:meta::jsonb)")
                    .addParameter("meta", json)
                    .executeUpdate();
        } catch (Exception e) {
            LOG.error("Can't create " + model + " : " + e.getMessage(), e);
        }
    }
    
    public void update(final GameModel model) {
        try(final Connection conn = sql.open()) {
            String json = objectMapper.writeValueAsString(model);
            LOG.info("Updating in db " + json);
            conn.createQuery("UPDATE game SET meta = :meta::jsonb WHERE meta->>'name' = :name")
                    .addParameter("meta", json)
                    .addParameter("name", model.getName())
                    .executeUpdate();
        } catch (Exception e) {
            LOG.error("Can't update " + model + " : " + e.getMessage(), e);
        }
    }
    
    public List<GameModel> findAll() {
        try(final Connection conn = sql.open()) {
            return conn
                    .createQuery("SELECT meta FROM game")
                    .executeAndFetch(resultHandler)
                    .stream().filter(m -> m != null)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            LOG.error("Can't find all : " + e.getMessage(), e);
            return new ArrayList<>();
        }
    }
    
    public void startGame(final String gameName) {
        try(final Connection conn = sql.open()) {
            GameModel model = conn.createQuery("SELECT meta FROM game WHERE meta->>'name' = :name")
                    .addParameter("name", gameName)
                    .executeAndFetchFirst(resultHandler);
            if (model != null) {
                model.setStartedAt(new Date());
                model.setPlayed(true);
                update(model);
            }
        } catch (Exception e) {
            LOG.error("Can't update game " + gameName + " : " + e.getMessage(), e);
        }
    }
    
    public static class GameModel {
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
    
    public static class PlayerModel {
        private String ip;
        private String uuid;
        private String status;

        public String getIp() {
            return ip;
        }

        public void setIp(String ip) {
            this.ip = ip;
        }

        public String getUuid() {
            return uuid;
        }

        public void setUuid(String uuid) {
            this.uuid = uuid;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        @Override
        public String toString() {
            return "PlayerModel{" +
                    "ip='" + ip + '\'' +
                    ", uuid='" + uuid + '\'' +
                    ", status='" + status + '\'' +
                    '}';
        }
    }
}
