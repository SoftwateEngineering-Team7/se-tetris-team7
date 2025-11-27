package org.tetris.network;

import java.io.IOException;

/**
 * 네트워크 모듈을 사용하는 클라이언트 예제 클래스.
 * P2P 대전 모드에서 클라이언트 역할을 수행합니다.
 * 
 * TODO: UI 통합 - IP 주소 입력 화면 구현
 * TODO: 최근 접속 IP 저장 - 최근에 접속했던 IP를 저장하고 표시
 * TODO: 연결 상태 메시지 - "연결 중...", "연결 성공" 등 상태 표시
 * TODO: 연결 완료 알림 - 서버 연결 성공 시 UI에 알림 표시
 * TODO: 대기 상태 화면 - 게임 시작 대기 중 상태 표시
 * TODO: 연결 실패 처리 - 잘못된 IP 또는 연결 불가 시 에러 메시지
 * 
 * TODO: 게임 모드 수신 - 서버가 선택한 모드 수신 및 표시
 * TODO: 준비 상태 관리 - '게임 시작' 버튼 상태 관리
 * TODO: 준비 완료 전송 - 준비 완료 시 서버에게 알림
 */
public class GameClient {
    private static GameClient instance;
    private ClientThread clientThread;

    public static GameClient getInstance() {
        if (instance == null) {
            instance = new GameClient();
        }
        return instance;
    }

    public ClientThread getClientThread() {
        if (clientThread == null) {
            clientThread = new ClientThread();
        }
        return clientThread;
    }

    public static void main(String[] args) {
        GameClient client = getInstance();
        client.start(args);
    }

    public void start(String[] args) {
        // 2. ClientThread를 생성합니다.
        clientThread = new ClientThread();

        try {
            // TODO: UI에서 IP 주소 입력받기
            // String serverIP = getUserInputIP();
            // 3. 서버에 연결합니다. (GameServer가 먼저 실행되어 있어야 합니다)
            clientThread.connect("localhost", GameServer.PORT);
            // TODO: 연결 성공 시 UI에 알림 표시
            // TODO: 준비 상태 관리 - '게임 시작' 버튼 상태 관리
            // TODO: 준비 완료 전송 - 준비 완료 시 서버에게 알림

            // TODO: 게임 종료 시 연결 해제

        } catch (IOException e) {
            System.err.println("[CLIENT-MAIN] Connection failed: " + e.getMessage());
        } finally {
            // 7. 애플리케이션 종료 시 연결을 해제합니다.
            System.out.println("[CLIENT-MAIN] Disconnecting...");
            clientThread.disconnect();
        }
    }
}
