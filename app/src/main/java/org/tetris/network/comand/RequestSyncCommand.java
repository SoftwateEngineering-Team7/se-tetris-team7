package org.tetris.network.comand;

/**
 * 서버에 현재 Ready 상태 동기화를 요청하는 커맨드.
 * 게임 화면에서 메뉴로 돌아올 때 클라이언트가 서버에 요청합니다.
 */
public class RequestSyncCommand implements Command {
    
    public RequestSyncCommand() {
    }
}
