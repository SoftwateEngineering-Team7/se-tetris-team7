package org.tetris.network.comand;

/**
 * 보드 상태 동기화를 위한 커맨드.
 * lockCurrentBlock 시점에 보드 상태와 블록 생성 개수를 상대방에게 전송하여 동기화합니다.
 */
public class BoardSyncCommand implements GameCommand {
    private static final long serialVersionUID = 2L;
    
    private final int[][] boardState;
    private final int blockCount;  // 현재까지 생성된 블록 개수
    
    public BoardSyncCommand(int[][] boardState, int blockCount) {
        // 깊은 복사로 전달
        this.boardState = deepCopy(boardState);
        this.blockCount = blockCount;
    }
    
    public int[][] getBoardState() {
        return boardState;
    }
    
    public int getBlockCount() {
        return blockCount;
    }
    
    @Override
    public void execute(GameCommandExecutor executor) {
        executor.syncBoard(boardState, blockCount);
    }
    
    private int[][] deepCopy(int[][] original) {
        if (original == null) return null;
        int[][] copy = new int[original.length][];
        for (int i = 0; i < original.length; i++) {
            copy[i] = original[i].clone();
        }
        return copy;
    }
}
