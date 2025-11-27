package org.tetris.network.mocks;

import org.tetris.network.comand.GameCommandExecutor;
import org.tetris.network.dto.MatchSettings;

import java.util.ArrayList;
import java.util.List;

public class TestGameCommandExecutor implements GameCommandExecutor {
    public List<String> executedCommands = new ArrayList<>();
    public MatchSettings lastSettings;
    public int lastScore;
    public boolean lastIsWinner;
    public int lastAttackLines;
    public String lastState;
    public long lastPing;

    @Override
    public void moveLeft() {
        executedCommands.add("moveLeft");
    }

    @Override
    public void moveRight() {
        executedCommands.add("moveRight");
    }

    @Override
    public void rotate() {
        executedCommands.add("rotate");
    }

    @Override
    public void softDrop() {
        executedCommands.add("softDrop");
    }

    @Override
    public void hardDrop() {
        executedCommands.add("hardDrop");
    }

    @Override
    public void gameStart(MatchSettings settings) {
        executedCommands.add("gameStart");
        this.lastSettings = settings;
    }

    @Override
    public void gameOver(int score) {
        executedCommands.add("gameOver");
        this.lastScore = score;
    }

    @Override
    public void onGameResult(boolean isWinner, int score) {
        executedCommands.add("onGameResult");
        this.lastIsWinner = isWinner;
        this.lastScore = score;
    }

    @Override
    public void attack(int lines) {
        executedCommands.add("attack");
        this.lastAttackLines = lines;
    }

    @Override
    public void updateState(String state) {
        executedCommands.add("updateState");
        this.lastState = state;
    }

    @Override
    public void updatePing(long ping) {
        executedCommands.add("updatePing");
        this.lastPing = ping;
    }
}
