package org.tetris.network.comand;

import org.tetris.game.model.blocks.Block;
import org.tetris.network.dto.MatchSettings;
import org.util.Point;

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

    void gameOver(int score);

    void onGameResult(boolean isWinner, int score);

    // 일시정지
    void pause();

    void resume();

    // 연결 끊김 처리
    void onOpponentDisconnect(String reason);

    // 공격
    void attack(int lines);

    // 기타
    void updateState(int[][] board, int currentPosRow, int currentPosCol);

    void updatePing(long ping);

    void updateOpponentPing(long ping);
}
