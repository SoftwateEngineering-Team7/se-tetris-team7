package org.tetris.network.comand;

import org.tetris.network.dto.MatchSettings;

public class GameStartCommand implements GameCommand, GameMenuCommand {
    private final MatchSettings settings;

    public GameStartCommand(MatchSettings settings) {
        this.settings = settings;
    }

    @Override
    public void execute(GameCommandExecutor executor) {
        executor.gameStart(settings);
    }

    @Override
    public void execute(GameMenuCommandExecutor executor) {
        executor.gameStart(settings);
    }

}
