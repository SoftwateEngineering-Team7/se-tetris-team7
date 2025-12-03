package org.tetris.network;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import org.tetris.network.comand.Command;
import org.tetris.network.comand.DisconnectCommand;
import org.tetris.network.comand.GameCommand;
import org.tetris.network.comand.GameCommandExecutor;
import org.tetris.network.comand.GameMenuCommand;
import org.tetris.network.comand.GameMenuCommandExecutor;
import org.tetris.network.comand.PingCommand;

/**
 * 클라이언트가 서버와 통신하기 위한 핸들러 클래스. 서버 -> 클라이언트를 처리하는 클래스 소켓 연결, 데이터 송수신, 비동기 수신 스레드 관리를 캡슐화하여
 * ServerHandler는 별도의 스레드에서 실행됩니다. 클라이언트의 다른 부분에서는 사용하기 쉬운 API만 노출합니다.
 */
public class ClientThread {
    private Socket socket;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    private GameCommandExecutor gameExecutor;
    private GameMenuCommandExecutor menuExecutor;
    private volatile boolean connected = false;
    private Thread receiverThread;
    private Thread pingThread;

    // 타임아웃 설정
    private static final long READ_TIMEOUT_MS = 10000; // 10초
    private volatile long lastResponseTime = System.currentTimeMillis();

    // 연결 끊김 콜백
    private Runnable onDisconnectCallback;

    public ClientThread() {
        // Executors will be set later
    }

    public void setGameExecutor(GameCommandExecutor gameExecutor) {
        this.gameExecutor = gameExecutor;
    }

    public void setMenuExecutor(GameMenuCommandExecutor menuExecutor) {
        this.menuExecutor = menuExecutor;
    }

    /**
     * gameExecutor가 설정되어 있는지 확인합니다.
     */
    public boolean hasGameExecutor() {
        return this.gameExecutor != null;
    }

    /**
     * 연결 끊김 콜백을 설정합니다.
     * @param callback 연결이 끊어질 때 호출될 콜백
     */
    public void setOnDisconnectCallback(Runnable callback) {
        this.onDisconnectCallback = callback;
    }

    /**
     * 서버에 연결하고 비동기 커맨드 수신을 시작합니다.
     * 
     * @param host 서버 호스트 주소
     * @param port 서버 포트 번호
     */
    public void connect(String host, int port) throws IOException, UnknownHostException {
        if (connected) {
            System.out.println("[CLIENT-FACADE] Already connected.");
            return;
        }
        // TODO: 연결 타임아웃 설정 (socket.connect(new InetSocketAddress(host, port), timeout))
        socket = new Socket(host, port);
        oos = new ObjectOutputStream(socket.getOutputStream());
        ois = new ObjectInputStream(socket.getInputStream());
        connected = true;

        // 서버로부터 커맨드를 수신하는 별도의 스레드를 시작합니다.
        receiverThread = new Thread(new CommandReceiver());
        receiverThread.start();

        // Ping을 주기적으로 전송하는 스레드를 시작합니다.
        pingThread = new Thread(new PingSender());
        pingThread.start();

        System.out.println("[CLIENT-FACADE] Connected to server at " + host + ":" + port);
    }

    /**
     * 서버로 GameCommand 객체를 전송합니다.
     * 
     * @param command 전송할 커맨드
     */
    public void sendCommand(Command command) {
        if (!connected) {
            System.err.println("[CLIENT-FACADE] Not connected. Cannot send command.");
            return;
        }
        try {
            synchronized (oos) {
                oos.writeObject(command);
                oos.flush();
            }
        } catch (IOException e) {
            System.err.println("[CLIENT-FACADE] Error sending command: " + e.getMessage());
        }
    }

    /**
     * 서버와의 연결을 종료합니다.
     */
    public void disconnect() {
        if (!connected) {
            return;
        }
        connected = false;
        try {
            if (oos != null)
                oos.close();
            if (ois != null)
                ois.close();
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            if (receiverThread != null) {
                receiverThread.interrupt();
            }
            if (pingThread != null) {
                pingThread.interrupt();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("[CLIENT-FACADE] Disconnected.");
    }

    /**
     * 연결 상태를 반환합니다.
     */
    public boolean isConnected() {
        return connected;
    }

    /**
     * 서버에 연결 끊김을 알리고 연결을 종료합니다.
     */
    public void disconnectGracefully() {
        if (connected) {
            // 서버에 연결 끊김 알림 전송
            sendCommand(new DisconnectCommand("플레이어가 게임을 종료했습니다."));
            // 짧은 지연 후 연결 종료 (메시지 전송 완료 대기)
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            disconnect();
        }
    }

    /**
     * 주기적으로 Ping을 전송하는 내부 Runnable 클래스.
     */
    private class PingSender implements Runnable {
        private static final long PING_INTERVAL = 2000; // Ping 전송 간격

        public void run() {
            try {
                // 첫 ping은 즉시 전송
                if (connected) {
                    sendCommand(new PingCommand());
                }

                while (connected && !Thread.currentThread().isInterrupted()) {
                    Thread.sleep(PING_INTERVAL);
                    
                    // 타임아웃 체크
                    long currentTime = System.currentTimeMillis();
                    if (currentTime - lastResponseTime > READ_TIMEOUT_MS) {
                        System.err.println("[CLIENT-THREAD] Connection timed out. No response for " + (currentTime - lastResponseTime) + "ms");
                        
                        // 연결 끊김 시 gameExecutor에 알림
                        if (gameExecutor != null) {
                            gameExecutor.onOpponentDisconnect("연결 시간 초과 (Timeout)");
                        }
                        
                        disconnectGracefully(); 
                        break;
                    }

                    if (connected) {
                        sendCommand(new PingCommand());
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * 서버로부터 GameCommand를 비동기적으로 수신하는 내부 Runnable 클래스.
     */
    private class CommandReceiver implements Runnable {
        @Override
        public void run() {
            try {
                while (connected && !Thread.currentThread().isInterrupted()) {
                    // 서버로부터 커맨드를 수신 대기합니다. (Blocking call)
                    Command command = (Command) ois.readObject();
                    
                    // 데이터 수신 시 마지막 응답 시간 갱신
                    lastResponseTime = System.currentTimeMillis();

                    System.out.println("[CLIENT-RECEIVER] Received command from server: "
                            + command.getClass().getSimpleName());
                    // 수신된 커맨드를 로컬 게임 엔진에서 실행합니다.
                    if (command instanceof GameMenuCommand) {
                        if (menuExecutor != null) {
                            ((GameMenuCommand) command).execute(menuExecutor);
                        }
                    }
                    if (command instanceof GameCommand) {
                        if (gameExecutor != null) {
                            System.out.println("[CLIENT-RECEIVER] Executing GameCommand on gameExecutor");
                            ((GameCommand) command).execute(gameExecutor);
                        } else {
                            System.out.println("[CLIENT-RECEIVER] WARNING: gameExecutor is null, cannot execute command");
                        }
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                if (connected) {
                    System.err.println("[CLIENT-RECEIVER] Connection lost: " + e.getMessage());
                    // 연결 끊김 시 gameExecutor에 알림
                    if (gameExecutor != null) {
                        gameExecutor.onOpponentDisconnect("네트워크 연결이 끊어졌습니다.");
                    }
                    // 콜백 호출
                    if (onDisconnectCallback != null) {
                        javafx.application.Platform.runLater(onDisconnectCallback);
                    }
                }
            } finally {
                disconnect();
            }
        }
    }
}
