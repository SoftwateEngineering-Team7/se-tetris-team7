package org.tetris.network.comand;

/**
 * 게임 재시작을 요청하는 커맨드.
 * 서버에서 새로운 seed를 생성하여 양쪽 클라이언트에 GameStartCommand를 전송합니다.
 */
public class RestartCommand implements Command {
    private static final long serialVersionUID = 1L;
    
    public RestartCommand() {
    }
}
