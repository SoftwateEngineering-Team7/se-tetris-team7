package org.tetris.integration;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.tetris.network.GameServer;
import org.tetris.network.ClientThread;
import org.tetris.network.comand.*;
import org.tetris.network.dto.MatchSettings;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;

/**
 * P2P 게임 통합 테스트
 * 서버 1개와 클라이언트 2개를 시뮬레이션하여 전체 네트워크 흐름을 테스트합니다.
 * 
 * 이 테스트는 다음을 검증합니다:
 * 1. 서버-클라이언트 연결
 * 2. Ready 명령 전송 및 수신
 * 3. GameStartCommand 수신 및 MatchSettings 전달
 * 4. 플레이어 번호 할당 (Player 1, Player 2)
 * 5. 시드 교차 할당
 * 6. 게임 명령 릴레이 (Client 1 → Server → Client 2)
 */
public class P2PGameIntegrationTest {

    private GameServer server;
    private ClientThread client1Thread;
    private ClientThread client2Thread;
    private static final int TEST_PORT = 54322;
    private static final String TEST_HOST = "localhost";

    @Before
    public void setUp() throws Exception {
        server = GameServer.getInstance();
        server.reset();
        
        client1Thread = new ClientThread();
        client2Thread = new ClientThread();
    }

    @After
    public void tearDown() {
        if (client1Thread != null) {
            client1Thread.disconnect();
        }
        if (client2Thread != null) {
            client2Thread.disconnect();
        }
        if (server != null) {
            server.stop();
        }
    }

    @Test
    public void testP2PGameFullFlow() throws Exception {
        // Given: 서버 시작
        server.start(TEST_PORT);
        Thread.sleep(500);

        CountDownLatch client1ReadyLatch = new CountDownLatch(1);
        CountDownLatch client2ReadyLatch = new CountDownLatch(1);
        CountDownLatch gameStartLatch = new CountDownLatch(2);

        AtomicReference<MatchSettings> client1Settings = new AtomicReference<>();
        AtomicReference<MatchSettings> client2Settings = new AtomicReference<>();
        AtomicBoolean client1OtherReady = new AtomicBoolean(false);
        AtomicBoolean client2OtherReady = new AtomicBoolean(false);

        // 클라이언트 1 Executor
        GameMenuCommandExecutor menuExecutor1 = new GameMenuCommandExecutor() {
            @Override
            public void onReady(boolean isReady) {
                System.out.println("[CLIENT1-MENU] Other player ready: " + isReady);
                client1OtherReady.set(isReady);
                if (isReady) {
                    client1ReadyLatch.countDown();
                }
            }

            @Override
            public void gameStart(MatchSettings settings) {
                System.out.println("[CLIENT1-MENU] Game starting - Player " + settings.getPlayerNumber());
                System.out.println("[CLIENT1-MENU] MySeed: " + settings.getMySeed() + 
                                   ", OtherSeed: " + settings.getOtherSeed());
                client1Settings.set(settings);
                gameStartLatch.countDown();
            }

            @Override
            public void updatePing(long ping) {
                // Ignore ping
            }
        };

        // 클라이언트 2 Executor
        GameMenuCommandExecutor menuExecutor2 = new GameMenuCommandExecutor() {
            @Override
            public void onReady(boolean isReady) {
                System.out.println("[CLIENT2-MENU] Other player ready: " + isReady);
                client2OtherReady.set(isReady);
                if (isReady) {
                    client2ReadyLatch.countDown();
                }
            }

            @Override
            public void gameStart(MatchSettings settings) {
                System.out.println("[CLIENT2-MENU] Game starting - Player " + settings.getPlayerNumber());
                System.out.println("[CLIENT2-MENU] MySeed: " + settings.getMySeed() + 
                                   ", OtherSeed: " + settings.getOtherSeed());
                client2Settings.set(settings);
                gameStartLatch.countDown();
            }

            @Override
            public void updatePing(long ping) {
                // Ignore ping
            }
        };

        // When: 클라이언트 연결
        client1Thread.setMenuExecutor(menuExecutor1);
        client1Thread.connect(TEST_HOST, TEST_PORT);
        Thread.sleep(300);

        client2Thread.setMenuExecutor(menuExecutor2);
        client2Thread.connect(TEST_HOST, TEST_PORT);
        Thread.sleep(300);

        // 클라이언트 1 Ready
        System.out.println("\n[TEST] === Client 1 sending Ready ===");
        client1Thread.sendCommand(new ReadyCommand(true));
        
        assertTrue("Client 2 should receive ready notification from Client 1", 
                   client2ReadyLatch.await(3, TimeUnit.SECONDS));
        assertTrue("Client 2 should know Client 1 is ready", client2OtherReady.get());

        // 클라이언트 2 Ready
        System.out.println("\n[TEST] === Client 2 sending Ready ===");
        client2Thread.sendCommand(new ReadyCommand(true));

        assertTrue("Client 1 should receive ready notification from Client 2", 
                   client1ReadyLatch.await(3, TimeUnit.SECONDS));
        assertTrue("Client 1 should know Client 2 is ready", client1OtherReady.get());

        // 게임 시작 대기
        System.out.println("\n[TEST] === Waiting for GameStart ===");
        assertTrue("Both clients should receive GameStartCommand", 
                   gameStartLatch.await(3, TimeUnit.SECONDS));

        // Then: MatchSettings 검증
        assertNotNull("Client 1 should receive MatchSettings", client1Settings.get());
        assertNotNull("Client 2 should receive MatchSettings", client2Settings.get());

        MatchSettings settings1 = client1Settings.get();
        MatchSettings settings2 = client2Settings.get();

        // 플레이어 번호 검증
        assertEquals("Client 1 should be assigned as Player 1", 1, settings1.getPlayerNumber());
        assertEquals("Client 2 should be assigned as Player 2", 2, settings2.getPlayerNumber());

        // 시드 교차 검증
        assertEquals("Client 1's mySeed should equal Client 2's otherSeed", 
                     settings1.getMySeed(), settings2.getOtherSeed());
        assertEquals("Client 2's mySeed should equal Client 1's otherSeed", 
                     settings2.getMySeed(), settings1.getOtherSeed());
        assertNotEquals("Seeds should be different", settings1.getMySeed(), settings1.getOtherSeed());

        System.out.println("\n[TEST] ✅ P2P Game Full Flow Test PASSED!");
        System.out.println("[TEST] Player 1: mySeed=" + settings1.getMySeed() + 
                           ", otherSeed=" + settings1.getOtherSeed());
        System.out.println("[TEST] Player 2: mySeed=" + settings2.getMySeed() + 
                           ", otherSeed=" + settings2.getOtherSeed());
    }

