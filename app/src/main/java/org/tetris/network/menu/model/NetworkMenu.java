package org.tetris.network.menu.model;

import java.io.IOException;

import org.tetris.network.ClientThread;
import org.tetris.network.comand.*;
import org.tetris.network.GameServer;
import org.tetris.network.game.GameEngine;
import org.tetris.shared.BaseModel;

public class NetworkMenu extends BaseModel{
    private static final int DEFAULT_PORT = 54321;
    private static final int SERVER_STARTUP_DELAY_MS = 100;

    private boolean isHost;
    private String ipAddress;
    private int port;
    private boolean isReady;

    private GameEngine engine;
    private ClientThread client;

    public NetworkMenu(){
        clear();
    }

    public boolean getIsHost(){
        return isHost;
    }

    public String getIpAddress(){
        return ipAddress;
    }

    public int getPort(){
        return port;
    }

    public boolean isReady(){
        return isReady;
    }

    public void setIsHost(boolean isHost){
        this.isHost = isHost;
    }

    public void setIpAddress(String ipString){
        if (isValidIP(ipString)){
            this.ipAddress = ipString;
        }            
    }

    public void setPort(int port){
        if (isValidPort(port)){
            this.port = port;
        }
    }

    public void setIsReady(boolean isReady){
        this.isReady = isReady;
        engine.setThisReady(isReady);

        if (client != null) {
            client.sendCommand(new ReadyCommand(isReady));
        }
    }

    public boolean isValidIP(String ipString)
    {
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

    public boolean isValidPort(int port)
    {
        if (port < 1 || port > 65535) {
            throw new IllegalArgumentException("포트는 1-65535 범위여야 합니다");
        }
        return true;
    }


    public void create()
    {
        if (client != null) {
            client.disconnect();
        }

        try {
            startServer();
        } catch (IOException e) {
            throw new RuntimeException("서버 시작에 실패했습니다", e);
        }
        
        engine = new GameEngine();
        client = new ClientThread(engine);

        try {
            client.connect("localhost", port);
            this.ipAddress = GameServer.getInstance().getHostIP().getHostAddress();
        } catch (IOException e) {
            throw new RuntimeException("클라이언트 연결에 실패했습니다", e);
        }

        System.out.println("호스트 방 생성 - IP: " + ipAddress + ", 포트: " + port);
    }

    private void startServer() throws IOException {
        GameServer.getInstance().start(port);
        
        try {
            Thread.sleep(SERVER_STARTUP_DELAY_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void join()
    {
        engine = new GameEngine();
        client = new ClientThread(engine);

        try {
            client.connect(ipAddress, port);
        } catch (IOException e) {
            throw new RuntimeException("서버 연결에 실패했습니다: " + ipAddress + ":" + port, e);
        }

        System.out.println("방 참여 성공 - IP: " + ipAddress + ", 포트: " + port);
    }

    public void clear()
    {
        if (client != null) {
            client.disconnect();
            client = null;
        }

        this.isHost = true;
        this.ipAddress = "";
        this.port = DEFAULT_PORT;
        this.isReady = false;
        this.engine = new GameEngine();
    }    
}
