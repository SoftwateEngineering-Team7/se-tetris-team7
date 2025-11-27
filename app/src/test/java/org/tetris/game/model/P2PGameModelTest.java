package org.tetris.game.model;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class P2PGameModelTest {

    private P2PGameModel p2pGameModel;

    @Before
    public void setUp() {
        p2pGameModel = new P2PGameModel();
    }

    @Test
    public void testInitialization() {
        assertNotNull(p2pGameModel.getLocalGameModel());
        assertNotNull(p2pGameModel.getRemoteGameModel());
        assertNotEquals(p2pGameModel.getLocalGameModel(), p2pGameModel.getRemoteGameModel());
    }

    @Test
    public void testSetNextBlockSeed() {
        long p1Seed = 11111L;
        long p2Seed = 22222L;

        p2pGameModel.setNextBlockSeed(p1Seed, p2Seed);

        assertEquals(p1Seed, p2pGameModel.getLocalGameModel().getNextBlockSeed());
        assertEquals(p2Seed, p2pGameModel.getRemoteGameModel().getNextBlockSeed());
    }
}