    @Test
    public void testP2PCommandRelay() throws Exception {
        // Given: 서버와 클라이언트 설정
        server.start(TEST_PORT);
        Thread.sleep(500);

        CountDownLatch gameStartLatch = new CountDownLatch(2);
        CountDownLatch moveLeftLatch = new CountDownLatch(1);
        CountDownLatch moveRightLatch = new CountDownLatch(1);
        AtomicBoolean client2ReceivedMoveLeft = new AtomicBoolean(false);
        AtomicBoolean client1ReceivedMoveRight = new AtomicBoolean(false);

        // 클라이언트 1 설정
        GameMenuCommandExecutor menuExecutor1 = new GameMenuCommandExecutor() {
            @Override
            public void onReady(boolean isReady) {}

            @Override
            public void gameStart(MatchSettings settings) {
                System.out.println("[CLIENT1] Game started as Player " + settings.getPlayerNumber());
                gameStartLatch.countDown();
            }

            @Override
            public void updatePing(long ping) {}
        };

        GameCommandExecutor gameExecutor1 = new GameCommandExecutor() {
            @Override
            public void moveLeft() {}

            @Override
            public void moveRight() {
                System.out.println("[CLIENT1-GAME] Received MoveRightCommand from Client 2");
                client1ReceivedMoveRight.set(true);
                moveRightLatch.countDown();
            }

            @Override
            public void rotate() {}

            @Override
            public void softDrop() {}

            @Override
            public void hardDrop() {}

            @Override
            public void attack(int lines) {}

            @Override
            public void gameStart(MatchSettings settings) {}

            @Override
            public void gameOver(int score) {}

            @Override
            public void onGameResult(boolean isWinner, int score) {}

            @Override
            public void pause() {}

            @Override
            public void resume() {}

            @Override
            public void onOpponentDisconnect(String reason) {}

            @Override
            public void updateState(String state) {}

            @Override
            public void updatePing(long ping) {}

            @Override
            public void updateOpponentPing(long ping) {}
        };

        // 클라이언트 2 설정
        GameMenuCommandExecutor menuExecutor2 = new GameMenuCommandExecutor() {
            @Override
            public void onReady(boolean isReady) {}

            @Override
            public void gameStart(MatchSettings settings) {
                System.out.println("[CLIENT2] Game started as Player " + settings.getPlayerNumber());
                gameStartLatch.countDown();
            }

            @Override
            public void updatePing(long ping) {}
        };

        GameCommandExecutor gameExecutor2 = new GameCommandExecutor() {
            @Override
            public void moveLeft() {
                System.out.println("[CLIENT2-GAME] Received MoveLeftCommand from Client 1");
                client2ReceivedMoveLeft.set(true);
                moveLeftLatch.countDown();
            }

            @Override
            public void moveRight() {}

            @Override
            public void rotate() {}

            @Override
            public void softDrop() {}

            @Override
            public void hardDrop() {}

            @Override
            public void attack(int lines) {}

            @Override
            public void gameStart(MatchSettings settings) {}

            @Override
            public void gameOver(int score) {}

            @Override
            public void onGameResult(boolean isWinner, int score) {}

            @Override
            public void pause() {}

            @Override
            public void resume() {}

            @Override
            public void onOpponentDisconnect(String reason) {}

            @Override
            public void updateState(String state) {}

            @Override
            public void updatePing(long ping) {}

            @Override
            public void updateOpponentPing(long ping) {}
        };

        // 연결 및 Ready
        client1Thread.setMenuExecutor(menuExecutor1);
        client1Thread.setGameExecutor(gameExecutor1);
        client1Thread.connect(TEST_HOST, TEST_PORT);
        Thread.sleep(300);

        client2Thread.setMenuExecutor(menuExecutor2);
        client2Thread.setGameExecutor(gameExecutor2);
        client2Thread.connect(TEST_HOST, TEST_PORT);
        Thread.sleep(300);

        client1Thread.sendCommand(new ReadyCommand(true));
        Thread.sleep(100);
        client2Thread.sendCommand(new ReadyCommand(true));

        assertTrue("Both clients should start game", gameStartLatch.await(3, TimeUnit.SECONDS));
        Thread.sleep(300); // 게임 시작 후 안정화

        // When: 양방향 명령 전송
        System.out.println("\n[TEST] === Client 1 sending MoveLeftCommand ===");
        client1Thread.sendCommand(new MoveLeftCommand());

        System.out.println("[TEST] === Client 2 sending MoveRightCommand ===");
        client2Thread.sendCommand(new MoveRightCommand());

        // Then: 명령 수신 검증
        assertTrue("Client 2 should receive MoveLeftCommand from Client 1", 
                   moveLeftLatch.await(3, TimeUnit.SECONDS));
        assertTrue("Client 1 should receive MoveRightCommand from Client 2", 
                   moveRightLatch.await(3, TimeUnit.SECONDS));

        assertTrue("Client 2 should have processed MoveLeftCommand", client2ReceivedMoveLeft.get());
        assertTrue("Client 1 should have processed MoveRightCommand", client1ReceivedMoveRight.get());

        System.out.println("\n[TEST] ✅ P2P Command Relay Test PASSED!");
    }

