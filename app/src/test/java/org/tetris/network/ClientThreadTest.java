package org.tetris.network;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.tetris.network.comand.GameCommand;
import org.tetris.network.comand.MoveLeftCommand;
import org.tetris.network.mocks.TestGameCommandExecutor;
import org.tetris.network.mocks.TestGameMenuCommandExecutor;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import static org.junit.Assert.*;

/**
 * ClientThread의 연결, 명령 전송, 연결 해제 기능을 테스트합니다.
 */
public class ClientThreadTest {
    private ClientThread clientThread;
    private TestGameCommandExecutor gameExecutor;
    private TestGameMenuCommandExecutor menuExecutor;
    private ServerSocket mockServerSocket; // 서버 소켓
    private Thread serverAcceptThread; // 서버 소켓 연결 수락 스레드

    @Before
    public void setUp() throws IOException {
        clientThread = new ClientThread();
        gameExecutor = new TestGameCommandExecutor();
        menuExecutor = new TestGameMenuCommandExecutor();

        clientThread.setGameExecutor(gameExecutor);
        clientThread.setMenuExecutor(menuExecutor);

        // 목업 소켓 셋업
        // OS가 여유 포트를 할당하도록 포트 0 사용
        mockServerSocket = new ServerSocket(0);
    }

    @After
    public void tearDown() throws IOException {
        if (clientThread != null) {
            clientThread.disconnect();
        }
        if (mockServerSocket != null && !mockServerSocket.isClosed()) {
            mockServerSocket.close();
        }
        if (serverAcceptThread != null && serverAcceptThread.isAlive()) {
            serverAcceptThread.interrupt();
            try {
                serverAcceptThread.join(1000); // 스레드가 종료될 때까지 대기
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        // 포트 해제를 위한 대기 시간
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Test
    public void testConnectSuccess() throws IOException {
        // 연결을 수락할 스레드 시작
        serverAcceptThread = new Thread(() -> {
            try {
                Socket socket = mockServerSocket.accept();
                // 핸드셰이크: 헤더 쓰기 후 헤더 읽기
                new ObjectOutputStream(socket.getOutputStream()).flush();
                new ObjectInputStream(socket.getInputStream());
            } catch (IOException e) {
                // 종료 중 예외 발생
            }
        });
        serverAcceptThread.start();

        // 목업 서버에 연결
        clientThread.connect("localhost", mockServerSocket.getLocalPort());

        // 예외가 발생하지 않으면 연결 성공
    }

    @Test
    public void testConnectToInvalidHost() {
        try {
            // 존재하지 않는 호스트에 연결 시도
            clientThread.connect("invalid-host-12345", mockServerSocket.getLocalPort());
            fail("Should throw exception when connecting to invalid host");
        } catch (UnknownHostException e) {
            // 예외 발생
        } catch (IOException e) {
            // 이것도 허용됨
        }
    }

    @Test
    public void testConnectToClosedPort() {
        try {
            // 닫힌 포트에 연결 시도 (54321이 닫혀 있거나 적어도 우리 목업 서버가 아니라고 가정)
            // 확실히 닫힌 포트를 사용하는 것이 좋지만, 간단한 테스트에서는 임의의 포트를 선택하는 것이 일반적임
            clientThread.connect("localhost", 54321);
            fail("Should throw exception when connecting to closed port");
        } catch (IOException e) {
            // 예상됨
        }
    }

    @Test
    public void testDuplicateConnect() throws IOException {
        // 서버 시작
        serverAcceptThread = new Thread(() -> {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    Socket socket = mockServerSocket.accept();
                    new ObjectOutputStream(socket.getOutputStream()).flush();
                    new ObjectInputStream(socket.getInputStream());
                }
            } catch (IOException e) {
                // 예상됨
            }
        });
        serverAcceptThread.start();

        // 첫 번째 연결
        clientThread.connect("localhost", mockServerSocket.getLocalPort());

        // 두 번째 연결 시도 (무시되거나 적절히 처리되어야 함)
        clientThread.connect("localhost", mockServerSocket.getLocalPort());
    }

    @Test
    public void testSendCommandWhenNotConnected() {
        // 연결 없이 커맨드 전송 시도
        GameCommand command = new MoveLeftCommand();

        // 안전해야 함 (예외 없음)
        clientThread.sendCommand(command);
    }

    @Test
    public void testDisconnect() throws IOException {
        // 서버 시작
        serverAcceptThread = new Thread(() -> {
            try {
                Socket socket = mockServerSocket.accept();
                new ObjectOutputStream(socket.getOutputStream()).flush();
                new ObjectInputStream(socket.getInputStream());
            } catch (IOException e) {
                // 예상됨
            }
        });
        serverAcceptThread.start();

        // 연결
        clientThread.connect("localhost", mockServerSocket.getLocalPort());

        // 연결 해제
        clientThread.disconnect();
    }

    @Test
    public void testDisconnectWhenNotConnected() {
        // 연결 없이 연결 해제
        clientThread.disconnect();
    }

    @Test
    public void testIsConnectedWhenNotConnected() {
        // When - 연결하지 않은 상태
        
        // Then
        assertFalse("연결하지 않은 상태에서 isConnected는 false여야 합니다", 
                    clientThread.isConnected());
    }

    @Test
    public void testIsConnectedAfterConnect() throws IOException {
        // Given - 서버 시작
        serverAcceptThread = new Thread(() -> {
            try {
                Socket socket = mockServerSocket.accept();
                new ObjectOutputStream(socket.getOutputStream()).flush();
                new ObjectInputStream(socket.getInputStream());
                // 연결 유지
                Thread.sleep(5000);
            } catch (IOException | InterruptedException e) {
                // 예상됨
            }
        });
        serverAcceptThread.start();

        // When
        clientThread.connect("localhost", mockServerSocket.getLocalPort());

        // Then
        assertTrue("연결 후 isConnected는 true여야 합니다", 
                   clientThread.isConnected());
    }

    @Test
    public void testIsConnectedAfterDisconnect() throws IOException {
        // Given - 서버 시작 및 연결
        serverAcceptThread = new Thread(() -> {
            try {
                Socket socket = mockServerSocket.accept();
                new ObjectOutputStream(socket.getOutputStream()).flush();
                new ObjectInputStream(socket.getInputStream());
            } catch (IOException e) {
                // 예상됨
            }
        });
        serverAcceptThread.start();
        clientThread.connect("localhost", mockServerSocket.getLocalPort());

        // When
        clientThread.disconnect();

        // Then
        assertFalse("연결 해제 후 isConnected는 false여야 합니다", 
                    clientThread.isConnected());
    }

    @Test
    public void testHasGameExecutor() {
        // Given - setUp에서 이미 gameExecutor가 설정됨
        
        // Then
        assertTrue("gameExecutor가 설정된 상태에서 hasGameExecutor는 true여야 합니다", 
                   clientThread.hasGameExecutor());
    }

    @Test
    public void testHasGameExecutorWhenNotSet() {
        // Given
        ClientThread newClient = new ClientThread();
        
        // Then
        assertFalse("gameExecutor가 설정되지 않은 상태에서 hasGameExecutor는 false여야 합니다", 
                    newClient.hasGameExecutor());
    }

    @Test
    public void testDisconnectGracefullyWhenNotConnected() {
        // When - 연결되지 않은 상태에서 graceful disconnect
        clientThread.disconnectGracefully();
        
        // Then - 예외 없이 통과
        assertFalse(clientThread.isConnected());
    }
}
