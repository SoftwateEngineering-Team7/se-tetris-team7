package org.tetris.network;

import org.junit.Test;
import org.tetris.network.comand.GameCommand;
import org.tetris.network.comand.MoveLeftCommand;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import static org.junit.Assert.fail;

/**
 * ClientThread의 연결, 명령 전송, 연결 해제 기능을 테스트합니다.
 * BaseNetworkTest를 상속받아 공통 설정을 재사용합니다.
 */
public class ClientThreadTest extends BaseNetworkTest {

    private ClientThread clientThread;
    private ServerSocket mockServerSocket;
    private Thread serverAcceptThread;

    @Override
    public void setUp() throws IOException {
        super.setUp();
        // ClientThreadTest는 GameServer 대신 Mock ServerSocket을 사용하므로
        // BaseNetworkTest의 startServer()를 호출하지 않고 개별 설정합니다.

        // 엔진과 클라이언트 생성 (BaseNetworkTest 헬퍼 사용)
        clientThread = createClientWithLocalMultiEngine();

        // 목업 소켓 셋업 (포트 0 = 자동 할당)
        mockServerSocket = new ServerSocket(0);
    }

    @Override
    public void tearDown() {
        super.tearDown(); // 클라이언트 연결 해제 등 처리

        try {
            if (mockServerSocket != null && !mockServerSocket.isClosed()) {
                mockServerSocket.close();
            }
            if (serverAcceptThread != null && serverAcceptThread.isAlive()) {
                serverAcceptThread.interrupt();
                serverAcceptThread.join(1000);
            }
            Thread.sleep(50);
        } catch (IOException | InterruptedException e) {
            // Ignore cleanup errors
        }
    }

    @Test
    public void testConnectSuccess() throws IOException {
        startMockServer();

        clientThread.connect("localhost", mockServerSocket.getLocalPort());
        // 예외 없으면 성공
    }

    @Test
    public void testConnectToInvalidHost() {
        try {
            clientThread.connect("invalid-host-12345", mockServerSocket.getLocalPort());
            fail("Should throw exception when connecting to invalid host");
        } catch (UnknownHostException e) {
            // Expected
        } catch (IOException e) {
            // Also acceptable
        }
    }

    @Test
    public void testConnectToClosedPort() {
        try {
            // 닫힌 포트 (임의의 포트 사용, 충돌 가능성 낮음)
            clientThread.connect("localhost", 54321);
            fail("Should throw exception when connecting to closed port");
        } catch (IOException e) {
            // Expected
        }
    }

    @Test
    public void testDuplicateConnect() throws IOException {
        startMockServerLoop();

        clientThread.connect("localhost", mockServerSocket.getLocalPort());
        // 두 번째 연결 시도 (로그만 찍히고 무시됨)
        clientThread.connect("localhost", mockServerSocket.getLocalPort());
    }

    @Test
    public void testSendCommandWhenNotConnected() {
        GameCommand command = new MoveLeftCommand();
        // 연결 없이 전송 -> 예외 없이 로그만 출력되어야 함
        clientThread.sendCommand(command);
    }

    @Test
    public void testDisconnect() throws IOException {
        startMockServer();

        clientThread.connect("localhost", mockServerSocket.getLocalPort());
        clientThread.disconnect();
    }

    @Test
    public void testDisconnectWhenNotConnected() {
        clientThread.disconnect();
    }

    // --- Helpers ---

    private void startMockServer() {
        serverAcceptThread = new Thread(() -> {
            try {
                Socket socket = mockServerSocket.accept();
                // Handshake
                new ObjectOutputStream(socket.getOutputStream()).flush();
                new ObjectInputStream(socket.getInputStream());
            } catch (IOException e) {
                // Ignore
            }
        });
        serverAcceptThread.start();
    }

    private void startMockServerLoop() {
        serverAcceptThread = new Thread(() -> {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    Socket socket = mockServerSocket.accept();
                    new ObjectOutputStream(socket.getOutputStream()).flush();
                    new ObjectInputStream(socket.getInputStream());
                }
            } catch (IOException e) {
                // Ignore
            }
        });
        serverAcceptThread.start();
    }
}
