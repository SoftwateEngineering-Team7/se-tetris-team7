package org.tetris.menu.start;

import org.tetris.menu.start.controller.StartMenuController;
import org.tetris.menu.start.model.StartMenuModel;
import org.tetris.shared.MvcFactory;

public class StartMenuFactory extends MvcFactory<StartMenuModel, StartMenuController> {
    public StartMenuFactory() {
        super(StartMenuModel::new, StartMenuController::new, "view/startmenu.fxml");
    }
}
