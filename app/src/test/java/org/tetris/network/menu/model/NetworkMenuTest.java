package org.tetris.network.menu.model;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.tetris.network.GameServer;

public class NetworkMenuTest {
    
    private NetworkMenu networkMenu;

    @Before
    public void setUp() {
        networkMenu = new NetworkMenu();
        // 서버 인스턴스 리셋
        GameServer.getInstance().reset();
    }

    @After
    public void tearDown() {
        // 테스트 후 정리
        if (networkMenu != null) {
            networkMenu.clear();
        }
        GameServer.getInstance().stop();
        
        // 포트 해제 대기
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Test
    public void testCreate() throws InterruptedException {
        // Given
        networkMenu.setIsHost(true);
        networkMenu.setPort(12345);

        // When
        try {
            networkMenu.create();
        } catch (Exception e) {
            fail("서버 생성 중 예외 발생: " + e.getMessage());
        }

        // 서버 시작 대기
        Thread.sleep(300);

        // Then
        assertTrue("Host 모드여야 합니다", networkMenu.getIsHost());
        assertEquals("localhost", networkMenu.getIpAddress());
        assertEquals(12345, networkMenu.getPort());
        
        // 서버가 실행 중인지 확인
        assertTrue("서버가 실행 중이어야 합니다", GameServer.getInstance() != null);
    }

    @Test
    public void testJoin() throws InterruptedException {
        // Given - 먼저 서버 시작
        NetworkMenu hostMenu = new NetworkMenu();
        hostMenu.setIsHost(true);
        hostMenu.setPort(12345);
        hostMenu.create();
        
        Thread.sleep(300); // 서버 준비 대기

        // When - 클라이언트 연결
        networkMenu.setIsHost(false);
        networkMenu.setIpAddress("localhost");
        networkMenu.setPort(12345);
        networkMenu.join();

        Thread.sleep(200); // 연결 대기

        // Then
        assertFalse("Client 모드여야 합니다", networkMenu.getIsHost());
        assertEquals("localhost", networkMenu.getIpAddress());
        assertEquals(12345, networkMenu.getPort());
        
        // 정리
        hostMenu.clear();
    }

    @Test
    public void testSetIpAddress() {
        // Valid IP
        networkMenu.setIpAddress("192.168.1.1");
        assertEquals("192.168.1.1", networkMenu.getIpAddress());

        // localhost
        networkMenu.setIpAddress("localhost");
        assertEquals("localhost", networkMenu.getIpAddress());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetInvalidIpAddress() {
        networkMenu.setIpAddress("999.999.999.999");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetEmptyIpAddress() {
        networkMenu.setIpAddress("");
    }

    @Test
    public void testSetPort() {
        networkMenu.setPort(54321);
        assertEquals(54321, networkMenu.getPort());

        networkMenu.setPort(1);
        assertEquals(1, networkMenu.getPort());

        networkMenu.setPort(65535);
        assertEquals(65535, networkMenu.getPort());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetInvalidPortTooLow() {
        networkMenu.setPort(0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetInvalidPortTooHigh() {
        networkMenu.setPort(65536);
    }

    @Test
    public void testSetReady() {
        assertFalse("초기 상태는 Ready가 아니어야 합니다", networkMenu.isReady());

        networkMenu.setIsReady(true);
        assertTrue("Ready 상태여야 합니다", networkMenu.isReady());

        networkMenu.setIsReady(false);
        assertFalse("Ready 해제 상태여야 합니다", networkMenu.isReady());
    }

    @Test
    public void testClear() {
        // Given - 상태 설정
        networkMenu.setIsHost(false);
        networkMenu.setIpAddress("192.168.1.1");
        networkMenu.setPort(12345);
        networkMenu.setIsReady(true);

        // When
        networkMenu.clear();

        // Then - 초기값으로 리셋
        assertTrue("Host 모드로 초기화되어야 합니다", networkMenu.getIsHost());
        assertEquals("IP는 빈 문자열이어야 합니다", "", networkMenu.getIpAddress());
        assertEquals("기본 포트여야 합니다", 54321, networkMenu.getPort());
        assertFalse("Ready는 false여야 합니다", networkMenu.isReady());
    }

    @Test
    public void testIsValidIP() {
        assertTrue(networkMenu.isValidIP("192.168.1.1"));
        assertTrue(networkMenu.isValidIP("localhost"));
        assertTrue(networkMenu.isValidIP("127.0.0.1"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIsValidIPWithInvalidIP() {
        networkMenu.isValidIP("invalid_ip");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIsValidIPWithNull() {
        networkMenu.isValidIP(null);
    }

    @Test
    public void testIsValidPort() {
        assertTrue(networkMenu.isValidPort(1));
        assertTrue(networkMenu.isValidPort(12345));
        assertTrue(networkMenu.isValidPort(65535));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIsValidPortTooLow() {
        networkMenu.isValidPort(0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIsValidPortTooHigh() {
        networkMenu.isValidPort(65536);
    }
}
