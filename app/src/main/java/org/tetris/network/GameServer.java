package org.tetris.network;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Enumeration;

import org.tetris.game.model.GameMode;
import org.tetris.network.comand.Command;
import org.tetris.network.comand.DisconnectCommand;
import org.tetris.network.comand.GameResultCommand;
import org.tetris.network.comand.GameStartCommand;
import org.tetris.network.comand.PlayerConnectionCommand;
import org.tetris.network.comand.ReadyCommand;
import org.tetris.network.dto.MatchSettings;

/**
 * 게임 서버의 메인 클래스 (싱글톤 패턴). P2P 대전 모드에서 호스트 역할을 수행하며, 연결된 클라이언트를 관리합니다.
 */
public class GameServer {
    public static final int PORT = 12345; // 서버 포트

    private static GameServer instance;
    private ServerThread client1;
    private ServerThread client2;
    private ServerSocket serverSocket;
    private Thread serverThread;
    private volatile boolean running = false;

    // 호스트가 설정한 게임 모드 및 난이도
    private GameMode selectedGameMode = GameMode.NORMAL;
    private String selectedDifficulty = "EASY";

    // 게임 진행 상태 추적
    private volatile boolean gameInProgress = false;

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

    public InetAddress getHostIP() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface ni = interfaces.nextElement();
                if (!ni.isUp() || ni.isLoopback() || ni.isVirtual()) {
                    continue;
                }
                var addresses = ni.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    if (!(addr instanceof Inet4Address)) {
                        continue;
                    }
                    if (!addr.isLoopbackAddress() && addr.isSiteLocalAddress()) {
                        return addr; // 첫 번째 사설 IPv4 반환
                    }
                }
            }
            // 적절한 인터페이스를 못 찾으면 기본값으로 반환
            return InetAddress.getLocalHost();
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
                            client1Ready = false;
                            client2Ready = false;
                            notifyConnectionState();
                        } else if (client2 == null) {
                            System.out.println("[SERVER] Player 2 connected: "
                                    + clientSocket.getInetAddress());
                            client2 = new ServerThread(clientSocket);
                            client2.start();
                            client2Ready = false;
                            notifyConnectionState();

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

    /**
     * 호스트가 선택한 게임 모드를 설정합니다.
     * @param mode 게임 모드
     */
    public void setGameMode(GameMode mode) {
        this.selectedGameMode = mode;
    }

    /**
     * 호스트가 선택한 난이도를 설정합니다.
     * @param difficulty 난이도 문자열 (EASY, NORMAL, HARD)
     */
    public void setDifficulty(String difficulty) {
        this.selectedDifficulty = difficulty;
    }

    public void onClientReady(ServerThread client, boolean isReady) {
        if (client == client1) {
            client1Ready = isReady;
        } else if (client == client2) {
            client2Ready = isReady;
        }

        // Notify other client about readiness
        ReadyCommand readyCmd = new ReadyCommand(isReady);
        sendToOtherClient(client, readyCmd);

        // 게임 시작은 호스트가 명시적으로 호출
    }

    public synchronized boolean startGameIfReady() {
        if (client1 != null && client2 != null && client1Ready && client2Ready) {
            startGame();
            return true;
        }
        return false;
    }

    private synchronized void startGame() {
        System.out.println("[SERVER] Both players ready. Starting game...");

        long seed1 = System.currentTimeMillis();
        long seed2 = seed1 + 1000; // Different seed for player 2

        // 게임 진행 상태 설정
        gameInProgress = true;

        // Send GameStartCommand with (playerNumber, mySeed, otherSeed, gameMode, difficulty)
        // For Client 1: playerNumber=1, mySeed = seed1, otherSeed = seed2
        if (client1 != null) {
            client1.sendCommand(new GameStartCommand(new MatchSettings(1, seed1, seed2, selectedGameMode, selectedDifficulty)));
        }
        // For Client 2: playerNumber=2, mySeed = seed2, otherSeed = seed1
        if (client2 != null) {
            client2.sendCommand(new GameStartCommand(new MatchSettings(2, seed2, seed1, selectedGameMode, selectedDifficulty)));
        }

        // Reset readiness for next game?
        client1Ready = false;
        client2Ready = false;
    }

    /**
     * 게임을 재시작합니다. 새로운 seed를 생성하여 양쪽에 전송합니다.
     */
    public void restartGame() {
        if (client1 == null || client2 == null) {
            System.out.println("[SERVER] Cannot restart: not all players connected");
            return;
        }
        
        System.out.println("[SERVER] Restarting game with new seeds...");
        startGame();
    }

    /**
     * 서버를 중지합니다.
     */
    public void stop() {
        running = false;
        client1Ready = false;
        client2Ready = false;
        gameInProgress = false;

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
     * 게임 진행 중 연결이 끊기면 남은 플레이어에게 승리 처리합니다.
     */
    public synchronized void removeClient(ServerThread handler) {
        ServerThread remainingClient = null;
        String disconnectedPlayer = "";
        
        if (client1 == handler) {
            client1 = null;
            client1Ready = false;
            remainingClient = client2;
            disconnectedPlayer = "Player 1";
            System.out.println("[SERVER] Player 1 disconnected.");
        } else if (client2 == handler) {
            client2 = null;
            client2Ready = false;
            remainingClient = client1;
            disconnectedPlayer = "Player 2";
            System.out.println("[SERVER] Player 2 disconnected.");
        }

        // 게임 진행 중이었다면 남은 플레이어에게 승리 통보
        if (gameInProgress && remainingClient != null) {
            System.out.println("[SERVER] " + disconnectedPlayer + " left during game. Notifying winner...");
            
            // 연결 끊김 알림 전송
            DisconnectCommand disconnectCmd = new DisconnectCommand("상대방이 게임을 종료했습니다.");
            remainingClient.sendCommand(disconnectCmd);
            
            // 게임 결과 전송 (남은 플레이어 승리)
            GameResultCommand resultCmd = new GameResultCommand(true, 0);
            remainingClient.sendCommand(resultCmd);
            
            // 게임 종료 상태로 변경
            gameInProgress = false;
        }

        notifyConnectionState();
    }

    /**
     * 특정 클라이언트를 제외한 다른 클라이언트에게 커맨드를 전송합니다. (Relay)
     */
    public synchronized void sendToOtherClient(ServerThread sender, Command command) {
        System.out.println("[GAME-SERVER] sendToOtherClient called with: " + command.getClass().getSimpleName());
        if (sender == client1 && client2 != null) {
            System.out.println("[GAME-SERVER] Relaying to client2");
            client2.sendCommand(command);
        } else if (sender == client2 && client1 != null) {
            System.out.println("[GAME-SERVER] Relaying to client1");
            client1.sendCommand(command);
        } else {
            System.out.println("[GAME-SERVER] Cannot relay - sender=" + sender + ", client1=" + client1 + ", client2=" + client2);
        }
    }

    /**
     * 해당 클라이언트가 호스트(client1)인지 확인합니다.
     */
    public synchronized boolean isHost(ServerThread client) {
        return client == client1;
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
        gameInProgress = false;
        client1Ready = false;
        client2Ready = false;

        // 포트가 완전히 해제될 시간을 줌
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private synchronized void notifyConnectionState() {
        boolean bothConnected = client1 != null && client2 != null;
        if (client1 != null) {
            client1.sendCommand(new PlayerConnectionCommand(bothConnected));
        }
        if (client2 != null) {
            client2.sendCommand(new PlayerConnectionCommand(bothConnected));
        }
    }

    /**
     * 게임 진행 상태를 반환합니다.
     */
    public boolean isGameInProgress() {
        return gameInProgress;
    }

    /**
     * 게임 종료를 처리합니다.
     */
    public void endGame() {
        gameInProgress = false;
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
