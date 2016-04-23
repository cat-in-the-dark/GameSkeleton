package com.catinthedark.server;

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
}
