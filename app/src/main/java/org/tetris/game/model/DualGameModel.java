package org.tetris.game.model;

import org.tetris.shared.BaseModel;

public class DualGameModel extends BaseModel {
    private GameModel player1GameModel;
    private GameModel player2GameModel;

    public DualGameModel() {
        this.player1GameModel = new GameModel();
        this.player2GameModel = new GameModel();
    }

    public GameModel getPlayer1GameModel() {
        return player1GameModel;
    }

    public GameModel getPlayer2GameModel() {
        return player2GameModel;
    }
}
