package com.catinthedark.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NotificationsService {
    private static final Logger LOG = LoggerFactory.getLogger(NotificationsService.class);
    private final ExecutorService executor;
    
    public NotificationsService() {
        this.executor = Executors.newSingleThreadExecutor();
    }
    
    public void sendNotification(final String message) {
        executor.submit(() -> {
            try {
                URL url = new URL(Configs.getNotificationUrl(message));
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                LOG.info("Notification status " + message + ": " + connection.getResponseCode());
                connection.disconnect();
            } catch (Exception e) {
                LOG.error("Can't send notification " + e.getMessage(), e);
            } 
        });
    }
}
