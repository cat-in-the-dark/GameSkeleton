package com.catinthedark.server;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;

public class Configs {
    private static ProcessBuilder pb = new ProcessBuilder();
    
    public static Integer getPort() {
        try {
            return Integer.valueOf(pb.environment().getOrDefault("PORT", "9000"));
        } catch (Exception e) {
            return 9000;
        }
    }
    
    public static String getJdbcURL() {
        return pb.environment().getOrDefault("SPRING_DB_URL", "jdbc:postgresql://localhost:5432/gamesserver_dev");
    }

    public static String getDbUser() {
        return pb.environment().getOrDefault("SPRING_DB_USERNAME", "gamesserver");
    }

    public static String getDbPassword() {
        return pb.environment().getOrDefault("SPRING_DB_PASSWORD", "gamesserverpwd");
    }

    public static String getNotificationUrl(String message) throws UnsupportedEncodingException {
        String key = pb.environment().get("TELEGRAM_KEY");
        String chatId = pb.environment().get("TELEGRAM_CHAT_ID");
        return String.format("https://api.telegram.org/bot%s/sendMessage?chat_id=%s&disable_web_page_preview=1&text=%s", key, chatId, URLEncoder.encode(message, "UTF-8"));
    }
}
