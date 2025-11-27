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

    public void setNextBlockSeed(long p1Seed, long p2Seed) {
        getPlayer1GameModel().setNextBlockSeed(p1Seed);
        getPlayer2GameModel().setNextBlockSeed(p2Seed);
    }
}
