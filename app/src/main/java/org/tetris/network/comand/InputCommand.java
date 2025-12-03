package org.tetris.network.comand;

/**
 * 플레이어 입력을 표현하는 커맨드 (시퀀스 포함)
 * 모든 입력에 로컬 시퀀스와 전역 시퀀스를 부여하여 결정론적 시뮬레이션을 가능하게 합니다.
 */
public class InputCommand implements GameCommand {
    private static final long serialVersionUID = 1L;

    private final int playerNumber; // 1 or 2
    private final long localSeq; // 클라이언트가 부여한 로컬 시퀀스
    private long globalSeq; // 서버가 부여할 전역 시퀀스 (초기값 -1)
    private final String action; // "moveLeft", "moveRight", "rotate", "softDrop", "hardDrop"
    private final long timestamp; // 클라이언트 타임스탬프

    /**
     * InputCommand 생성자
     * @param playerNumber 플레이어 번호 (1 or 2)
     * @param localSeq 클라이언트가 부여한 로컬 시퀀스
     * @param action 입력 종류 ("moveLeft", "moveRight", "rotate", "softDrop", "hardDrop")
     */
    public InputCommand(int playerNumber, long localSeq, String action) {
        this.playerNumber = playerNumber;
        this.localSeq = localSeq;
        this.action = action;
        this.timestamp = System.currentTimeMillis();
        this.globalSeq = -1; // 서버가 설정하기 전
    }

    // Getters
    public int getPlayerNumber() {
        return playerNumber;
    }

    public long getLocalSeq() {
        return localSeq;
    }

    public long getGlobalSeq() {
        return globalSeq;
    }

    public String getAction() {
        return action;
    }

    public long getTimestamp() {
        return timestamp;
    }

    /**
     * 서버가 전역 시퀀스를 설정합니다.
     * @param globalSeq 전역 시퀀스 번호
     */
    public void setGlobalSeq(long globalSeq) {
        this.globalSeq = globalSeq;
    }

    @Override
    public void execute(GameCommandExecutor executor) {
        // GameCommandExecutor에 새로운 메서드 추가 필요
        executor.executeInput(this);
    }

    @Override
    public String toString() {
        return String.format("InputCommand[player=%d, local=%d, global=%d, action=%s, timestamp=%d]",
                playerNumber, localSeq, globalSeq, action, timestamp);
    }
}
