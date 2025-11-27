package org.tetris.network.mocks;

import org.tetris.network.comand.GameMenuCommandExecutor;

import java.util.ArrayList;
import java.util.List;

public class TestGameMenuCommandExecutor implements GameMenuCommandExecutor {
    public List<String> executedCommands = new ArrayList<>();
    public boolean lastIsReady;
    public long lastPing;

    @Override
    public void onReady(boolean isReady) {
        executedCommands.add("onReady");
        this.lastIsReady = isReady;
    }

    @Override
    public void updatePing(long ping) {
        executedCommands.add("updatePing");
        this.lastPing = ping;
    }

    @Override
    public void gameStart() {
        executedCommands.add("gameStart");
    }
}
