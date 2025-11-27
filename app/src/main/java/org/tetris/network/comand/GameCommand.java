package org.tetris.network.comand;

/**
 * 커맨드 패턴의 기반이 되는 인터페이스.
 * 모든 네트워크 요청은 이 인터페이스를 구현한 객체로 캡슐화됩니다.
 * Serializable 인터페이스를 상속받아 객체 직렬화를 지원합니다.
 */
public interface GameCommand extends Command {
    /**
     * 클라이언트 측에서 수신된 커맨드를 실행합니다.
     * 
     * @param executor 실행 대상이 되는 게임 커맨드 실행기 (GameEngine, Controller 등)
     */
    void execute(GameCommandExecutor executor);
}
