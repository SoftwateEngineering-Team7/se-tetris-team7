package org.tetris.game.model;

/**
 * P2P 게임 모델
 * 서버로부터 받은 playerNumber에 따라 로컬/원격 플레이어를 동적으로 결정합니다.
 */
public class P2PGameModel extends DualGameModel {

    private int playerNumber = 1; // 기본값: Player 1

    public P2PGameModel() {
        super();
    }

    /**
     * 이 클라이언트의 플레이어 번호를 설정 (1 또는 2)
     */
    public void setPlayerNumber(int playerNumber) {
        this.playerNumber = playerNumber;
    }

    public int getPlayerNumber() {
        return playerNumber;
    }

    /**
     * 로컬 플레이어의 게임 모델 반환
     * playerNumber가 1이면 Player1, 2이면 Player2
     */
    public GameModel getLocalGameModel() {
        return playerNumber == 1 ? getPlayer1GameModel() : getPlayer2GameModel();
    }

    /**
     * 원격 플레이어의 게임 모델 반환
     * playerNumber가 1이면 Player2, 2이면 Player1
     */
    public GameModel getRemoteGameModel() {
        return playerNumber == 1 ? getPlayer2GameModel() : getPlayer1GameModel();
    }

    /**
     * 시드 설정
     * @param localSeed 로컬 플레이어의 시드
     * @param remoteSeed 원격 플레이어의 시드
     */
    public void setNextBlockSeed(long localSeed, long remoteSeed) {
        getLocalGameModel().setNextBlockSeed(localSeed);
        getRemoteGameModel().setNextBlockSeed(remoteSeed);
    }
}
