package org.util;

import static org.junit.Assert.*;

import org.junit.Test;

import javafx.scene.paint.Color;

public class GameColorTest {
    
    @Test
    public void testGetColor() {
        GameColor color1 = GameColor.RED;
        assertEquals(color1.getColor(), Color.RED);
        
        GameColor.setColorBlind(true);
        assertEquals(color1.getColor(), Color.web("#D55E00"));

        GameColor color2 = GameColor.BLUE;
        assertEquals(color2.getColor(), Color.web("#0072B2"));
    }
}
