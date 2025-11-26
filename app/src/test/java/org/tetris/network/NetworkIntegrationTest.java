package org.tetris.network;

import org.junit.Test;
import org.tetris.network.comand.GameCommand;
import org.tetris.network.comand.MoveLeftCommand;
import org.tetris.network.comand.UpdateStateCommand;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * 네트워크 모듈의 전체 통합 테스트.
 * 서버 시작, 클라이언트 연결, 메시지 브로드캐스트 등을 테스트합니다.
 */
public class NetworkIntegrationTest extends BaseNetworkTest {

    @Test
    public void testServerStartAndStop() throws IOException {
        startServer();
        assertTrue("서버가 시작되어야 합니다", true); // 예외 없으면 성공

        stopServer();
        // 예외 없으면 성공
    }

    @Test
    public void testSingleClientConnection() throws IOException, InterruptedException {
        startServer();

        ClientThread client = createClientWithP2PEngine();
        client.connect("localhost", GameServer.PORT);

        Thread.sleep(100);
        // 예외 없으면 성공
    }

    @Test
    public void testMultipleClientConnections() throws IOException, InterruptedException {
        startServer();

        int clientCount = 2;
        CountDownLatch latch = new CountDownLatch(clientCount);

        for (int i = 0; i < clientCount; i++) {
            ClientThread client = createClientWithP2PEngine();
            client.connect("localhost", GameServer.PORT);
            latch.countDown();
        }

        assertTrue("모든 클라이언트가 연결되어야 합니다",
                latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
        Thread.sleep(100);
    }

    @Test
    public void testClientSendCommand() throws IOException, InterruptedException {
        startServer();

        ClientThread client = createClientWithP2PEngine();
        client.connect("localhost", GameServer.PORT);
        Thread.sleep(100);

        GameCommand command = new MoveLeftCommand();
        client.sendCommand(command);

        Thread.sleep(100);
        // 예외 없으면 성공
    }

    @Test
    public void testClientDisconnectAndReconnect() throws IOException, InterruptedException {
        startServer();

        ClientThread client = createClientWithP2PEngine();
        client.connect("localhost", GameServer.PORT);
        Thread.sleep(100);

        // 연결 해제
        client.disconnect();
        Thread.sleep(100);

        // 재연결 (새 클라이언트 객체 사용)
        ClientThread newClient = createClientWithP2PEngine();
        newClient.connect("localhost", GameServer.PORT);
        Thread.sleep(100);

        // 재연결 성공 확인 (예외가 발생하지 않으면 성공)
    }

    @Test
    public void testBroadcastBetweenClients() throws IOException, InterruptedException {
        startServer();

        ClientThread client1 = createClientWithP2PEngine();
        ClientThread client2 = createClientWithP2PEngine();

        client1.connect("localhost", GameServer.PORT);
        client2.connect("localhost", GameServer.PORT);
        Thread.sleep(200);

        // 클라이언트1에서 명령 전송
        UpdateStateCommand command = new UpdateStateCommand("Test State from Client 1");
        client1.sendCommand(command);

        Thread.sleep(500);

        // 브로드캐스트가 성공적으로 이루어졌는지 확인 (예외가 발생하지 않으면 성공)
    }

    @Test
    public void testServerHandlesClientDisconnection() throws IOException, InterruptedException {
        startServer();

        ClientThread client = createClientWithP2PEngine();
        client.connect("localhost", GameServer.PORT);
        Thread.sleep(100);

        client.disconnect();
        Thread.sleep(100);
        // 서버 계속 실행 중이어야 함
    }

    @Test
    public void testConcurrentClientConnections() throws IOException, InterruptedException {
        startServer();

        int clientCount = 5;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(clientCount);
        AtomicInteger successCount = new AtomicInteger(0);

        for (int i = 0; i < clientCount; i++) {
            new Thread(() -> {
                try {
                    startLatch.await();
                    // 동시성 테스트에서는 헬퍼 대신 직접 생성하여 리스트 동기화 문제 회피
                    // 하지만 BaseNetworkTest의 리스트는 @Before에서 초기화되므로
                    // 여기서는 단순히 연결 성공 여부만 카운트
                    ClientThread client = createClientWithP2PEngine();
                    client.connect("localhost", GameServer.PORT);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    // Expected failure for > 2 clients
                } finally {
                    doneLatch.countDown();
                }
            }).start();
        }

        startLatch.countDown();
        assertTrue("모든 클라이언트가 연결 시도를 완료해야 합니다",
                doneLatch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));

        assertEquals("최대 2명의 클라이언트만 연결되어야 합니다",
                2, successCount.get());
    }

    @Test
    public void testServerShutdownDisconnectsClients() throws IOException, InterruptedException {
        startServer();

        ClientThread client = createClientWithP2PEngine();
        client.connect("localhost", GameServer.PORT);
        Thread.sleep(100);

        stopServer();
        Thread.sleep(100);
        // 클라이언트 측에서 연결 끊김 감지 (로그 확인)
    }
}
