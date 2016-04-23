package com.catinthedark.server;

import com.catinthedark.lib.network.JacksonConverter;
import com.catinthedark.server.persist.RoomRepository;
import com.catinthedark.server.socket.SocketIOService;
import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketConfig;
import com.corundumstudio.socketio.SocketIOServer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sql2o.Sql2o;

public class SocketIOServerExample {
    private static final Logger LOG = LoggerFactory.getLogger(SocketIOServerExample.class);

    public static void main(String[] args) {
        final ObjectMapper mapper = new ObjectMapper();
        final Sql2o sql2o = new Sql2o(Configs.getJdbcURL(), Configs.getDbUser(), Configs.getDbPassword());
        final Flyway flyway = new Flyway();
        flyway.setDataSource(sql2o.getDataSource());
        flyway.migrate();
        final RoomRepository repository = new RoomRepository(sql2o, mapper);

        LOG.info("Init SocketIOService");
        final SocketIOServer server = setupNettyServer(mapper, repository);
        final SocketIOService socketIOService = new SocketIOService(
                repository, new JacksonConverter(mapper), mapper, server);
        socketIOService.start();
    }
    
    private static SocketIOServer setupNettyServer(ObjectMapper objectMapper, RoomRepository roomRepository) {
        Configuration config = new Configuration();
        config.setPort(Configs.getPort());
        SocketConfig socketConfig = new SocketConfig();
        socketConfig.setReuseAddress(true);
        config.setSocketConfig(socketConfig);
        SocketIOServer server = new SocketIOServer(config);
        ExtendedPipelineFactory pipeline = new ExtendedPipelineFactory();
        pipeline.registerJsonHandler("/games.json", () -> {
            try {
                return objectMapper.writeValueAsString(roomRepository.findAll());
            } catch (JsonProcessingException e) {
                return "{\"error\": \""+e.getMessage()+"\"}";
            }
        });
        server.setPipelineFactory(pipeline);
        
        return server;
    }
}
