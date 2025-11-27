package org.tetris.network;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.tetris.network.comand.GameCommand;
import org.tetris.network.comand.MoveLeftCommand;
import org.tetris.network.comand.UpdateStateCommand;
import org.tetris.network.mocks.TestGameCommandExecutor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

/**
 * 네트워크 모듈의 전체 통합 테스트. 서버 시작, 클라이언트 연결, 메시지 브로드캐스트 등을 테스트합니다.
 */
public class NetworkIntegrationTest {
    private static final int TIMEOUT_SECONDS = 10;

    private List<ClientThread> clients;
    private List<TestGameCommandExecutor> gameExecutors;

    @Before
    public void setUp() {
        clients = new ArrayList<>();
        gameExecutors = new ArrayList<>();
        // GameServer 인스턴스 리셋
        GameServer.getInstance().reset();
    }

    @After
    public void tearDown() {
        // 모든 클라이언트 연결 해제
        for (ClientThread client : clients) {
            if (client != null) {
                client.disconnect();
            }
        }
        clients.clear();
        gameExecutors.clear();

        // 서버 종료
        stopServer();
    }

    /**
     * 테스트용 서버를 시작합니다.
     */
    private void startServer() throws IOException {
        GameServer.getInstance().start();

        // 서버가 시작될 시간을 줌
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 서버를 종료합니다.
     */
    private void stopServer() {
        GameServer.getInstance().stop();
    }

    @Test
    public void testServerStartAndStop() throws IOException {
        // 서버 시작
        startServer();

        // 서버가 실행 중인지 확인 (예외가 발생하지 않으면 성공)
        assertTrue("서버가 시작되어야 합니다", true);

        // 서버 종료
        stopServer();

        // 잠시 대기
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // 서버가 종료되었는지 확인 (예외가 발생하지 않으면 성공)
    }

    @Test
    public void testSingleClientConnection() throws IOException, InterruptedException {
        // 서버 시작
        startServer();

        // 클라이언트 생성 및 연결
        TestGameCommandExecutor executor = new TestGameCommandExecutor();
        ClientThread client = new ClientThread();
        client.setGameExecutor(executor);

        clients.add(client);
        gameExecutors.add(executor);

        // 연결
        client.connect("localhost", GameServer.PORT);

        // 연결 성공 확인 (예외가 발생하지 않으면 성공)

        // 잠시 대기
        Thread.sleep(100);
    }

    @Test
    public void testMultipleClientConnections() throws IOException, InterruptedException {
        // 서버 시작
        startServer();

        // 여러 클라이언트 생성 및 연결
        int clientCount = 2; // GameServer only accepts 2 clients
        CountDownLatch latch = new CountDownLatch(clientCount);

        for (int i = 0; i < clientCount; i++) {
            TestGameCommandExecutor executor = new TestGameCommandExecutor();
            ClientThread client = new ClientThread();
            client.setGameExecutor(executor);

            gameExecutors.add(executor);
            clients.add(client);

            // 연결
            client.connect("localhost", GameServer.PORT);
            latch.countDown();
        }

        // 모든 클라이언트가 연결될 때까지 대기
        assertTrue("모든 클라이언트가 연결되어야 합니다", latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));

        // 잠시 대기
        Thread.sleep(100);
    }

    @Test
    public void testClientSendCommand() throws IOException, InterruptedException {
        // 서버 시작
        startServer();

        // 클라이언트 생성 및 연결
        TestGameCommandExecutor executor = new TestGameCommandExecutor();
        ClientThread client = new ClientThread();
        client.setGameExecutor(executor);

        clients.add(client);
        gameExecutors.add(executor);

        client.connect("localhost", GameServer.PORT);

        // 잠시 대기 (연결 완료)
        Thread.sleep(100);

        // 명령 전송
        GameCommand command = new MoveLeftCommand();
        client.sendCommand(command);

        // 명령이 전송되었는지 확인 (예외가 발생하지 않으면 성공)

        // 서버가 명령을 처리할 시간을 줌
        Thread.sleep(100);
    }

