package org.tetris.menu.start.controller;

import org.tetris.menu.start.model.StartMenuModel;
import org.tetris.menu.start.view.StartMenuView;

import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class StartMenuController {
    private final StartMenuModel model;
    private final StartMenuView view;
    private final UIRouter router;

    public StartMenuController(StartMenuModel model, StartMenuView view, UIRouter router) {
        this.model = model;
        this.view = view;
        this.router = router;

        setView();
    }

    private void setView() {
        // Text Set
        view.setTitleText(model.getTitleText());
        view.setGameStartButtonText(model.getGameStartButtonText());
        view.setSettingButtonText(model.getSettingButtonText());
        view.setExitButtonText(model.getExitButtonText());

        // Font Set
        view.setTitleFont(model.getTitleFont());
        view.setButtonFont(model.getButtonFont());
        view.setTextColor(model.getTextColor());

        // Size Set
        view.setTitleSize((int) model.getTitleSize().getWidth(), (int) model.getTitleSize().getHeight());
        view.setButtonSize((int) model.getButtonSize().getWidth(), (int) model.getButtonSize().getHeight());

        view.setActionHandlers(
                this::gameStart,
                this::openSettings,
                this::exitGame);
    }

    public void bindInput(Scene scene) {
        scene.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.UP || e.getCode() == KeyCode.DOWN ||
                    e.getCode() == KeyCode.SPACE || e.getCode() == KeyCode.ENTER) {
                e.consume();
            } // 기본 버튼 처리(자동 fire) 차단

            if (e.getCode() == KeyCode.UP) {
                view.setFocusButton(-1);
            } else if (e.getCode() == KeyCode.DOWN) {
                view.setFocusButton(1);
            } else if (e.getCode() == KeyCode.SPACE || e.getCode() == KeyCode.ENTER) {
                view.buttonFire(); // 키로만 실행
            }
        });
    };

    private void gameStart() {
        router.showGamePlaceholder();
    }

    private void openSettings() {
        router.showSettings();
    }

    private void exitGame() {
        router.exitGame();
    }
}