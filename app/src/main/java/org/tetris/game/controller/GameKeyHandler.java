package org.tetris.game.controller;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import org.tetris.network.game.GameEngine;
import org.tetris.network.comand.*;

import org.util.KeyLayout;

public class GameKeyHandler {
    private final GameEngine<?, ?> gameEngine;
    private final KeyLayout keyLayout;

    private GameCommand moveLeftCommand;
    private GameCommand moveRightCommand;
    private GameCommand rotateCommand;
    private GameCommand softDropCommand;
    private GameCommand hardDropCommand;
    private GameCommand pauseCommand;

    public GameKeyHandler(GameEngine<?, ?> gameEngine) {
        this(gameEngine, KeyLayout.WASD); // Default to WASD
    }

    public GameKeyHandler(GameEngine<?, ?> gameEngine, KeyLayout keyLayout) {
        this.gameEngine = gameEngine;
        this.keyLayout = keyLayout;
        this.moveLeftCommand = new MoveLeftCommand();
        this.moveRightCommand = new MoveRightCommand();
        this.rotateCommand = new RotateCommand();
        this.softDropCommand = new SoftDropCommand();
        this.hardDropCommand = new HardDropCommand();
        this.pauseCommand = new TogglePauseCommand();
    }

    // === Command Setters ===
    public void setMoveLeftCommand(GameCommand command) {
        this.moveLeftCommand = command;
    }

    public void setMoveRightCommand(GameCommand command) {
        this.moveRightCommand = command;
    }

    public void setRotateCommand(GameCommand command) {
        this.rotateCommand = command;
    }

    public void setSoftDropCommand(GameCommand command) {
        this.softDropCommand = command;
    }

    public void setHardDropCommand(GameCommand command) {
        this.hardDropCommand = command;
    }

    public void setPauseCommand(GameCommand command) {
        this.pauseCommand = command;
    }

    // === 키 입력 처리 ===
    public void handleKeyPress(KeyEvent e) {
        KeyCode code = e.getCode();

        // 전체 공통: P키로 Pause 토글 (Default) - Can be customized if needed, but P is
        // standard
        if (code == KeyCode.P) {
            pauseCommand.execute(gameEngine);
            e.consume();
            return;
        }

        if (gameEngine.isPaused()) {
            e.consume();
            return;
        }

        if (code == keyLayout.getLeft()) {
            moveLeftCommand.execute(gameEngine);
            e.consume();
            return;
        }
        if (code == keyLayout.getRight()) {
            moveRightCommand.execute(gameEngine);
            e.consume();
            return;
        }
        if (code == keyLayout.getUp()) {
            rotateCommand.execute(gameEngine);
            e.consume();
            return;
        }
        if (code == keyLayout.getDown()) {
            softDropCommand.execute(gameEngine);
            e.consume();
            return;
        }
        if (code == KeyCode.SPACE) { // HardDrop default to SPACE, maybe add to KeyLayout?
            hardDropCommand.execute(gameEngine);
            e.consume();
            return;
        }
        // Support Slash for P2 HardDrop if needed, or add hardDropKey to KeyLayout
    }
}
