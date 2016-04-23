package com.catinthedark.server.persist;

public final class PlayerModel {
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
