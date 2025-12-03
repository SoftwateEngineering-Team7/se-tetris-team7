package org.tetris.game;

import org.tetris.game.controller.ReplayController;
import org.tetris.game.model.DualGameModel;
import org.tetris.shared.MvcFactory;

public class ReplayGameFactory extends MvcFactory<DualGameModel, ReplayController> {
    public ReplayGameFactory() {
        super(() -> new DualGameModel(), model -> new ReplayController(model), "view/replay_game.fxml");
    }
}
