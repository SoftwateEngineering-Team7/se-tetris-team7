package org.tetris.game;

import org.tetris.game.controller.DualGameController;
import org.tetris.game.model.DualGameModel;
import org.tetris.shared.MvcFactory;

public class DualGameFactory extends MvcFactory<DualGameModel, DualGameController> {
    public DualGameFactory() {
        super(() -> new DualGameModel(), model -> new DualGameController(model), "view/dual_game.fxml");
    }

}
