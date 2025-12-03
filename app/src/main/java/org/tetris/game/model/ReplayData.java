package org.tetris.game.model;

import org.tetris.network.comand.InputCommand;
import org.tetris.network.dto.MatchSettings;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Replay 재생을 위한 데이터 모델
 * 게임의 초기 상태와 모든 입력 로그를 포함합니다.
 * 
 * 주의: P2P 게임에서는 각 클라이언트의 시점이 다릅니다.
 * - myInputs: 해당 클라이언트(나)의 입력 로그
 * - opponentInputs: 상대방의 입력 로그
 * - myScore/opponentScore: 각각 나와 상대의 최종 점수
 * - meWon: 내가 이겼는지 여부
 */
public class ReplayData implements Serializable {
    private static final long serialVersionUID = 2L; // 필드 변경으로 버전 업

    private final MatchSettings initialSettings; // 초기 게임 설정 (seed 포함)
    private final List<InputCommand> myInputs; // 나의 모든 입력
    private final List<InputCommand> opponentInputs; // 상대방의 모든 입력
    private final long gameDurationMs; // 게임 총 시간 (밀리초)
    private final int myFinalScore; // 나의 최종 점수
    private final int opponentFinalScore; // 상대방의 최종 점수
    private final boolean meWon; // 내가 이겼는지

    public ReplayData(MatchSettings initialSettings,
                      List<InputCommand> myInputs,
                      List<InputCommand> opponentInputs,
                      long gameDurationMs,
                      int myFinalScore,
                      int opponentFinalScore,
                      boolean meWon) {
        this.initialSettings = initialSettings;
        // 방어적 복사
        this.myInputs = myInputs != null ? new ArrayList<>(myInputs) : null;
        this.opponentInputs = opponentInputs != null ? new ArrayList<>(opponentInputs) : null;
        this.gameDurationMs = gameDurationMs;
        this.myFinalScore = myFinalScore;
        this.opponentFinalScore = opponentFinalScore;
        this.meWon = meWon;
    }

    /**
     * Replay 데이터가 유효한지 검증
     * @return 유효하면 true, 아니면 false
     */
    public boolean isValid() {
        if (initialSettings == null) {
            return false;
        }
        if (myInputs == null || opponentInputs == null) {
            return false;
        }
        if (gameDurationMs < 0) {
            return false;
        }
        // 최소한 한 입력은 있어야 함
        return !myInputs.isEmpty() || !opponentInputs.isEmpty();
    }

    // Getters
    public MatchSettings getInitialSettings() {
        return initialSettings;
    }

    /**
     * 나의 입력 로그 반환 (Replay 재생 시 player1에 적용)
     */
    public List<InputCommand> getMyInputs() {
        return new ArrayList<>(myInputs); // 방어적 복사
    }

    /**
     * 상대방의 입력 로그 반환 (Replay 재생 시 player2에 적용)
     */
    public List<InputCommand> getOpponentInputs() {
        return new ArrayList<>(opponentInputs); // 방어적 복사
    }
    
    // 하위 호환성을 위한 별칭 메서드
    public List<InputCommand> getPlayer1Inputs() {
        return getMyInputs();
    }
    
    public List<InputCommand> getPlayer2Inputs() {
        return getOpponentInputs();
    }

    public long getGameDurationMs() {
        return gameDurationMs;
    }

    public int getMyFinalScore() {
        return myFinalScore;
    }

    public int getOpponentFinalScore() {
        return opponentFinalScore;
    }
    
    // 하위 호환성을 위한 별칭 메서드
    public int getFinalScoreP1() {
        return myFinalScore;
    }
    
    public int getFinalScoreP2() {
        return opponentFinalScore;
    }

    public boolean isMeWon() {
        return meWon;
    }
    
    // 하위 호환성을 위한 별칭 메서드
    public boolean isPlayer1Won() {
        return meWon;
    }

    @Override
    public String toString() {
        return "ReplayData{" +
                "duration=" + gameDurationMs + "ms" +
                ", myInputs=" + myInputs.size() +
                ", opponentInputs=" + opponentInputs.size() +
                ", myScore=" + myFinalScore +
                ", opponentScore=" + opponentFinalScore +
                ", meWon=" + meWon +
                '}';
    }
}
