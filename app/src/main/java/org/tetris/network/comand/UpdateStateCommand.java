package org.tetris.network.comand;

/**
 * 블록이 고정(Lock)될 때, 보드 상태와 점수를 동기화하기 위한 커맨드.
 * 낙관적 업데이트(Optimistic Update) 후 상태 보정(Correction) 역할을 합니다.
 */
public class UpdateStateCommand implements GameCommand {
    private static final long serialVersionUID = 1L;
    
    private final int[][] board;
    private final int score;

    public UpdateStateCommand(int[][] board, int score) {
        // 배열 깊은 복사 (Deep Copy)하여 전송 시점의 상태 보존
        this.board = new int[board.length][];
        for (int i = 0; i < board.length; i++) {
            this.board[i] = board[i].clone();
        }
        this.score = score;
    }

    @Override
    public void execute(GameCommandExecutor executor) {
        executor.updateState(board, score);
    }

    public int[][] getBoard() {
        return board;
    }

    public int getScore() {
        return score;
    }
}
