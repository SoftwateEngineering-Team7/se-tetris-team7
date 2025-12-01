package org.tetris.network.comand;

import org.junit.Test;
import org.tetris.game.model.GameMode;
import org.tetris.network.dto.MatchSettings;
import org.tetris.network.mocks.TestGameMenuCommandExecutor;

import static org.junit.Assert.*;

public class GameMenuCommandTest {

    @Test
    public void testReadyCommand() {
        TestGameMenuCommandExecutor executor = new TestGameMenuCommandExecutor();
        boolean isReady = true;

        ReadyCommand command = new ReadyCommand(isReady);
        command.execute(executor);

        assertTrue(executor.executedCommands.contains("onReady"));
        assertTrue(executor.lastIsReady);
    }

    @Test
    public void testReadyCommandFalse() {
        TestGameMenuCommandExecutor executor = new TestGameMenuCommandExecutor();

        ReadyCommand command = new ReadyCommand(false);
        command.execute(executor);

        assertTrue(executor.executedCommands.contains("onReady"));
        assertFalse(executor.lastIsReady);
    }

    @Test
    public void testGameStartCommand() {
        TestGameMenuCommandExecutor executor = new TestGameMenuCommandExecutor();
        MatchSettings settings = new MatchSettings(1, 12345L, 67890L, GameMode.ITEM, "HARD");

        GameStartCommand command = new GameStartCommand(settings);
        command.execute(executor);

        assertTrue(executor.executedCommands.contains("gameStart"));
        assertEquals(settings, executor.lastSettings);
    }

    @Test
    public void testGameStartCommandPassesAllSettings() {
        TestGameMenuCommandExecutor executor = new TestGameMenuCommandExecutor();
        MatchSettings settings = new MatchSettings(2, 111L, 222L, GameMode.TIME_ATTACK, "NORMAL");

        GameStartCommand command = new GameStartCommand(settings);
        command.execute(executor);

        assertNotNull(executor.lastSettings);
        assertEquals(2, executor.lastSettings.getPlayerNumber());
        assertEquals(111L, executor.lastSettings.getMySeed());
        assertEquals(222L, executor.lastSettings.getOtherSeed());
        assertEquals(GameMode.TIME_ATTACK, executor.lastSettings.getGameMode());
        assertEquals("NORMAL", executor.lastSettings.getDifficulty());
    }
}
