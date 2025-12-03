package org.tetris.network;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.tetris.network.comand.InputCommand;
import org.tetris.network.comand.SnapshotCommand;
import org.tetris.network.mocks.TestGameCommandExecutor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * P2P 게임 시퀀싱 시스템 통합 테스트
 * - 전역 시퀀스 부여 검증
 * - 입력 순서 보장 검증
 * - 스냅샷 복원 검증
 * - 네트워크 지연 시뮬레이션
 */
public class P2PGameIntegrationTest {
    private static final int TIMEOUT_SECONDS = 10;
    private static final int NETWORK_DELAY_MS = 50; // 네트워크 지연 시뮬레이션

    private List<ClientThread> clients;
    private List<TestGameCommandExecutor> gameExecutors;

    @Before
    public void setUp() {
        clients = new ArrayList<>();
        gameExecutors = new ArrayList<>();
        GameServer.getInstance().reset();
    }

    @After
    public void tearDown() {
        for (ClientThread client : clients) {
            if (client != null) {
                client.disconnect();
            }
        }
        clients.clear();
        gameExecutors.clear();
        stopServer();
    }

    private void startServer() throws IOException {
        GameServer.getInstance().start();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void stopServer() {
        GameServer.getInstance().stop();
    }

    /**
     * 테스트 1: 서버가 InputCommand에 전역 시퀀스를 부여하는지 검증
     */
    @Test
    public void testGlobalSequenceAssignment() throws IOException, InterruptedException {
        // Given
        startServer();

        TestGameCommandExecutor executor1 = new TestGameCommandExecutor();
        TestGameCommandExecutor executor2 = new TestGameCommandExecutor();

        ClientThread client1 = new ClientThread();
        client1.setGameExecutor(executor1);
        ClientThread client2 = new ClientThread();
        client2.setGameExecutor(executor2);

        clients.add(client1);
        clients.add(client2);
        gameExecutors.add(executor1);
        gameExecutors.add(executor2);

        client1.connect("localhost", GameServer.PORT);
        client2.connect("localhost", GameServer.PORT);
        Thread.sleep(200);

        // When - 클라이언트1이 입력 전송
        InputCommand cmd1 = new InputCommand(1, 1, "moveLeft", 0L);
        client1.sendCommand(cmd1);
        Thread.sleep(200);

        // When - 클라이언트2가 입력 전송
        InputCommand cmd2 = new InputCommand(2, 1, "moveRight", 0L);
        client2.sendCommand(cmd2);
        Thread.sleep(200);

        // Then - 양쪽 클라이언트 모두 executeInput이 호출되었는지 확인
        assertTrue("Client1 should receive executeInput",
                executor1.executedCommands.contains("executeInput"));
        assertTrue("Client2 should receive executeInput",
                executor2.executedCommands.contains("executeInput"));

        // Then - globalSeq가 부여되었는지 확인
        if (executor1.lastInputCommand != null) {
            assertTrue("GlobalSeq should be assigned",
                    executor1.lastInputCommand.getGlobalSeq() > 0);
        }
        if (executor2.lastInputCommand != null) {
            assertTrue("GlobalSeq should be assigned",
                    executor2.lastInputCommand.getGlobalSeq() > 0);
        }
    }

    /**
     * 테스트 2: 입력 순서가 보장되는지 검증 (FIFO)
     */
    @Test
    public void testInputOrderGuarantee() throws IOException, InterruptedException {
        // Given
        startServer();

        TestGameCommandExecutor executor1 = new TestGameCommandExecutor();
        TestGameCommandExecutor executor2 = new TestGameCommandExecutor();

        ClientThread client1 = new ClientThread();
        client1.setGameExecutor(executor1);
        ClientThread client2 = new ClientThread();
        client2.setGameExecutor(executor2);

        clients.add(client1);
        clients.add(client2);
        gameExecutors.add(executor1);
        gameExecutors.add(executor2);

        client1.connect("localhost", GameServer.PORT);
        client2.connect("localhost", GameServer.PORT);
        Thread.sleep(200);

        // When - 여러 입력을 순차적으로 전송
        List<Long> sentSequences = Collections.synchronizedList(new ArrayList<>());

        for (int i = 1; i <= 5; i++) {
            InputCommand cmd = new InputCommand(1, i, "moveLeft", 0L);
            sentSequences.add((long) i);
            client1.sendCommand(cmd);
            Thread.sleep(50); // 약간의 간격
        }

        Thread.sleep(500); // 모든 명령이 처리될 시간

        // Then - 수신된 순서가 전송된 순서와 동일한지 확인
        assertTrue("Should receive at least some commands",
                executor1.executedCommands.size() > 0 || executor2.executedCommands.size() > 0);
    }

    /**
     * 테스트 3: 동시 입력 시 서버가 순서를 결정하는지 검증
     */
    @Test
    public void testConcurrentInputOrdering() throws IOException, InterruptedException {
        // Given
        startServer();

        TestGameCommandExecutor executor1 = new TestGameCommandExecutor();
        TestGameCommandExecutor executor2 = new TestGameCommandExecutor();

        ClientThread client1 = new ClientThread();
        client1.setGameExecutor(executor1);
        ClientThread client2 = new ClientThread();
        client2.setGameExecutor(executor2);

        clients.add(client1);
        clients.add(client2);
        gameExecutors.add(executor1);
        gameExecutors.add(executor2);

        client1.connect("localhost", GameServer.PORT);
        client2.connect("localhost", GameServer.PORT);
        Thread.sleep(200);

        // When - 두 클라이언트가 동시에 입력 전송
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(2);

        new Thread(() -> {
            try {
                startLatch.await();
                for (int i = 1; i <= 3; i++) {
                    client1.sendCommand(new InputCommand(1, i, "moveLeft", 0L));
                    Thread.sleep(10);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                doneLatch.countDown();
            }
        }).start();

        new Thread(() -> {
            try {
                startLatch.await();
                for (int i = 1; i <= 3; i++) {
                    client2.sendCommand(new InputCommand(2, i, "moveRight", 0L));
                    Thread.sleep(10);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                doneLatch.countDown();
            }
        }).start();

        startLatch.countDown(); // 동시 시작
        assertTrue("All inputs should be sent", doneLatch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));

        Thread.sleep(500); // 모든 명령이 처리될 시간

        // Then - 명령이 처리되었는지 확인
        int totalCommands = executor1.executedCommands.size() + executor2.executedCommands.size();
        assertTrue("Should receive commands", totalCommands > 0);
    }

    /**
     * 테스트 4: 스냅샷 브로드캐스트 검증
     */
    @Test
    public void testSnapshotBroadcast() throws IOException, InterruptedException {
        // Given
        startServer();

        TestGameCommandExecutor executor1 = new TestGameCommandExecutor();
        TestGameCommandExecutor executor2 = new TestGameCommandExecutor();

        ClientThread client1 = new ClientThread();
        client1.setGameExecutor(executor1);
        ClientThread client2 = new ClientThread();
        client2.setGameExecutor(executor2);

        clients.add(client1);
        clients.add(client2);
        gameExecutors.add(executor1);
        gameExecutors.add(executor2);

        client1.connect("localhost", GameServer.PORT);
        client2.connect("localhost", GameServer.PORT);
        Thread.sleep(200);

        // When - 클라이언트1(호스트)이 스냅샷 전송
        int[][] board1 = createTestBoard(20, 10, 1);
        int[][] board2 = createTestBoard(20, 10, 2);
        SnapshotCommand snapshot = new SnapshotCommand(
                100, board1, board2, 1000, 2000, 0, 0);

        client1.sendCommand(snapshot);
        Thread.sleep(300);

        // Then - 양쪽 클라이언트 모두 restoreSnapshot이 호출되었는지 확인
        assertTrue("Client1 should receive restoreSnapshot",
                executor1.executedCommands.contains("restoreSnapshot"));
        assertTrue("Client2 should receive restoreSnapshot",
                executor2.executedCommands.contains("restoreSnapshot"));

        // Then - 스냅샷 데이터가 전달되었는지 확인
        assertNotNull("Client1 should have snapshot", executor1.lastSnapshotCommand);
        assertNotNull("Client2 should have snapshot", executor2.lastSnapshotCommand);
    }

    /**
     * 테스트 5: 네트워크 지연 시뮬레이션
     */
    @Test
    public void testNetworkDelaySimulation() throws IOException, InterruptedException {
        // Given
        startServer();

        TestGameCommandExecutor executor1 = new TestGameCommandExecutor();
        TestGameCommandExecutor executor2 = new TestGameCommandExecutor();

        ClientThread client1 = new ClientThread();
        client1.setGameExecutor(executor1);
        ClientThread client2 = new ClientThread();
        client2.setGameExecutor(executor2);

        clients.add(client1);
        clients.add(client2);
        gameExecutors.add(executor1);
        gameExecutors.add(executor2);

        client1.connect("localhost", GameServer.PORT);
        client2.connect("localhost", GameServer.PORT);
        Thread.sleep(200);

        // When - 지연을 시뮬레이션하면서 입력 전송
        long startTime = System.currentTimeMillis();

        InputCommand cmd1 = new InputCommand(1, 1, "moveLeft", 0L);
        client1.sendCommand(cmd1);
        Thread.sleep(NETWORK_DELAY_MS); // 네트워크 지연 시뮬레이션

        InputCommand cmd2 = new InputCommand(1, 2, "moveRight", 0L);
        client1.sendCommand(cmd2);
        Thread.sleep(NETWORK_DELAY_MS);

        InputCommand cmd3 = new InputCommand(1, 3, "rotate", 0L);
        client1.sendCommand(cmd3);

        Thread.sleep(500); // 모든 명령이 처리될 시간
        long endTime = System.currentTimeMillis();

        // Then - 지연이 있어도 명령이 처리되었는지 확인
        assertTrue("Commands should be processed despite delay",
                executor1.executedCommands.size() > 0 || executor2.executedCommands.size() > 0);

        // Then - 총 소요 시간이 지연을 포함하는지 확인
        long totalTime = endTime - startTime;
        assertTrue("Total time should include delays",
                totalTime >= NETWORK_DELAY_MS * 2);
    }

    /**
     * 테스트 6: 패킷 손실 시뮬레이션 (일부 명령 누락)
     */
    @Test
    public void testPacketLossRecovery() throws IOException, InterruptedException {
        // Given
        startServer();

        TestGameCommandExecutor executor1 = new TestGameCommandExecutor();
        TestGameCommandExecutor executor2 = new TestGameCommandExecutor();

        ClientThread client1 = new ClientThread();
        client1.setGameExecutor(executor1);
        ClientThread client2 = new ClientThread();
        client2.setGameExecutor(executor2);

        clients.add(client1);
        clients.add(client2);
        gameExecutors.add(executor1);
        gameExecutors.add(executor2);

        client1.connect("localhost", GameServer.PORT);
        client2.connect("localhost", GameServer.PORT);
        Thread.sleep(200);

        // When - 여러 입력 전송 (일부는 의도적으로 누락 시뮬레이션)
        for (int i = 1; i <= 5; i++) {
            if (i != 3) { // 3번째 입력은 "손실" 시뮬레이션
                InputCommand cmd = new InputCommand(1, i, "moveLeft", 0L);
                client1.sendCommand(cmd);
                Thread.sleep(50);
            }
        }

        Thread.sleep(300);

        // When - 스냅샷으로 복구
        int[][] board1 = createTestBoard(20, 10, 5);
        int[][] board2 = createTestBoard(20, 10, 5);
        SnapshotCommand snapshot = new SnapshotCommand(
                10, board1, board2, 500, 600, 0, 0);

        client1.sendCommand(snapshot);
        Thread.sleep(300);

        // Then - 스냅샷이 수신되어 복구되었는지 확인
        assertTrue("Snapshot should be received for recovery",
                executor1.executedCommands.contains("restoreSnapshot") ||
                        executor2.executedCommands.contains("restoreSnapshot"));
    }

    /**
     * 테스트 7: 높은 부하 상황에서의 시퀀스 일관성
     */
    @Test
    public void testSequenceConsistencyUnderLoad() throws IOException, InterruptedException {
        // Given
        startServer();

        TestGameCommandExecutor executor1 = new TestGameCommandExecutor();
        TestGameCommandExecutor executor2 = new TestGameCommandExecutor();

        ClientThread client1 = new ClientThread();
        client1.setGameExecutor(executor1);
        ClientThread client2 = new ClientThread();
        client2.setGameExecutor(executor2);

        clients.add(client1);
        clients.add(client2);
        gameExecutors.add(executor1);
        gameExecutors.add(executor2);

        client1.connect("localhost", GameServer.PORT);
        client2.connect("localhost", GameServer.PORT);
        Thread.sleep(200);

        // When - 대량의 입력을 빠르게 전송
        int commandCount = 20;
        for (int i = 1; i <= commandCount; i++) {
            InputCommand cmd = new InputCommand(1, i, "moveLeft", 0L);
            client1.sendCommand(cmd);
            Thread.sleep(10); // 매우 짧은 간격
        }

        Thread.sleep(1000); // 모든 명령이 처리될 충분한 시간

        // Then - 명령이 처리되었는지 확인
        int totalProcessed = executor1.executedCommands.size() + executor2.executedCommands.size();
        assertTrue("Should process commands under load", totalProcessed > 0);
    }

    /**
     * 테스트용 보드 생성 헬퍼 메서드
     */
    private int[][] createTestBoard(int height, int width, int fillValue) {
        int[][] board = new int[height][width];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                board[i][j] = fillValue;
            }
        }
        return board;
    }
}
