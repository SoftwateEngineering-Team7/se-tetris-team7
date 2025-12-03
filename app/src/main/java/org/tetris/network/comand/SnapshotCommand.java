package org.tetris.network.comand;

/**
 * 서버가 authoritative 상태를 전송하는 스냅샷 커맨드
 * 주기적으로 전송되어 클라이언트가 동기화 상태를 확인하고 필요 시 롤백/재적용을 수행합니다.
 */
public class SnapshotCommand implements GameCommand {
    private static final long serialVersionUID = 1L;

    private final long authoritativeSeq; // 이 스냅샷이 확정한 마지막 globalSeq
    private final int[][] player1Board;
    private final int[][] player2Board;
    private final int player1Score;
    private final int player2Score;
    private final long player1Seed; // RNG 상태 (optional)
    private final long player2Seed;

    /**
     * SnapshotCommand 생성자
     * @param authSeq 이 스냅샷이 확정한 마지막 전역 시퀀스
     * @param p1Board Player 1의 보드 상태
     * @param p2Board Player 2의 보드 상태
     * @param p1Score Player 1의 점수
     * @param p2Score Player 2의 점수
     * @param p1Seed Player 1의 RNG 시드
     * @param p2Seed Player 2의 RNG 시드
     */
    public SnapshotCommand(long authSeq, int[][] p1Board, int[][] p2Board,
                           int p1Score, int p2Score, long p1Seed, long p2Seed) {
        this.authoritativeSeq = authSeq;
        this.player1Board = deepCopy(p1Board);
        this.player2Board = deepCopy(p2Board);
        this.player1Score = p1Score;
        this.player2Score = p2Score;
        this.player1Seed = p1Seed;
        this.player2Seed = p2Seed;
    }

    /**
     * 2D 배열의 깊은 복사를 수행합니다.
     * @param src 원본 배열
     * @return 복사된 배열
     */
    private int[][] deepCopy(int[][] src) {
        if (src == null) {
            return null;
        }
        int[][] copy = new int[src.length][];
        for (int i = 0; i < src.length; i++) {
            copy[i] = src[i].clone();
        }
        return copy;
    }

    // Getters
    public long getAuthoritativeSeq() {
        return authoritativeSeq;
    }

    public int[][] getPlayer1Board() {
        return deepCopy(player1Board);
    }

    public int[][] getPlayer2Board() {
        return deepCopy(player2Board);
    }

    public int getPlayer1Score() {
        return player1Score;
    }

    public int getPlayer2Score() {
        return player2Score;
    }

    public long getPlayer1Seed() {
        return player1Seed;
    }

    public long getPlayer2Seed() {
        return player2Seed;
    }

    @Override
    public void execute(GameCommandExecutor executor) {
        executor.restoreSnapshot(this);
    }

    @Override
    public String toString() {
        return String.format("SnapshotCommand[authSeq=%d, p1Score=%d, p2Score=%d]",
                authoritativeSeq, player1Score, player2Score);
    }
}
