package org.tetris.game;

import org.tetris.game.model.Board;

import org.tetris.game.controller.GameController;

import org.tetris.shared.MvcFactory;

public class GameFactory extends MvcFactory<Board, GameController> {
    public GameFactory() {
        super( () -> new Board(), model -> new GameController(model), "view/game.fxml");
    }

}