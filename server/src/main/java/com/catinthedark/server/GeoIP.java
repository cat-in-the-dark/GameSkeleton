package com.catinthedark.server;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Map;
import java.util.Objects;

public class GeoIP {
    private static final Logger LOG = LoggerFactory.getLogger(GeoIP.class);
    public static final String host = "http://ip-api.com/json/";
    private final ObjectMapper mapper;

    public GeoIP(final ObjectMapper objectMapper) {
        this.mapper = objectMapper;
    }

    public Map<String, Object> findByIp(String ipAddress) {
        if (ipAddress == null) return null;
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
    }
}
