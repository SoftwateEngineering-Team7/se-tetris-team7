package org.tetris.network.menu.model;

import org.tetris.shared.BaseModel;

public class NetworkMenu extends BaseModel{
    private static final int DEFAULT_PORT = 54321;

    private boolean isHost;
    private String ipAddress;
    private int port;

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

    public void setIsHost(boolean isHost){
        this.isHost = isHost;
    }

    public void setIpAddress(String ipAddress){
        this.ipAddress = ipAddress;
    }

    public void setPort(int port){
        this.port = port;
    }


    public void create()
    {
        // TODO: P2P Host 방 생성
        System.out.println("호스트 방 생성 - IP: " + ipAddress + ", 포트: " + port);
    }

    public void join()
    {
        // TODO: P2P Client 방 참여
        System.out.println("방 참여 - IP: " + ipAddress + ", 포트: " + port);
    }

    public void clear()
    {
        this.isHost = true;
        this.ipAddress = "";
        this.port = DEFAULT_PORT;
    }    
}
