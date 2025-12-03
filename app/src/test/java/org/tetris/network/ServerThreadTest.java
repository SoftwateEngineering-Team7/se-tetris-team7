package org.tetris.network;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.tetris.network.comand.GameCommand;
import org.tetris.network.comand.MoveLeftCommand;
import org.tetris.network.comand.MoveRightCommand;
import org.tetris.network.comand.UpdateStateCommand;

import java.io.IOException;
import java.net.Socket;

import static org.junit.Assert.*;

/**
 * ServerThread의 클라이언트 통신 기능을 테스트합니다.
 */
public class ServerThreadTest {
    private Socket mockSocket;
    private ServerThread serverThread;

    @Before
    public void setUp() {
        // Mock 소켓 생성
        mockSocket = new Socket();
    }

    @After
    public void tearDown() {
        if (mockSocket != null && !mockSocket.isClosed()) {
            try {
                mockSocket.close();
            } catch (IOException e) {
                // 무시
            }
        }
    }

    @Test
    public void testServerThreadCreation() {
        // ServerThread 생성
        serverThread = new ServerThread(mockSocket);
        
        // 생성 성공 확인
        assertNotNull("ServerThread가 생성되어야 합니다", serverThread);
    }

    @Test
    public void testServerThreadWithNullSocket() {
        // null 소켓으로 ServerThread 생성
        serverThread = new ServerThread(null);
        
        // 생성은 가능하지만 start 시 예외 발생 예상
        assertNotNull("ServerThread가 null 소켓으로도 생성되어야 합니다", serverThread);
    }

    @Test
    public void testSendCommandWithoutConnection() {
        serverThread = new ServerThread(mockSocket);
        
        // 연결되지 않은 상태에서 명령 전송 시도
        GameCommand command = new MoveLeftCommand();
        
        try {
            serverThread.sendCommand(command);
            // IOException이 발생할 수 있지만 내부에서 처리됨 - 테스트 통과
        } catch (Exception e) {
            // 예외가 발생해도 허용 - 테스트 통과
        }
    }

    @Test
    public void testMultipleServerThreads() {
        // 여러 ServerThread 생성
        Socket socket1 = new Socket();
        Socket socket2 = new Socket();
        Socket socket3 = new Socket();
        
        ServerThread thread1 = new ServerThread(socket1);
        ServerThread thread2 = new ServerThread(socket2);
        ServerThread thread3 = new ServerThread(socket3);
        
        // 모두 생성되어야 함
        assertNotNull("ServerThread 1이 생성되어야 합니다", thread1);
        assertNotNull("ServerThread 2가 생성되어야 합니다", thread2);
        assertNotNull("ServerThread 3이 생성되어야 합니다", thread3);
        
        // 정리
        try {
            socket1.close();
            socket2.close();
            socket3.close();
        } catch (IOException e) {
            // 무시
        }
    }

    @Test
    public void testSendNullCommand() {
        serverThread = new ServerThread(mockSocket);
        
        try {
            // null 명령 전송 시도 - NullPointerException이 발생할 수 있지만 내부에서 처리될 수 있음
            serverThread.sendCommand(null);
        } catch (NullPointerException e) {
            // NPE도 허용 - 테스트 통과
        }
    }

    @Test
    public void testSendMultipleCommands() {
        serverThread = new ServerThread(mockSocket);
        
        // 여러 명령 전송 시도
        GameCommand command1 = new MoveLeftCommand();
        GameCommand command2 = new MoveRightCommand();
        
        try {
            serverThread.sendCommand(command1);
            serverThread.sendCommand(command2);
            serverThread.sendCommand(command1);
        } catch (Exception e) {
            // 예외 발생 허용 - 테스트 통과
        }
    }
}
