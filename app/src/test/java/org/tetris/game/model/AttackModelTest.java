package org.tetris.game.model;

import static org.junit.Assert.*;

import org.junit.Test;


public class AttackModelTest {
    
    @Test
    public void testAttackModelPush()
    {
        AttackModel attackModel = new AttackModel();

        for (int i = 0; i < 12; i++) {
            attackModel.push(new int[]{i, i + 1});
        }

        assertEquals(10, attackModel.getAttacks().size());
        assertArrayEquals(new int[]{2, 3}, attackModel.getAttacks().get(0));
        assertArrayEquals(new int[]{11, 12}, attackModel.getAttacks().get(9));
    }

    @Test
    public void testAttackModelPop()
    {
        AttackModel attackModel = new AttackModel();

        attackModel.push(new int[]{1, 2});
        attackModel.push(new int[]{3, 4});

        int[] attack1 = attackModel.pop();
        assertArrayEquals(new int[]{1, 2}, attack1);
        assertEquals(1, attackModel.getAttacks().size());

        int[] attack2 = attackModel.pop();
        assertArrayEquals(new int[]{3, 4}, attack2);
        assertEquals(0, attackModel.getAttacks().size());
    }

    @Test
    public void testAttackModeltoString()
    {
        AttackModel attackModel = new AttackModel();

        attackModel.push(new int[]{1, 2});
        attackModel.push(new int[]{3, 4});

        String expectedString = 
            "1 2 \n" +
            "3 4 \n";

        System.out.println(attackModel.toString());
        assertEquals(expectedString, attackModel.toString());
    }

    @Test
    public void testAttackOnBoard()
    {
        AttackModel attackModel = new AttackModel();
        Board board = new Board(5, 5);

        attackModel.push(new int[]{1, 0, 1, 1, 1});
        attackModel.push(new int[]{1, 0, 1, 1, 1});
        attackModel.push(new int[]{1, 0, 1, 1, 1});
        attackModel.push(new int[]{1, 0, 1, 1, 1});

        var attack = attackModel.pop();
        while (attack != null) {
            board.pushUp(attack);
            attack = attackModel.pop();
        }

        assertEquals("", attackModel.toString());
        assertEquals(0, attackModel.getAttacks().size());

        String expectedBoard = 
            "0 0 0 0 0 \n" +
            "1 0 1 1 1 \n" +
            "1 0 1 1 1 \n" +
            "1 0 1 1 1 \n" +
            "1 0 1 1 1 \n";
        
        System.out.println("Board after attacks:\n" + board.toString());
        assertEquals(expectedBoard, board.toString());
    }
}
