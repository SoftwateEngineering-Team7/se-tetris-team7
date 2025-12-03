package org.tetris.network;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import org.tetris.network.comand.*;

/**
 * 서버 측에서 개별 클라이언트와의 통신을 담당하는 Runnable 클래스.
 * 클라이언트 -> 서버를 처리하는 클래스
 * ClientHandler는 별도의 스레드에서 실행됩니다.
 */
public class ServerThread {
    private final Socket socket;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    private volatile boolean running = true;

    private final java.util.concurrent.BlockingQueue<Command> sendQueue = new java.util.concurrent.LinkedBlockingQueue<>();
    private Thread senderThread;
    private Thread receiverThread;

    public ServerThread(Socket socket) {
        this.socket = socket;
    }

    public java.net.InetAddress getClientIP() {
        return socket.getInetAddress();
    }

    public void start() {
        try {
            // 스트림을 한 번만 초기화합니다.
            this.oos = new ObjectOutputStream(socket.getOutputStream());
            this.ois = new ObjectInputStream(socket.getInputStream());

            // 전송 스레드 시작
            senderThread = new Thread(new CommandSender());
            senderThread.start();

            // 수신 스레드 시작
            receiverThread = new Thread(new CommandReceiver());
            receiverThread.start();

            System.out.println("[SERVER-THREAD] Handler started for client: " + socket.getInetAddress());

        } catch (IOException e) {
            System.out.println("[SERVER-THREAD] Error initializing streams: " + e.getMessage());
            close();
        }
    }

    /**
     * 이 클라이언트에게 커맨드를 전송합니다. (큐에 추가)
     * 
     * @param command 전송할 커맨드
     */
    public void sendCommand(Command command) {
        if (!sendQueue.offer(command)) {
            System.err.println(
                    "[SERVER-THREAD] Send queue full. Dropping command: " + command.getClass().getSimpleName());
        }
    }

    /**
     * 소켓과 스트림을 닫습니다.
     */
    public void close() {
        running = false;
        try {
            if (senderThread != null)
                senderThread.interrupt();
            if (receiverThread != null)
                receiverThread.interrupt();
            if (ois != null)
                ois.close();
            if (oos != null)
                oos.close();
            if (socket != null && !socket.isClosed())
                socket.close();
        } catch (IOException ex) {
            System.err.println("[SERVER-THREAD] Error closing resources: " + ex.getMessage());
        }
        System.out.println("[SERVER-THREAD] Connection closed for client: " + socket.getInetAddress());
    }

    /**
     * 큐에서 커맨드를 꺼내 전송하는 내부 스레드
     */
    private class CommandSender implements Runnable {
        @Override
        public void run() {
            try {
                while (running && !Thread.currentThread().isInterrupted()) {
                    Command command = sendQueue.take(); // 큐가 빌 때까지 대기 (Blocking)
                    synchronized (oos) {
                        oos.writeObject(command);
                        oos.flush();
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (IOException e) {
                System.err.println("[SERVER-THREAD] Error sending command: " + e.getMessage());
            }
        }
    }

    /**
     * 클라이언트로부터 커맨드를 수신하는 내부 스레드
     */
    private class CommandReceiver implements Runnable {
        @Override
        public void run() {
            try {
                while (running && !Thread.currentThread().isInterrupted()) {
                    // 클라이언트로부터 커맨드를 읽어옵니다.
                    Command command = (Command) ois.readObject();

                    // 서버 콘솔에 수신된 커맨드 정보를 출력합니다.
                    System.out.println("[SERVER-THREAD] Received command: " + command.getClass().getSimpleName());

                    if (command instanceof PingCommand) {
                        // Ping 요청에 대한 Pong 응답
                        PingCommand pingCmd = (PingCommand) command;
                        PongCommand pongCmd = new PongCommand(pingCmd.getTimestamp());
                        sendCommand(pongCmd);
                    } else if (command instanceof ReadyCommand) {
                        ReadyCommand readyCmd = (ReadyCommand) command;
                        GameServer.getInstance().onClientReady(ServerThread.this, readyCmd.getIsReady());
                    } else if (command instanceof GameOverCommand) {
                        // 게임 오버 처리 - 게임 종료 상태로 변경
                        GameServer.getInstance().endGame();
                        GameServer.getInstance().broadcast(command); // 모두에게 알림
                    } else if (command instanceof PauseCommand) {
                        // 일시정지 커맨드는 상대방에게 릴레이
                        PauseCommand pauseCmd = (PauseCommand) command;
                        System.out.println("[SERVER-THREAD] PauseCommand received: isPaused=" + pauseCmd.isPaused());
                        GameServer.getInstance().sendToOtherClient(ServerThread.this, command);
                    } else if (command instanceof DisconnectCommand) {
                        // 연결 끊김 알림 - 상대방에게 전달하고 게임 종료
                        GameServer.getInstance().sendToOtherClient(ServerThread.this, command);
                        GameServer.getInstance().endGame();
                    } else if (command instanceof RestartCommand) {
                        // 재시작 요청 - 호스트만 허용
                        if (GameServer.getInstance().isHost(ServerThread.this)) {
                            System.out.println("[SERVER-THREAD] Restart requested by host");
                            GameServer.getInstance().restartGame();
                        } else {
                            System.out.println("[SERVER-THREAD] Restart request ignored - only host can restart");
                        }
                    } else {
                        // 그 외의 커맨드(이동, 공격 등)는 상대방에게 릴레이
                        GameServer.getInstance().sendToOtherClient(ServerThread.this, command);
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                if (running) {
                    System.out.println("[SERVER-THREAD] Client connection lost: " + e.getMessage());
                }
            } finally {
                // 리소스를 정리합니다.
                close();
                GameServer.getInstance().removeClient(ServerThread.this);
            }
        }
    }
}