    @Test
    public void testPlayerNumberAssignment() throws Exception {
        // Given
        server.start(TEST_PORT);
        Thread.sleep(500);

        CountDownLatch gameStartLatch = new CountDownLatch(2);
        AtomicReference<Integer> player1Number = new AtomicReference<>();
        AtomicReference<Integer> player2Number = new AtomicReference<>();

        GameMenuCommandExecutor executor1 = new GameMenuCommandExecutor() {
            @Override
            public void onReady(boolean isReady) {}

            @Override
            public void gameStart(MatchSettings settings) {
                player1Number.set(settings.getPlayerNumber());
                gameStartLatch.countDown();
            }

            @Override
            public void updatePing(long ping) {}
        };

        GameMenuCommandExecutor executor2 = new GameMenuCommandExecutor() {
            @Override
            public void onReady(boolean isReady) {}

            @Override
            public void gameStart(MatchSettings settings) {
                player2Number.set(settings.getPlayerNumber());
                gameStartLatch.countDown();
            }

            @Override
            public void updatePing(long ping) {}
        };

        // When
        client1Thread.setMenuExecutor(executor1);
        client1Thread.connect(TEST_HOST, TEST_PORT);
        Thread.sleep(200);

        client2Thread.setMenuExecutor(executor2);
        client2Thread.connect(TEST_HOST, TEST_PORT);
        Thread.sleep(200);

        client1Thread.sendCommand(new ReadyCommand(true));
        client2Thread.sendCommand(new ReadyCommand(true));

        assertTrue(gameStartLatch.await(3, TimeUnit.SECONDS));

        // Then
        assertNotNull("Player 1 should receive player number", player1Number.get());
        assertNotNull("Player 2 should receive player number", player2Number.get());
        assertEquals("First client should be Player 1", Integer.valueOf(1), player1Number.get());
        assertEquals("Second client should be Player 2", Integer.valueOf(2), player2Number.get());

        System.out.println("\n[TEST] ✅ Player Number Assignment Test PASSED!");
    }
    @Test
    public void testAttackAndGameOver() throws Exception {
        // Given: 서버와 클라이언트 설정
        server.start(TEST_PORT);
        Thread.sleep(500);

        CountDownLatch gameStartLatch = new CountDownLatch(2);
        CountDownLatch attackLatch = new CountDownLatch(1);
        CountDownLatch gameOverLatch = new CountDownLatch(1);
        
        AtomicReference<Integer> receivedAttackLines = new AtomicReference<>();
        AtomicReference<Integer> receivedGameOverScore = new AtomicReference<>();

        // 클라이언트 1 설정 (공격자)
        GameMenuCommandExecutor menuExecutor1 = new GameMenuCommandExecutor() {
            @Override public void onReady(boolean isReady) {}
            @Override public void gameStart(MatchSettings settings) { gameStartLatch.countDown(); }
            @Override public void updatePing(long ping) {}
        };

        GameCommandExecutor gameExecutor1 = new GameCommandExecutor() {
            @Override public void moveLeft() {}
            @Override public void moveRight() {}
            @Override public void rotate() {}
            @Override public void softDrop() {}
            @Override public void hardDrop() {}
            @Override public void attack(int lines) {}
            @Override public void gameStart(MatchSettings settings) {}
            @Override public void gameOver(int score) {
                // 자신이 보낸 게임오버는 받을 수도 있고 안 받을 수도 있음 (서버 구현에 따라 다름)
                // 여기서는 상대방의 게임오버 수신을 테스트하므로 패스
            }
            @Override public void onGameResult(boolean isWinner, int score) {}
            @Override public void pause() {}
            @Override public void resume() {}
            @Override public void onOpponentDisconnect(String reason) {}
            @Override public void updateState(String state) {}
            @Override public void updatePing(long ping) {}
            @Override public void updateOpponentPing(long ping) {}
        };

        // 클라이언트 2 설정 (피해자 & 게임오버 발생자)
        GameMenuCommandExecutor menuExecutor2 = new GameMenuCommandExecutor() {
            @Override public void onReady(boolean isReady) {}
            @Override public void gameStart(MatchSettings settings) { gameStartLatch.countDown(); }
            @Override public void updatePing(long ping) {}
        };

        GameCommandExecutor gameExecutor2 = new GameCommandExecutor() {
            @Override public void moveLeft() {}
            @Override public void moveRight() {}
            @Override public void rotate() {}
            @Override public void softDrop() {}
            @Override public void hardDrop() {}
            @Override public void attack(int lines) {
                System.out.println("[CLIENT2-GAME] Received Attack: " + lines + " lines");
                receivedAttackLines.set(lines);
                attackLatch.countDown();
            }
            @Override public void gameStart(MatchSettings settings) {}
            @Override public void gameOver(int score) {
                System.out.println("[CLIENT2-GAME] Received GameOver: " + score);
                receivedGameOverScore.set(score);
                gameOverLatch.countDown();
            }
            @Override public void onGameResult(boolean isWinner, int score) {}
            @Override public void pause() {}
            @Override public void resume() {}
            @Override public void onOpponentDisconnect(String reason) {}
            @Override public void updateState(String state) {}
            @Override public void updatePing(long ping) {}
            @Override public void updateOpponentPing(long ping) {}
        };

        // 연결 및 게임 시작
        client1Thread.setMenuExecutor(menuExecutor1);
        client1Thread.setGameExecutor(gameExecutor1);
        client1Thread.connect(TEST_HOST, TEST_PORT);
        
        client2Thread.setMenuExecutor(menuExecutor2);
        client2Thread.setGameExecutor(gameExecutor2);
        client2Thread.connect(TEST_HOST, TEST_PORT);
        Thread.sleep(300);

        client1Thread.sendCommand(new ReadyCommand(true));
        client2Thread.sendCommand(new ReadyCommand(true));
        
        assertTrue("Game should start", gameStartLatch.await(3, TimeUnit.SECONDS));
        Thread.sleep(200);

        // When 1: Client 1 attacks Client 2
        System.out.println("\n[TEST] === Client 1 sending Attack(2) ===");
        client1Thread.sendCommand(new AttackCommand(2));

        // Then 1: Client 2 receives attack
        assertTrue("Client 2 should receive attack", attackLatch.await(3, TimeUnit.SECONDS));
        assertEquals("Should receive 2 lines of attack", Integer.valueOf(2), receivedAttackLines.get());

        // When 2: Client 1 sends GameOver (Client 1 died)
        // Note: GameOverCommand is broadcasted.
        System.out.println("\n[TEST] === Client 1 sending GameOver(500) ===");
        client1Thread.sendCommand(new GameOverCommand(500));

        // Then 2: Client 2 receives GameOver
        assertTrue("Client 2 should receive game over", gameOverLatch.await(3, TimeUnit.SECONDS));
        assertEquals("Should receive score 500", Integer.valueOf(500), receivedGameOverScore.get());

        System.out.println("\n[TEST] ✅ Attack and GameOver Test PASSED!");
    }
}
