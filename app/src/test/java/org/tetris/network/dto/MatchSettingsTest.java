package org.tetris.network.dto;

import org.junit.Test;
import static org.junit.Assert.*;

public class MatchSettingsTest {

    @Test
    public void testMatchSettingsCreationAndGetters() {
        int playerNumber = 1;
        long mySeed = 12345L;
        long otherSeed = 67890L;

        MatchSettings settings = new MatchSettings(playerNumber, mySeed, otherSeed);

        assertEquals(playerNumber, settings.getPlayerNumber());
        assertEquals(mySeed, settings.getMySeed());
        assertEquals(otherSeed, settings.getOtherSeed());
    }
}
