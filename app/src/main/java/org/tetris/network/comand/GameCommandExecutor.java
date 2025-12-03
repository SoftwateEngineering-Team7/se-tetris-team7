package org.tetris.network.comand;

import org.tetris.network.dto.MatchSettings;

/**
 * 게임 내에서 발생할 수 있는 모든 액션을 정의하는 인터페이스.
 * 네트워크 커맨드는 이 인터페이스를 통해 실제 게임 로직(Controller 등)에 명령을 전달합니다.
 */
public interface GameCommandExecutor {
    // 이동 및 조작
    void moveLeft();

    void moveRight();

    void rotate();

    void softDrop();

    void hardDrop();

    // 게임 상태
    void gameStart(MatchSettings settings);

    void gameOver(int score, java.util.List<int[]> pendingAttacks);

    void onGameResult(boolean isWinner, int score);

    // 일시정지
    void pause();

    void resume();

    // 연결 끊김 처리
    void onOpponentDisconnect(String reason);

    // 공격
    default void attack(java.util.List<int[]> attackRows) {
        // 기본 구현: 아무 동작도 하지 않음 (서브클래스에서 오버라이드)
    }

    // 기타
    void updateState(String state);

    void updatePing(long ping);

    void updateOpponentPing(long ping);

    // 보드 동기화 (P2P)
    void syncBoard(int[][] boardState, int blockCount);
}
