package org.tetris.network.menu.model;

import java.io.IOException;

import org.tetris.network.GameClient;
import org.tetris.network.comand.*;
import org.tetris.network.GameServer;
import org.tetris.shared.BaseModel;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleLongProperty;

public class NetworkMenu extends BaseModel {
    private static final int DEFAULT_PORT = 54321;
    private static final int SERVER_STARTUP_DELAY_MS = 100;

    private boolean isHost;
    private String ipAddress;
    private int port;
    private boolean isReady;
    private boolean serverStarted;

    private GameClient client;
    private LongProperty ping = new SimpleLongProperty();
    private BooleanProperty otherIsReady = new SimpleBooleanProperty();
    private BooleanProperty connected = new SimpleBooleanProperty(false);
    private BooleanProperty opponentConnected = new SimpleBooleanProperty(false);

    public NetworkMenu() {
        clear();
        this.client = GameClient.getInstance();
    }

    public boolean getIsHost() {
        return isHost;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public int getPort() {
        return port;
    }

    public boolean isReady() {
        return isReady;
    }

    public boolean isConnected() {
        return connected.get();
    }

    public boolean isOpponentConnected() {
        return opponentConnected.get();
    }

    public void setIsHost(boolean isHost) {
        this.isHost = isHost;
    }

    public void setIpAddress(String ipString) {
        if (isValidIP(ipString)) {
            this.ipAddress = ipString;
        }
    }

    public void setPort(int port) {
        if (isValidPort(port)) {
            this.port = port;
        }
    }

    public void setIsReady(boolean isReady) {
        this.isReady = isReady;
    }

    public void setConnected(boolean isConnected) {
        javafx.application.Platform.runLater(() -> connected.set(isConnected));
    }

    public void setOpponentConnected(boolean isOpponentConnected) {
        javafx.application.Platform.runLater(() -> opponentConnected.set(isOpponentConnected));
    }

    public boolean isValidIP(String ipString) {
        if (isHost) {
            return true; // 호스트 모드에서는 IP 검증이 필요 없음
        }

        if (ipString == null || ipString.isBlank()) {
            throw new IllegalArgumentException("IP 주소는 비어 있을 수 없습니다");
        }
        try {
            java.net.InetAddress.getByName(ipString);
        } catch (Exception e) {
            throw new IllegalArgumentException("유효하지 않은 IP 주소입니다: " + ipString, e);
        }
        return true;
    }

    public boolean isValidPort(int port) {
        if (port < 1 || port > 65535) {
            throw new IllegalArgumentException("포트는 1-65535 범위여야 합니다");
        }
        return true;
    }

    public void create() {
        clearConnections();
        if (client != null) {
            client.disconnect();
        }

        try {
            startServer();
        } catch (IOException e) {
            throw new RuntimeException("서버 시작에 실패했습니다", e);
        }

        if (client != null) {
            client.connect("localhost", port);
            java.net.InetAddress hostIP = GameServer.getInstance().getHostIP();
            this.ipAddress = (hostIP != null) ? hostIP.getHostAddress() : "localhost";
            setConnected(true);
            setOpponentConnected(false);
            resetReadyStates();
        } else {
            throw new IllegalStateException("GameClient 인스턴스가 null입니다. 서버에 연결할 수 없습니다.");
        }

        System.out.println("호스트 방 생성 - IP: " + ipAddress + ", 포트: " + port);
    }

    private void startServer() throws IOException {
        GameServer.getInstance().reset();
        GameServer.getInstance().start(port);
        serverStarted = true;

        try {
            Thread.sleep(SERVER_STARTUP_DELAY_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void join() {
        clearConnections();
        client.connect(ipAddress, port);
        setConnected(true);
        setOpponentConnected(true); // 호스트는 이미 연결되어 있음
        resetReadyStates();
        System.out.println("방 참여 성공 - IP: " + ipAddress + ", 포트: " + port);
    }

    public void clear() {
        clearConnections();
        this.isHost = true;
        this.ipAddress = "";
        this.port = DEFAULT_PORT;
        this.isReady = false;
        this.ping.set(0);
    }

    public LongProperty pingProperty() {
        return ping;
    }

    public long getPing() {
        return ping.get();
    }

    public void setPing(long ping) {
        javafx.application.Platform.runLater(() -> {
            this.ping.set(ping);
        });
    }

    public BooleanProperty otherIsReadyProperty() {
        return otherIsReady;
    }

    public boolean getOtherIsReady() {
        return otherIsReady.get();
    }

    public void setOtherIsReady(boolean isReady) {
        this.otherIsReady.set(isReady);
    }

    public BooleanProperty connectedProperty() {
        return connected;
    }

    public BooleanProperty opponentConnectedProperty() {
        return opponentConnected;
    }

    public void resetReadyStates() {
        setIsReady(false);
        setOtherIsReady(false);
    }

    private void clearConnections() {
        if (client != null) {
            client.disconnect();
        }
        if (serverStarted) {
            GameServer.getInstance().stop();
            serverStarted = false;
        }
        setConnected(false);
        setOpponentConnected(false);
        resetReadyStates();
    }
}
