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

    // 시퀀스 기반 입력 처리
    /**
     * 시퀀스가 포함된 입력 커맨드를 실행합니다.
     * @param cmd globalSeq가 설정된 InputCommand
     */
    void executeInput(InputCommand cmd);

    /**
     * 스냅샷으로 상태를 복원하고, 이후 입력들을 재적용합니다.
     * @param snapshot 서버에서 받은 스냅샷
     */
    void restoreSnapshot(SnapshotCommand snapshot);
}
