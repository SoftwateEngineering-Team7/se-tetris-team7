package org.tetris.game;

import org.tetris.game.controller.P2PGameController;
import org.tetris.game.model.P2PGameModel;
import org.tetris.shared.MvcFactory;

public class P2PGameFactory extends MvcFactory<P2PGameModel, P2PGameController> {
    public P2PGameFactory() {
        super(() -> new P2PGameModel(), model -> new P2PGameController(model), "view/p2p_game.fxml");
    }
}
