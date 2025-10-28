package org.tetris.game;

import org.tetris.game.model.GameModel;

import org.tetris.game.controller.GameController;

import org.tetris.shared.MvcFactory;

public class GameFactory extends MvcFactory<GameModel, GameController> {
    public GameFactory() {
        super( () -> new GameModel(), model -> new GameController(model), "view/game.fxml");
    }

}