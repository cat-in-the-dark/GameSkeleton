package com.catinthedark.server.persist;

import java.util.Date;

public final class PlayerModel {
    private String ip;
    private String uuid;
    private String status;
    private Date disconnectedAt;
    private Date connectedAt;

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

    public Date getDisconnectedAt() {
        return disconnectedAt;
    }

    public void setDisconnectedAt(Date disconnectedAt) {
        this.disconnectedAt = disconnectedAt;
    }

    public Date getConnectedAt() {
        return connectedAt;
    }

    public void setConnectedAt(Date connectedAt) {
        this.connectedAt = connectedAt;
    }

    @Override
    public String toString() {
        return "PlayerModel{" +
                "ip='" + ip + '\'' +
                ", uuid='" + uuid + '\'' +
                ", status='" + status + '\'' +
                ", disconnectedAt=" + disconnectedAt +
                ", connectedAt=" + connectedAt +
                '}';
    }
}
