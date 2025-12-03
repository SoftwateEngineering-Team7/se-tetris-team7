package org.tetris.network.mocks;

import org.tetris.network.comand.GameMenuCommandExecutor;
import org.tetris.network.dto.MatchSettings;

import java.util.ArrayList;
import java.util.List;

public class TestGameMenuCommandExecutor implements GameMenuCommandExecutor {
    public List<String> executedCommands = new ArrayList<>();
    public boolean lastIsReady;
    public long lastPing;
    public MatchSettings lastSettings;
    public boolean lastOpponentConnected;

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
    public void gameStart(MatchSettings settings) {
        executedCommands.add("gameStart");
        this.lastSettings = settings;
    }

    @Override
    public void onPlayerConnectionChanged(boolean opponentConnected) {
        executedCommands.add("onPlayerConnectionChanged");
        this.lastOpponentConnected = opponentConnected;
    }
}
