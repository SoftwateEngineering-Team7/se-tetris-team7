package org.tetris.network;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * GameServer 테스트
 */
public class GameServerTest {

    private GameServer server;

    @Before
    public void setUp() {
        server = GameServer.getInstance();
        server.reset();
    }

    @After
    public void tearDown() {
        if (server != null) {
            server.stop();
            server.reset();
        }
    }

    @Test
    public void testGameServerSingleton() {
        // When
        GameServer instance1 = GameServer.getInstance();
        GameServer instance2 = GameServer.getInstance();

        // Then
        assertSame("싱글톤 인스턴스가 동일해야 합니다", instance1, instance2);
    }

    @Test
    public void testGameInProgressInitialState() {
        // Then
        assertFalse("게임이 시작 전에는 isGameInProgress가 false여야 합니다", 
                    server.isGameInProgress());
    }

    @Test
    public void testEndGame() {
        // Given - 게임이 진행 중이라고 가정 (내부 상태 설정은 불가하므로 endGame 호출만 테스트)
        
        // When
        server.endGame();

        // Then
        assertFalse("endGame 후에는 isGameInProgress가 false여야 합니다", 
                    server.isGameInProgress());
    }

    @Test
    public void testServerStart() throws Exception {
        // When
        server.start(54321);
        
        // Then - 서버가 시작되면 예외 없이 통과
        Thread.sleep(100);
        
        // Cleanup
        server.stop();
    }

    @Test
    public void testServerStop() throws Exception {
        // Given
        server.start(54322);
        Thread.sleep(100);

        // When
        server.stop();
        
        // Then - stop 후에도 예외 없이 통과
        // 다시 시작 가능한지 확인
        server.start(54323);
        Thread.sleep(100);
        server.stop();
    }

    @Test
    public void testReset() throws Exception {
        // Given
        server.start(54324);
        Thread.sleep(100);
        server.stop();

        // When
        server.reset();

        // Then
        assertFalse("reset 후에는 isGameInProgress가 false여야 합니다", 
                    server.isGameInProgress());
    }
}
