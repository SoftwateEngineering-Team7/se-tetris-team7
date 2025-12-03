package org.tetris.network.menu;

import org.tetris.network.menu.controller.NetworkMenuController;
import org.tetris.network.menu.model.NetworkMenu;
import org.tetris.shared.MvcFactory;

public class NetworkMenuFactory extends MvcFactory<NetworkMenu, NetworkMenuController> {
    public NetworkMenuFactory() {
        super( () -> new NetworkMenu(), model -> new NetworkMenuController(model), "view/NetworkMenu.fxml");
    }
}
