package org.tetris.network;

import java.io.IOException;
import org.tetris.network.comand.Command;
import org.tetris.network.comand.GameCommandExecutor;
import org.tetris.network.comand.GameMenuCommand;
import org.tetris.network.comand.GameMenuCommandExecutor;

/**
 * 네트워크 모듈을 사용하는 클라이언트 예제 클래스. P2P 대전 모드에서 클라이언트 역할을 수행합니다.
 */
public class GameClient {
    private static GameClient instance;
    private ClientThread clientThread;

    public static synchronized GameClient getInstance() {
        if (instance == null) {
            instance = new GameClient();
            instance.clientThread = new ClientThread();
        }
        return instance;
    }

    public void setMenuExecutor(GameMenuCommandExecutor menuCommandExecutor) {
        clientThread.setMenuExecutor(menuCommandExecutor);
    }

    public void setGameExecutor(GameCommandExecutor gameCommandExecutor) {
        clientThread.setGameExecutor(gameCommandExecutor);
    }

    /**
     * 연결 끊김 콜백을 설정합니다.
     * @param callback 연결이 끊어질 때 호출될 콜백
     */
    public void setOnDisconnectCallback(Runnable callback) {
        clientThread.setOnDisconnectCallback(callback);
    }

    public void connect(String ip, int port) {
        try {
            clientThread.connect(ip, port);
        } catch (IOException e) {
            throw new RuntimeException("서버 연결에 실패했습니다: " + ip + ":" + port, e);
        }
    }

    public void disconnect() {
        clientThread.disconnect();
    }

    /**
     * 서버에 연결 끊김을 알리고 정상적으로 연결을 종료합니다.
     */
    public void disconnectGracefully() {
        clientThread.disconnectGracefully();
    }

    /**
     * 연결 상태를 반환합니다.
     */
    public boolean isConnected() {
        return clientThread.isConnected();
    }

    public void sendCommand(Command command) {
        clientThread.sendCommand(command);
    }
    
    /**
     * gameExecutor가 설정되어 있는지 확인합니다.
     * 게임이 이미 진행 중인지 판단하는 데 사용됩니다.
     */
    public boolean hasGameExecutor() {
        return clientThread.hasGameExecutor();
    }
    
    /**
     * 클라이언트 인스턴스를 리셋합니다. (테스트용)
     */
    public static synchronized void reset() {
        if (instance != null) {
            instance.disconnect();
            instance.clientThread = new ClientThread();
        }
    }
}
