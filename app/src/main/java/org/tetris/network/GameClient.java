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

    public static GameClient getInstance() {
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

    public void sendCommand(Command command) {
        clientThread.sendCommand(command);
    }
}
