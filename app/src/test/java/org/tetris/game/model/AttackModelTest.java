package org.tetris.game.model;

import static org.junit.Assert.*;

import java.util.ArrayList;

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
        assertArrayEquals(new int[]{2, 3}, attackModel.getAttacks().get(2));
        assertArrayEquals(new int[]{9, 10}, attackModel.getAttacks().get(9));
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

    // ==================== popAttacks 테스트 ====================

    @Test
    public void testPopAttacks_ReturnsAllAttacks() {
        AttackModel attackModel = new AttackModel();

        attackModel.push(new int[]{1, 2, 3, 4, 5});
        attackModel.push(new int[]{6, 7, 8, 9, 0});
        attackModel.push(new int[]{8, 0, 8, 8, 8});

        ArrayList<int[]> attacks = attackModel.popAttacks();

        assertEquals("popAttacks는 모든 공격을 반환해야 합니다.", 3, attacks.size());
        assertArrayEquals(new int[]{1, 2, 3, 4, 5}, attacks.get(0));
        assertArrayEquals(new int[]{6, 7, 8, 9, 0}, attacks.get(1));
        assertArrayEquals(new int[]{8, 0, 8, 8, 8}, attacks.get(2));
    }

    @Test
    public void testPopAttacks_ClearsOriginalList() {
        AttackModel attackModel = new AttackModel();

        attackModel.push(new int[]{1, 2, 3, 4, 5});
        attackModel.push(new int[]{6, 7, 8, 9, 0});

        attackModel.popAttacks();

        assertEquals("popAttacks 후 원본 리스트는 비워져야 합니다.", 0, attackModel.getAttacks().size());
    }

    @Test
    public void testPopAttacks_EmptyModel() {
        AttackModel attackModel = new AttackModel();

        ArrayList<int[]> attacks = attackModel.popAttacks();

        assertEquals("빈 모델에서 popAttacks는 빈 리스트를 반환해야 합니다.", 0, attacks.size());
    }

    @Test
    public void testPopAttacks_ReturnsNewList() {
        AttackModel attackModel = new AttackModel();

        attackModel.push(new int[]{1, 2, 3, 4, 5});

        ArrayList<int[]> attacks1 = attackModel.popAttacks();
        
        // 새로운 공격 추가
        attackModel.push(new int[]{9, 9, 9, 9, 9});
        
        ArrayList<int[]> attacks2 = attackModel.popAttacks();

        // 첫 번째 반환된 리스트는 영향받지 않아야 함
        assertEquals(1, attacks1.size());
        assertEquals(1, attacks2.size());
        assertArrayEquals(new int[]{1, 2, 3, 4, 5}, attacks1.get(0));
        assertArrayEquals(new int[]{9, 9, 9, 9, 9}, attacks2.get(0));
    }

    // ==================== reset 테스트 ====================

    @Test
    public void testReset() {
        AttackModel attackModel = new AttackModel();

        attackModel.push(new int[]{1, 2, 3, 4, 5});
        attackModel.push(new int[]{6, 7, 8, 9, 0});

        attackModel.reset();

        assertEquals("reset 후 공격 리스트는 비워져야 합니다.", 0, attackModel.getAttacks().size());
        assertEquals("reset 후 toString은 빈 문자열이어야 합니다.", "", attackModel.toString());
    }

    // ==================== AttackModel과 Board 통합 테스트 ====================

    @Test
    public void testAttackOnBoard_SingleAttack() {
        AttackModel attackModel = new AttackModel();
        Board board = new Board(5, 5);

        attackModel.push(new int[]{8, 0, 8, 8, 8});

        ArrayList<int[]> attacks = attackModel.popAttacks();
        boolean result = board.pushUp(attacks);

        assertTrue("빈 보드에 공격이 성공해야 합니다.", result);
        assertEquals("", attackModel.toString());
        assertEquals(0, attackModel.getAttacks().size());

        String expectedBoard = 
            "0 0 0 0 0 \n" +
            "0 0 0 0 0 \n" +
            "0 0 0 0 0 \n" +
            "0 0 0 0 0 \n" +
            "8 0 8 8 8 \n";
        
        assertEquals(expectedBoard, board.toString());
    }

    @Test
    public void testAttackOnBoard_MultipleAttacks() {
        AttackModel attackModel = new AttackModel();
        Board board = new Board(5, 5);

        attackModel.push(new int[]{8, 0, 8, 8, 8});
        attackModel.push(new int[]{8, 8, 0, 8, 8});
        attackModel.push(new int[]{8, 8, 8, 0, 8});
        attackModel.push(new int[]{8, 8, 8, 8, 0});

        ArrayList<int[]> attacks = attackModel.popAttacks();
        boolean result = board.pushUp(attacks);

        assertTrue(result);

        String expectedBoard = 
            "0 0 0 0 0 \n" +
            "8 0 8 8 8 \n" +
            "8 8 0 8 8 \n" +
            "8 8 8 0 8 \n" +
            "8 8 8 8 0 \n";
        
        assertEquals(expectedBoard, board.toString());
    }

    @Test
    public void testAttackOnBoard_Overflow() {
        AttackModel attackModel = new AttackModel();
        Board board = new Board(5, 5);
        int[][] boardData = board.getBoard();

        // 보드에 기존 블럭 배치 (1번 행)
        for (int c = 0; c < 5; c++) {
            boardData[1][c] = 1;
        }

        // 5줄 공격 추가 -> 넘침
        for (int i = 0; i < 5; i++) {
            attackModel.push(new int[]{8, 0, 8, 8, 8});
        }

        ArrayList<int[]> attacks = attackModel.popAttacks();
        boolean result = board.pushUp(attacks);

        assertFalse("오버플로우 시 false를 반환해야 합니다.", result);
        
        // 모든 공격이 적용되었는지 확인 (다 넣고 false 반환)
        assertEquals(0, attacks.size());
    }

    @Test
    public void testPopAttacks_PreservesOrder() {
        AttackModel attackModel = new AttackModel();

        // 순서대로 추가
        attackModel.push(new int[]{1, 1, 1, 1, 1});
        attackModel.push(new int[]{2, 2, 2, 2, 2});
        attackModel.push(new int[]{3, 3, 3, 3, 3});

        ArrayList<int[]> attacks = attackModel.popAttacks();

        // 순서가 유지되는지 확인
        assertEquals(1, attacks.get(0)[0]);
        assertEquals(2, attacks.get(1)[0]);
        assertEquals(3, attacks.get(2)[0]);
    }
}
