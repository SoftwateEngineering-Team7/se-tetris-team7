package org.tetris.network;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import org.tetris.network.comand.Command;
import org.tetris.network.comand.GameCommand;
import org.tetris.network.comand.GameCommandExecutor;
import org.tetris.network.comand.GameMenuCommand;
import org.tetris.network.comand.GameMenuCommandExecutor;
import org.tetris.network.comand.PingCommand;

/**
 * 클라이언트가 서버와 통신하기 위한 핸들러 클래스.
 * 서버 -> 클라이언트를 처리하는 클래스
 * 소켓 연결, 데이터 송수신, 비동기 수신 스레드 관리를 캡슐화하여
 * ServerHandler는 별도의 스레드에서 실행됩니다.
 * 클라이언트의 다른 부분에서는 사용하기 쉬운 API만 노출합니다.
 * 
 * TODO: 네트워크 지연 감지 - 전송 지연 시간 측정 및 "랙 걸린 상태" 표시
 * TODO: 연결 타임아웃 관리 - 일정 시간 이상 응답 없으면 연결 끊김으로 판단
 * TODO: 연결 끊김 처리 - 에러 메시지 표시 후 P2P 대전 모드 초기 화면으로 복귀
 * TODO: Heartbeat/Ping 메커니즘 - 주기적으로 연결 상태 확인
 * TODO: 재연결 시도 - 일시적 끊김 시 자동 재연결 시도 (exponential backoff)
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
        connected = false;
        try {
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
                    System.out.println(
                            "[CLIENT-RECEIVER] Received command from server: " + command.getClass().getSimpleName());
                    // 수신된 커맨드를 로컬 게임 엔진에서 실행합니다.
                    if (command instanceof GameMenuCommand && command instanceof GameCommand) {
                        if (menuExecutor != null) {
                            ((GameMenuCommand) command).execute(menuExecutor);
                        }
                        if (gameExecutor != null) {
                            ((GameCommand) command).execute(gameExecutor);
                        }
                    } else if (command instanceof GameMenuCommand) {
                        if (menuExecutor != null) {
                            ((GameMenuCommand) command).execute(menuExecutor);
                        }
                    } else if (command instanceof GameCommand) {
                        if (gameExecutor != null) {
                            ((GameCommand) command).execute(gameExecutor);
                        }
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                if (connected) {
                    System.err.println("[CLIENT-RECEIVER] Connection lost: " + e.getMessage());
                    // TODO: 자동 재연결 시도 (exponential backoff)
                    // TODO: 연결 끊김 콜백 구현
                }
            } finally {
                disconnect();
            }
        }
    }
}
