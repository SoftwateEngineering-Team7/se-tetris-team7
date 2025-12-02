package org.tetris.network.dto;

import org.junit.Test;
import org.tetris.game.model.GameMode;

import static org.junit.Assert.*;

public class MatchSettingsTest {

    @Test
    public void testMatchSettingsCreationAndGetters() {
        int playerNumber = 1;
        long mySeed = 12345L;
        long otherSeed = 67890L;
        GameMode gameMode = GameMode.ITEM;
        String difficulty = "HARD";

        MatchSettings settings = new MatchSettings(playerNumber, mySeed, otherSeed, gameMode, difficulty);

        assertEquals(playerNumber, settings.getPlayerNumber());
        assertEquals(mySeed, settings.getMySeed());
        assertEquals(otherSeed, settings.getOtherSeed());
        assertEquals(gameMode, settings.getGameMode());
        assertEquals(difficulty, settings.getDifficulty());
    }

    @Test
    public void testMatchSettingsWithNormalMode() {
        MatchSettings settings = new MatchSettings(1, 100L, 200L, GameMode.NORMAL, "EASY");

        assertEquals(1, settings.getPlayerNumber());
        assertEquals(GameMode.NORMAL, settings.getGameMode());
        assertEquals("EASY", settings.getDifficulty());
    }

    @Test
    public void testMatchSettingsWithTimeAttackMode() {
        MatchSettings settings = new MatchSettings(2, 100L, 200L, GameMode.TIME_ATTACK, "NORMAL");

        assertEquals(2, settings.getPlayerNumber());
        assertEquals(GameMode.TIME_ATTACK, settings.getGameMode());
        assertEquals("NORMAL", settings.getDifficulty());
    }
}
