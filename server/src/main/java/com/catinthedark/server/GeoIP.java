package com.catinthedark.server;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class GeoIP {
    private static final Logger LOG = LoggerFactory.getLogger(GeoIP.class);
    public static final String host = "http://ip-api.com/json/";
    private final ObjectMapper mapper;
    private final ExecutorService executor;

    public GeoIP(final ObjectMapper objectMapper) {
        this.mapper = objectMapper;
        this.executor = Executors.newFixedThreadPool(4);
    }

    public Future<Map<String, Object>> findByIp(String ipAddress) {
        return executor.submit(() -> {
            if (ipAddress == null) return null;
            LOG.info("Search geo info " + ipAddress);
            try {
                Map<String, Object> model = mapper.readValue(new URL(host + ipAddress), new TypeReference<Map<String, Object>>(){});
                if (Objects.equals(model.get("status"), "success")) {
                    return model;
                } else {
                    LOG.error("Can't get address by ip " + model + " : " + model.get("message"));
                }
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }

            return null;
        });
    }
}
