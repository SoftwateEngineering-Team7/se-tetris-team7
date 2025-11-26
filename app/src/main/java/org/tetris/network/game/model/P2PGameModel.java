package org.tetris.network.game.model;

import org.tetris.game.model.DualGameModel;
import org.tetris.game.model.GameModel;

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

    @Override
    public void reset() {
        super.reset(); // Resets both player1 and player2 models
    }
}
