package org.tetris.menu.setting;

import org.tetris.menu.setting.model.Setting;
import org.tetris.menu.setting.controller.SettingMenuController;
import org.tetris.menu.setting.model.SettingMenuModel;
import org.tetris.shared.MvcFactory;

public class SettingMenuFactory extends MvcFactory<SettingMenuModel, SettingMenuController> {
    public SettingMenuFactory(Setting setting) {
        super(() -> new SettingMenuModel(setting), model -> new SettingMenuController(model), "view/settingmenu.fxml");
    }
}
