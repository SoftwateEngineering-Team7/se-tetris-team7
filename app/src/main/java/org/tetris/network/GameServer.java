package org.tetris.network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.tetris.network.comand.Command;

import org.tetris.network.comand.GameStartCommand;
import org.tetris.network.comand.ReadyCommand;
import org.tetris.network.dto.MatchSettings;

/**
 * 게임 서버의 메인 클래스 (싱글톤 패턴). P2P 대전 모드에서 호스트 역할을 수행하며, 연결된 클라이언트를 관리합니다.
 * 
 * TODO: 모드 선택 동기화 - 선택한 모드를 클라이언트에게 전송
 */
public class GameServer {
    public static final int PORT = 12345; // 서버 포트

    private static GameServer instance;
    private ServerThread client1;
    private ServerThread client2;
    private ServerSocket serverSocket;
    private Thread serverThread;
    private volatile boolean running = false;

    /**
     * Private 생성자 (싱글톤 패턴)
     */
    private GameServer() {}

    /**
     * GameServer 싱글톤 인스턴스를 반환합니다.
     */
    public static synchronized GameServer getInstance() {
        if (instance == null) {
            instance = new GameServer();
        }
        return instance;
    }

    public java.net.InetAddress getHostIP() {
        try {
            return java.net.InetAddress.getLocalHost();
        } catch (Exception e) {
            return null;
        }
    }

    public void start() throws IOException {
        start(PORT);
    }

    /**
     * 서버를 시작합니다.
     */
    public void start(int port) throws IOException {
        if (running) {
            System.out.println("[SERVER] Server is already running.");
            return;
        }

        serverSocket = new ServerSocket(port);
        running = true;

        System.out.println("[SERVER] Game Server is running on port " + port);
        System.out.println("[SERVER] Waiting for 2 players...");

        serverThread = new Thread(() -> {
            try {
                while (running && !Thread.currentThread().isInterrupted()) {
                    Socket clientSocket = serverSocket.accept();

                    synchronized (this) {
                        if (client1 == null) {
                            System.out.println("[SERVER] Player 1 connected: "
                                    + clientSocket.getInetAddress());
                            client1 = new ServerThread(clientSocket);
                            client1.start();
                        } else if (client2 == null) {
                            System.out.println("[SERVER] Player 2 connected: "
                                    + clientSocket.getInetAddress());
                            client2 = new ServerThread(clientSocket);
                            client2.start();

                        } else {
                            System.out.println("[SERVER] Connection rejected: Server is full.");
                            clientSocket.close();
                        }
                    }
                }
            } catch (IOException e) {
                if (running) {
                    System.err.println("[SERVER] Server exception: " + e.getMessage());
                }
            }
        });
        serverThread.start();
    }

    private boolean client1Ready = false;
    private boolean client2Ready = false;

    public void onClientReady(ServerThread client, boolean isReady) {
        if (client == client1) {
            client1Ready = isReady;
        } else if (client == client2) {
            client2Ready = isReady;
        }

        // Notify other client about readiness
        ReadyCommand readyCmd = new ReadyCommand(isReady);
        sendToOtherClient(client, readyCmd);

        // Check if both are ready
        if (client1Ready && client2Ready) {
            startGame();
        }
    }

    private void startGame() {
        System.out.println("[SERVER] Both players ready. Starting game...");

        long seed1 = System.currentTimeMillis();
        long seed2 = seed1 + 1000; // Different seed for player 2

        // Send GameStartCommand with (playerNumber, mySeed, otherSeed)
        // For Client 1: playerNumber=1, mySeed = seed1, otherSeed = seed2
        if (client1 != null) {
            client1.sendCommand(new GameStartCommand(new MatchSettings(1, seed1, seed2)));
        }
        // For Client 2: playerNumber=2, mySeed = seed2, otherSeed = seed1
        if (client2 != null) {
            client2.sendCommand(new GameStartCommand(new MatchSettings(2, seed2, seed1)));
        }

        // Reset readiness for next game?
        client1Ready = false;
        client2Ready = false;
    }

    /**
     * 서버를 중지합니다.
     */
    public void stop() {
        running = false;

        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            System.err.println("[SERVER] Error closing server socket: " + e.getMessage());
        }

        if (serverThread != null) {
            serverThread.interrupt();
        }

        // 클라이언트 연결 종료
        if (client1 != null)
            client1.close();
        if (client2 != null)
            client2.close();

        client1 = null;
        client2 = null;

        System.out.println("[SERVER] Server stopped.");
    }

    /**
     * 클라이언트 핸들러를 제거합니다.
     */
    public synchronized void removeClient(ServerThread handler) {
        if (client1 == handler) {
            client1 = null;
            System.out.println("[SERVER] Player 1 disconnected.");
        } else if (client2 == handler) {
            client2 = null;
            System.out.println("[SERVER] Player 2 disconnected.");
        }

        // 한 명이 나가면 게임 종료 처리
        // TODO: 남은 플레이어에게 승리 메시지 전송 등
    }

    /**
     * 특정 클라이언트를 제외한 다른 클라이언트에게 커맨드를 전송합니다. (Relay)
     */
    public synchronized void sendToOtherClient(ServerThread sender, Command command) {
        if (sender == client1 && client2 != null) {
            client2.sendCommand(command);
        } else if (sender == client2 && client1 != null) {
            client1.sendCommand(command);
        }
    }

    /**
     * 모든 클라이언트에게 커맨드를 전송합니다.
     */
    public synchronized void broadcast(Command command) {
        if (client1 != null)
            client1.sendCommand(command);
        if (client2 != null)
            client2.sendCommand(command);
    }

    /**
     * 테스트를 위한 리셋 메서드
     */
    public void reset() {
        stop();
        client1 = null;
        client2 = null;
        serverSocket = null;
        serverThread = null;

        // 포트가 완전히 해제될 시간을 줌
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static void main(String[] args) {
        try {
            GameServer.getInstance().start();
        } catch (IOException e) {
            System.err.println("[SERVER] Failed to start server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
