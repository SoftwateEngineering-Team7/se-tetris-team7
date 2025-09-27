package org.tetris.menu.start.view;

import org.tetris.menu.start.controller.StartMenuController;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

public class StartMenuView {
    private final Parent root;
    private final StartMenuController controller;

    public StartMenuView() {
        try {
            var loader = new FXMLLoader(
                    getClass().getResource("/fxml/startmenu.fxml"));
            this.root = loader.load();
            this.controller = loader.getController(); // FXML의 controller 인스턴스
        } catch (Exception e) {
            throw new RuntimeException("Failed to load start_menu.fxml", e);
        }
    }

    public Parent getRoot() {
        return root;
    }

    public StartMenuController getController() {
        return controller;
    }
}