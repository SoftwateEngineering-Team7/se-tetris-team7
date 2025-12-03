package org.tetris.game.model;

import org.junit.Test;
import org.tetris.network.comand.InputCommand;
import org.tetris.network.dto.MatchSettings;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class ReplayDataTest {

    @Test
    public void testReplayDataCreation() {
        // Given
        MatchSettings settings = new MatchSettings(1, 12345L, 67890L, GameMode.NORMAL, "Normal");
        List<InputCommand> p1Inputs = new ArrayList<>();
        p1Inputs.add(new InputCommand(1, 1, "moveLeft", 0L));
        List<InputCommand> p2Inputs = new ArrayList<>();
        p2Inputs.add(new InputCommand(2, 1, "moveRight", 0L));
        long duration = 10000L;
        int scoreP1 = 1000;
        int scoreP2 = 500;
        boolean p1Won = true;

        // When
        ReplayData data = new ReplayData(settings, p1Inputs, p2Inputs, duration, scoreP1, scoreP2, p1Won);

        // Then
        assertEquals(settings, data.getInitialSettings());
        assertEquals(1, data.getPlayer1Inputs().size());
        assertEquals(1, data.getPlayer2Inputs().size());
        assertEquals(duration, data.getGameDurationMs());
        assertEquals(scoreP1, data.getFinalScoreP1());
        assertEquals(scoreP2, data.getFinalScoreP2());
        assertTrue(data.isPlayer1Won());
    }

    @Test
    public void testDefensiveCopyingInConstructor() {
        // Given
        List<InputCommand> p1Inputs = new ArrayList<>();
        p1Inputs.add(new InputCommand(1, 1, "moveLeft", 0L));
        List<InputCommand> p2Inputs = new ArrayList<>();

        // When
        ReplayData data = new ReplayData(new MatchSettings(1, 1L, 2L, GameMode.NORMAL, "Normal"), p1Inputs, p2Inputs, 100L, 0, 0, true);
        
        // Modify original list
        p1Inputs.clear();

        // Then
        assertEquals(1, data.getPlayer1Inputs().size());
    }

    @Test
    public void testDefensiveCopyingInGetter() {
        // Given
        List<InputCommand> p1Inputs = new ArrayList<>();
        p1Inputs.add(new InputCommand(1, 1, "moveLeft", 0L));
        List<InputCommand> p2Inputs = new ArrayList<>();
        ReplayData data = new ReplayData(new MatchSettings(1, 1L, 2L, GameMode.NORMAL, "Normal"), p1Inputs, p2Inputs, 100L, 0, 0, true);

        // When
        List<InputCommand> retrievedInputs = data.getPlayer1Inputs();
        retrievedInputs.clear();

        // Then
        assertEquals(1, data.getPlayer1Inputs().size());
    }

    @Test
    public void testIsValid() {
        MatchSettings settings = new MatchSettings(1, 1L, 2L, GameMode.NORMAL, "Normal");
        List<InputCommand> inputs = new ArrayList<>();
        inputs.add(new InputCommand(1, 1, "moveLeft", 0L));

        // Valid case
        ReplayData validData = new ReplayData(settings, inputs, inputs, 100L, 0, 0, true);
        assertTrue(validData.isValid());

        // Invalid: null settings
        ReplayData nullSettings = new ReplayData(null, inputs, inputs, 100L, 0, 0, true);
        assertFalse(nullSettings.isValid());

        // Invalid: null inputs
        ReplayData nullInputs = new ReplayData(settings, null, inputs, 100L, 0, 0, true);
        assertFalse(nullInputs.isValid());

        // Invalid: negative duration
        ReplayData negativeDuration = new ReplayData(settings, inputs, inputs, -1L, 0, 0, true);
        assertFalse(negativeDuration.isValid());

        // Invalid: empty inputs (both empty)
        ReplayData emptyInputs = new ReplayData(settings, new ArrayList<>(), new ArrayList<>(), 100L, 0, 0, true);
        assertFalse(emptyInputs.isValid());
    }
}
