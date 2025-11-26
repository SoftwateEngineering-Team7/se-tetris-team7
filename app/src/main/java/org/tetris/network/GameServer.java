package org.tetris.network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.tetris.network.comand.GameCommand;

/**
 * 게임 서버의 메인 클래스 (싱글톤 패턴).
 * P2P 대전 모드에서 호스트 역할을 수행하며, 연결된 클라이언트를 관리합니다.
 * 
 * TODO: UI 통합 - 서버 IP 주소를 화면에 표시
 * TODO: 1:1 대전 연결 제한 - 최대 2명의 클라이언트(호스트 + 게스트 1명)만 접속 가능하도록 제한
 * TODO: 연결 완료 알림 - 클라이언트 접속 시 UI에 알림 표시
 * TODO: 대기 상태 화면 - 클라이언트 접속 대기 중 상태 표시
 * 
 * TODO: 게임 모드 선택 - 서버(호스트)가 일반/아이템/시간제한 모드 선택
 * TODO: 모드 선택 동기화 - 선택한 모드를 클라이언트에게 전송
 * TODO: 준비 상태 관리 - 서버/클라이언트 각각의 '게임 시작' 버튼 상태 관리
 * TODO: 게임 시작 동기화 - 양측 모두 준비 완료 시 게임 시작
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
    private GameServer() {
    }

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
        if (client1 != null) {
            return client1.getClientIP();
        } else {
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
                            System.out.println("[SERVER] Player 1 connected: " + clientSocket.getInetAddress());
                            client1 = new ServerThread(clientSocket);
                            client1.start();
                        } else if (client2 == null) {
                            System.out.println("[SERVER] Player 2 connected: " + clientSocket.getInetAddress());
                            client2 = new ServerThread(clientSocket);
                            client2.start();
                            
                            // 두 명 접속 완료 -> 게임 시작
                            startGame();
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

    private void startGame() {
        System.out.println("[SERVER] Both players connected. Starting game...");
        
        GameCommand readyCommand = new org.tetris.network.comand.ReadyCommand(false);
        
        broadcast(readyCommand);
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
        if (client1 != null) client1.close();
        if (client2 != null) client2.close();
        
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
    public synchronized void sendToOtherClient(ServerThread sender, GameCommand command) {
        if (sender == client1 && client2 != null) {
            client2.sendCommand(command);
        } else if (sender == client2 && client1 != null) {
            client1.sendCommand(command);
        }
    }

    /**
     * 모든 클라이언트에게 커맨드를 전송합니다.
     */
    public synchronized void broadcast(GameCommand command) {
        if (client1 != null) client1.sendCommand(command);
        if (client2 != null) client2.sendCommand(command);
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
