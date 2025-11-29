package org.tetris.game.model;

public class P2PGameModel extends DualGameModel {

    public P2PGameModel() {
        super();
    }

    public GameModel getLocalGameModel() {
        return getPlayer1GameModel();
    }

    public GameModel getRemoteGameModel() {
        return getPlayer2GameModel();
    }

    /**
     * P2P 게임에서 시드를 설정합니다.
     * @param mySeed 로컬 플레이어(내 화면)의 시드
     * @param otherSeed 원격 플레이어(상대방 화면)의 시드
     */
    public void setNextBlockSeed(long mySeed, long otherSeed) {
        getLocalGameModel().setNextBlockSeed(mySeed);
        getRemoteGameModel().setNextBlockSeed(otherSeed);
    }
}
