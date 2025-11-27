package org.tetris.network.comand;

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
    void gameStart(long seed);

    void gameOver(int score);

    void onGameResult(boolean isWinner, int score);

    // 공격
    void attack(int lines);

    // 기타
    void updateState(String state);

    void updatePing(long ping);
}