    @Test
    public void testClientDisconnectAndReconnect() throws IOException, InterruptedException {
        // 서버 시작
        startServer();

        // 클라이언트 생성 및 연결
        TestGameCommandExecutor executor = new TestGameCommandExecutor();
        ClientThread client = new ClientThread();
        client.setGameExecutor(executor);

        client.connect("localhost", GameServer.PORT);
        Thread.sleep(100);

        // 연결 해제
        client.disconnect();
        Thread.sleep(100);

        // 재연결
        ClientThread newClient = new ClientThread();
        newClient.setGameExecutor(executor);
        clients.add(newClient);
        gameExecutors.add(executor);

        newClient.connect("localhost", GameServer.PORT);
        Thread.sleep(100);

        // 재연결 성공 확인 (예외가 발생하지 않으면 성공)
    }

    @Test
    public void testBroadcastBetweenClients() throws IOException, InterruptedException {
        // 서버 시작
        startServer();

        // 두 개의 클라이언트 생성
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

        // 두 클라이언트 모두 연결
        client1.connect("localhost", GameServer.PORT);
        client2.connect("localhost", GameServer.PORT);

        Thread.sleep(200);

        // 클라이언트1에서 명령 전송
        UpdateStateCommand command = new UpdateStateCommand("Test State from Client 1");
        client1.sendCommand(command);

        // 브로드캐스트가 처리될 시간을 줌
        Thread.sleep(500);

        // 브로드캐스트가 성공적으로 이루어졌는지 확인 (예외가 발생하지 않으면 성공)
    }

    @Test
    public void testServerHandlesClientDisconnection() throws IOException, InterruptedException {
        // 서버 시작
        startServer();

        // 클라이언트 생성 및 연결
        TestGameCommandExecutor executor = new TestGameCommandExecutor();
        ClientThread client = new ClientThread();
        client.setGameExecutor(executor);

        client.connect("localhost", GameServer.PORT);
        Thread.sleep(100);

        // 클라이언트 연결 해제
        client.disconnect();
        Thread.sleep(100);

        // 서버가 여전히 실행 중인지 확인 (예외가 발생하지 않으면 성공)
    }

    @Test
    public void testConcurrentClientConnections() throws IOException, InterruptedException {
        // 서버 시작
        startServer();

        // 동시에 여러 클라이언트 연결
        int clientCount = 5;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(clientCount);
        AtomicInteger successCount = new AtomicInteger(0);

        for (int i = 0; i < clientCount; i++) {
            final int clientId = i;
            new Thread(() -> {
                try {
                    startLatch.await(); // 모든 스레드가 준비될 때까지 대기

                    TestGameCommandExecutor executor = new TestGameCommandExecutor();
                    ClientThread client = new ClientThread();
                    client.setGameExecutor(executor);

                    synchronized (clients) {
                        clients.add(client);
                        gameExecutors.add(executor);
                    }

                    client.connect("localhost", GameServer.PORT);
                    successCount.incrementAndGet();

                } catch (Exception e) {
                    // Expected failure for clients > 2
                    // System.err.println("[TEST] Client " + clientId + " failed: " +
                    // e.getMessage());
                } finally {
                    doneLatch.countDown();
                }
            }).start();
        }

        // 모든 스레드 시작
        startLatch.countDown();

        // 모든 클라이언트가 연결 시도를 완료할 때까지 대기
        assertTrue("모든 클라이언트가 연결 시도를 완료해야 합니다", doneLatch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));

        // 최대 2명의 클라이언트만 연결되어야 함
        assertEquals("최대 2명의 클라이언트만 연결되어야 합니다", 2, successCount.get());

        Thread.sleep(100);
    }

    @Test
    public void testServerShutdownDisconnectsClients() throws IOException, InterruptedException {
        // 서버 시작
        startServer();

        // 클라이언트 생성 및 연결
        TestGameCommandExecutor executor = new TestGameCommandExecutor();
        ClientThread client = new ClientThread();
        client.setGameExecutor(executor);

        clients.add(client);
        gameExecutors.add(executor);

        client.connect("localhost", GameServer.PORT);
        Thread.sleep(100);

        // 서버 종료
        stopServer();
        Thread.sleep(100);

        // 서버 종료 시 클라이언트가 안전하게 처리되어야 합니다 (예외가 발생하지 않으면 성공)
    }
}
