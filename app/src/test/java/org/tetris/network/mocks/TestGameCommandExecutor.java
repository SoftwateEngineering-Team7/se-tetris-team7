package org.tetris.network.mocks;

import org.tetris.game.model.blocks.Block;
import org.tetris.network.comand.GameCommandExecutor;
import org.tetris.network.comand.InputCommand;
import org.tetris.network.comand.SnapshotCommand;
import org.tetris.network.dto.MatchSettings;
import org.util.Point;

import java.util.ArrayList;
import java.util.List;

public class TestGameCommandExecutor implements GameCommandExecutor {
    public List<String> executedCommands = new ArrayList<>();
    public MatchSettings lastSettings;
    public int lastScore;
    public boolean lastIsWinner;
    public int lastAttackLines;
    public int[][] lastBoardState;
    public int lastStateScore;
    public long lastPing;
    public boolean isPaused;
    public String lastDisconnectReason;
    public Block lastCurrentBlock;
    public Point lastCurrentPos;

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
    public void pause() {
        executedCommands.add("pause");
        this.isPaused = true;
    }

    @Override
    public void resume() {
        executedCommands.add("resume");
        this.isPaused = false;
    }

    @Override
    public void onOpponentDisconnect(String reason) {
        executedCommands.add("onOpponentDisconnect");
        this.lastDisconnectReason = reason;
    }

    @Override
    public void attack(int lines) {
        executedCommands.add("attack");
        this.lastAttackLines = lines;
    }

    @Override
    public void updateState(int[][] board, int currentPosRow, int currentPosCol, int score) {
        executedCommands.add("updateState");
        this.lastBoardState = board;
        this.lastCurrentPos = new Point(currentPosRow, currentPosCol);
        this.lastStateScore = score;
    }

    @Override
    public void updatePing(long ping) {
        executedCommands.add("updatePing");
        this.lastPing = ping;
    }

    @Override
    public void updateOpponentPing(long ping) {
        executedCommands.add("updateOpponentPing");
        this.lastPing = ping;
    }

    // 시퀀싱 시스템 메서드
    public InputCommand lastInputCommand;
    public SnapshotCommand lastSnapshotCommand;

    @Override
    public void executeInput(InputCommand cmd) {
        executedCommands.add("executeInput");
        this.lastInputCommand = cmd;
    }

    @Override
    public void restoreSnapshot(SnapshotCommand snapshot) {
        executedCommands.add("restoreSnapshot");
        this.lastSnapshotCommand = snapshot;
    }
}
