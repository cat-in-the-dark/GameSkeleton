package com.catinthedark.server.persist;

import com.catinthedark.server.GeoIP;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sql2o.Connection;
import org.sql2o.ResultSetHandler;
import org.sql2o.Sql2o;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class RoomRepository {
    private final Sql2o sql;
    private final ObjectMapper objectMapper;
    private final static Logger LOG = LoggerFactory.getLogger(RoomRepository.class);
    private final GeoIP geoIPService;

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
        this.geoIPService = new GeoIP(objectMapper);
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

    private void updateQuery(final Connection conn, final GameModel model) throws JsonProcessingException {
        String json = objectMapper.writeValueAsString(model);
        LOG.info("Updating in db " + json);
        conn.createQuery("UPDATE game SET meta = :meta::jsonb WHERE meta->>'name' = :name")
                .addParameter("meta", json)
                .addParameter("name", model.getName())
                .executeUpdate();
    }

    private GameModel findQuery(final Connection conn, final String gameName) {
        return conn.createQuery("SELECT meta FROM game WHERE meta->>'name' = :name")
                .addParameter("name", gameName)
                .executeAndFetchFirst(resultHandler);
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
        try(final Connection conn = sql.beginTransaction()) {
            GameModel model = findQuery(conn, gameName);
            if (model != null) {
                model.setStartedAt(new Date());
                model.setPlayed(true);
                updateQuery(conn, model);
            }
            conn.commit();
        } catch (Exception e) {
            LOG.error("Can't update game " + gameName + " : " + e.getMessage(), e);
        }
    }

    public void updateDisconnect(final UUID gameName, final UUID playerId) {
        try(final Connection conn = sql.beginTransaction()) {
            final GameModel model = findQuery(conn, gameName.toString());
            model.getPlayers().forEach(p -> {
                if (Objects.equals(UUID.fromString(p.getUuid()), playerId)) {
                    p.setDisconnectedAt(new Date());
                }
            });
            updateQuery(conn, model);
            conn.commit();
        } catch (Exception e) {
            LOG.error("Can't update disconnection message " + gameName + " : " + e.getMessage(), e);
        }
    }

    public void connect(UUID gameName, PlayerModel playerModel) {
        try(final Connection conn = sql.beginTransaction()) {
            final GameModel model = findQuery(conn, gameName.toString());
            playerModel.setConnectedAt(new Date());
            playerModel.setGeo(geoIPService.findByIp(playerModel.getIp()));
            model.getPlayers().add(playerModel);
            updateQuery(conn, model);
            conn.commit();
        } catch (Exception e) {
            LOG.error("Can't connect player " + playerModel + " to room " + gameName + " : " + e.getMessage(), e);
        }
    }
}
