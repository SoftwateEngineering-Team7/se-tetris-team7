package org.util;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javafx.scene.input.KeyCode;

public class KeyLayoutTest {
    
    @Before
    public void setUp() {
        // 각 테스트 전에 기본값으로 초기화
        KeyLayout.resetAllToDefault();
    }
    
    @After
    public void tearDown() {
        // 각 테스트 후에 기본값으로 복원
        KeyLayout.resetAllToDefault();
    }
    
    // ========== Player 1 기본 테스트 ==========
    
    @Test
    public void testDefaultKeysPlayer1() {
        // Player 1 기본 키 설정 확인 (방향키 + SPACE)
        assertEquals(KeyCode.LEFT, KeyLayout.getLeftKey(PlayerId.PLAYER1));
        assertEquals(KeyCode.RIGHT, KeyLayout.getRightKey(PlayerId.PLAYER1));
        assertEquals(KeyCode.DOWN, KeyLayout.getDownKey(PlayerId.PLAYER1));
        assertEquals(KeyCode.UP, KeyLayout.getUpKey(PlayerId.PLAYER1));
        assertEquals(KeyCode.SPACE, KeyLayout.getHardDropKey(PlayerId.PLAYER1));
    }
    
    @Test
    public void testSetLeftKeyPlayer1() {
        KeyLayout.setLeftKey(PlayerId.PLAYER1, KeyCode.A);
        assertEquals(KeyCode.A, KeyLayout.getLeftKey(PlayerId.PLAYER1));
        
        // 다른 키는 변경되지 않아야 함
        assertEquals(KeyCode.RIGHT, KeyLayout.getRightKey(PlayerId.PLAYER1));
        assertEquals(KeyCode.DOWN, KeyLayout.getDownKey(PlayerId.PLAYER1));
        assertEquals(KeyCode.UP, KeyLayout.getUpKey(PlayerId.PLAYER1));
        assertEquals(KeyCode.SPACE, KeyLayout.getHardDropKey(PlayerId.PLAYER1));
    }
    
    @Test
    public void testSetRightKeyPlayer1() {
        KeyLayout.setRightKey(PlayerId.PLAYER1, KeyCode.D);
        assertEquals(KeyCode.D, KeyLayout.getRightKey(PlayerId.PLAYER1));
        
        // 다른 키는 변경되지 않아야 함
        assertEquals(KeyCode.LEFT, KeyLayout.getLeftKey(PlayerId.PLAYER1));
    }
    
    @Test
    public void testSetDownKeyPlayer1() {
        KeyLayout.setDownKey(PlayerId.PLAYER1, KeyCode.S);
        assertEquals(KeyCode.S, KeyLayout.getDownKey(PlayerId.PLAYER1));
        
        // 다른 키는 변경되지 않아야 함
        assertEquals(KeyCode.LEFT, KeyLayout.getLeftKey(PlayerId.PLAYER1));
        assertEquals(KeyCode.RIGHT, KeyLayout.getRightKey(PlayerId.PLAYER1));
    }
    
    @Test
    public void testSetUpKeyPlayer1() {
        KeyLayout.setUpKey(PlayerId.PLAYER1, KeyCode.W);
        assertEquals(KeyCode.W, KeyLayout.getUpKey(PlayerId.PLAYER1));
        
        // 다른 키는 변경되지 않아야 함
        assertEquals(KeyCode.LEFT, KeyLayout.getLeftKey(PlayerId.PLAYER1));
    }
    
    @Test
    public void testSetHardDropKeyPlayer1() {
        KeyLayout.setHardDropKey(PlayerId.PLAYER1, KeyCode.SHIFT);
        assertEquals(KeyCode.SHIFT, KeyLayout.getHardDropKey(PlayerId.PLAYER1));
        
        // 다른 키는 변경되지 않아야 함
        assertEquals(KeyCode.LEFT, KeyLayout.getLeftKey(PlayerId.PLAYER1));
        assertEquals(KeyCode.RIGHT, KeyLayout.getRightKey(PlayerId.PLAYER1));
    }
    
    @Test
    public void testSetKeysMethodPlayer1() {
        // setKeys 메서드로 한 번에 설정
        KeyLayout.setKeys(PlayerId.PLAYER1, KeyCode.A, KeyCode.D, KeyCode.S, KeyCode.W, KeyCode.SHIFT);
        
        assertEquals(KeyCode.A, KeyLayout.getLeftKey(PlayerId.PLAYER1));
        assertEquals(KeyCode.D, KeyLayout.getRightKey(PlayerId.PLAYER1));
        assertEquals(KeyCode.S, KeyLayout.getDownKey(PlayerId.PLAYER1));
        assertEquals(KeyCode.W, KeyLayout.getUpKey(PlayerId.PLAYER1));
        assertEquals(KeyCode.SHIFT, KeyLayout.getHardDropKey(PlayerId.PLAYER1));
    }
    
    @Test
    public void testSetNullKeysPlayer1() {
        // null 설정 시 변경되지 않아야 함
        KeyLayout.setLeftKey(PlayerId.PLAYER1, null);
        assertEquals(KeyCode.LEFT, KeyLayout.getLeftKey(PlayerId.PLAYER1));
        
        KeyLayout.setRightKey(PlayerId.PLAYER1, null);
        assertEquals(KeyCode.RIGHT, KeyLayout.getRightKey(PlayerId.PLAYER1));
        
        KeyLayout.setDownKey(PlayerId.PLAYER1, null);
        assertEquals(KeyCode.DOWN, KeyLayout.getDownKey(PlayerId.PLAYER1));
        
        KeyLayout.setUpKey(PlayerId.PLAYER1, null);
        assertEquals(KeyCode.UP, KeyLayout.getUpKey(PlayerId.PLAYER1));
        
        KeyLayout.setHardDropKey(PlayerId.PLAYER1, null);
        assertEquals(KeyCode.SPACE, KeyLayout.getHardDropKey(PlayerId.PLAYER1));
    }
    
    @Test
    public void testResetToDefaultPlayer1() {
        // 키를 변경한 후
        KeyLayout.setKeys(PlayerId.PLAYER1, KeyCode.A, KeyCode.D, KeyCode.S, KeyCode.W, KeyCode.SHIFT);
        
        // 기본값으로 리셋
        KeyLayout.resetToDefault(PlayerId.PLAYER1);
        
        // 기본값으로 복원되었는지 확인
        assertEquals(KeyCode.LEFT, KeyLayout.getLeftKey(PlayerId.PLAYER1));
        assertEquals(KeyCode.RIGHT, KeyLayout.getRightKey(PlayerId.PLAYER1));
        assertEquals(KeyCode.DOWN, KeyLayout.getDownKey(PlayerId.PLAYER1));
        assertEquals(KeyCode.UP, KeyLayout.getUpKey(PlayerId.PLAYER1));
        assertEquals(KeyCode.SPACE, KeyLayout.getHardDropKey(PlayerId.PLAYER1));
    }
    
    @Test
    public void testMultipleChangesPlayer1() {
        // 여러 번 변경
        KeyLayout.setLeftKey(PlayerId.PLAYER1, KeyCode.A);
        KeyLayout.setRightKey(PlayerId.PLAYER1, KeyCode.D);
        
        assertEquals(KeyCode.A, KeyLayout.getLeftKey(PlayerId.PLAYER1));
        assertEquals(KeyCode.D, KeyLayout.getRightKey(PlayerId.PLAYER1));
        
        // 다시 변경
        KeyLayout.setLeftKey(PlayerId.PLAYER1, KeyCode.Q);
        assertEquals(KeyCode.Q, KeyLayout.getLeftKey(PlayerId.PLAYER1));
        assertEquals(KeyCode.D, KeyLayout.getRightKey(PlayerId.PLAYER1));
    }
    
    // ========== Player 2 테스트 ==========
    
    @Test
    public void testDefaultKeysPlayer2() {
        // Player 2 기본 키 설정 확인 (WASD + SHIFT)
        assertEquals(KeyCode.A, KeyLayout.getLeftKey(PlayerId.PLAYER2));
        assertEquals(KeyCode.D, KeyLayout.getRightKey(PlayerId.PLAYER2));
        assertEquals(KeyCode.S, KeyLayout.getDownKey(PlayerId.PLAYER2));
        assertEquals(KeyCode.W, KeyLayout.getUpKey(PlayerId.PLAYER2));
        assertEquals(KeyCode.SHIFT, KeyLayout.getHardDropKey(PlayerId.PLAYER2));
    }
    
    @Test
    public void testSetKeysPlayer2() {
        // Player 2 키를 변경
        KeyLayout.setLeftKey(PlayerId.PLAYER2, KeyCode.J);
        KeyLayout.setRightKey(PlayerId.PLAYER2, KeyCode.L);
        KeyLayout.setDownKey(PlayerId.PLAYER2, KeyCode.K);
        KeyLayout.setUpKey(PlayerId.PLAYER2, KeyCode.I);
        KeyLayout.setHardDropKey(PlayerId.PLAYER2, KeyCode.CONTROL);
        
        assertEquals(KeyCode.J, KeyLayout.getLeftKey(PlayerId.PLAYER2));
        assertEquals(KeyCode.L, KeyLayout.getRightKey(PlayerId.PLAYER2));
        assertEquals(KeyCode.K, KeyLayout.getDownKey(PlayerId.PLAYER2));
        assertEquals(KeyCode.I, KeyLayout.getUpKey(PlayerId.PLAYER2));
        assertEquals(KeyCode.CONTROL, KeyLayout.getHardDropKey(PlayerId.PLAYER2));
        
        // Player 1은 변경되지 않아야 함
        assertEquals(KeyCode.LEFT, KeyLayout.getLeftKey(PlayerId.PLAYER1));
        assertEquals(KeyCode.RIGHT, KeyLayout.getRightKey(PlayerId.PLAYER1));
    }
    
    @Test
    public void testSetKeysMethodPlayer2() {
        // setKeys로 한 번에 설정
        KeyLayout.setKeys(PlayerId.PLAYER2, KeyCode.J, KeyCode.L, KeyCode.K, KeyCode.I, KeyCode.CONTROL);
        
        assertEquals(KeyCode.J, KeyLayout.getLeftKey(PlayerId.PLAYER2));
        assertEquals(KeyCode.L, KeyLayout.getRightKey(PlayerId.PLAYER2));
        assertEquals(KeyCode.K, KeyLayout.getDownKey(PlayerId.PLAYER2));
        assertEquals(KeyCode.I, KeyLayout.getUpKey(PlayerId.PLAYER2));
        assertEquals(KeyCode.CONTROL, KeyLayout.getHardDropKey(PlayerId.PLAYER2));
    }
    
    @Test
    public void testResetToDefaultPlayer2() {
        // Player 2 키를 변경한 후
        KeyLayout.setKeys(PlayerId.PLAYER2, KeyCode.J, KeyCode.L, KeyCode.K, KeyCode.I, KeyCode.CONTROL);
        
        // resetToDefault 호출
        KeyLayout.resetToDefault(PlayerId.PLAYER2);
        
        // 기본값으로 복원되어야 함
        assertEquals(KeyCode.A, KeyLayout.getLeftKey(PlayerId.PLAYER2));
        assertEquals(KeyCode.D, KeyLayout.getRightKey(PlayerId.PLAYER2));
        assertEquals(KeyCode.S, KeyLayout.getDownKey(PlayerId.PLAYER2));
        assertEquals(KeyCode.W, KeyLayout.getUpKey(PlayerId.PLAYER2));
        assertEquals(KeyCode.SHIFT, KeyLayout.getHardDropKey(PlayerId.PLAYER2));
    }
    
    // ========== 독립성 테스트 ==========
    
    @Test
    public void testIndependentPlayerKeys() {
        // Player 1 변경
        KeyLayout.setLeftKey(PlayerId.PLAYER1, KeyCode.A);
        
        // Player 2 변경
        KeyLayout.setLeftKey(PlayerId.PLAYER2, KeyCode.J);
        
        // 각 플레이어가 독립적으로 유지되어야 함
        assertEquals(KeyCode.A, KeyLayout.getLeftKey(PlayerId.PLAYER1));
        assertEquals(KeyCode.J, KeyLayout.getLeftKey(PlayerId.PLAYER2));
    }
    
    @Test
    public void testResetAllToDefault() {
        // 두 플레이어 모두 변경
        KeyLayout.setKeys(PlayerId.PLAYER1, KeyCode.A, KeyCode.D, KeyCode.S, KeyCode.W, KeyCode.SHIFT);
        KeyLayout.setKeys(PlayerId.PLAYER2, KeyCode.J, KeyCode.L, KeyCode.K, KeyCode.I, KeyCode.CONTROL);
        
        // 모두 리셋
        KeyLayout.resetAllToDefault();
        
        // Player 1 기본값 확인
        assertEquals(KeyCode.LEFT, KeyLayout.getLeftKey(PlayerId.PLAYER1));
        assertEquals(KeyCode.SPACE, KeyLayout.getHardDropKey(PlayerId.PLAYER1));
        
        // Player 2 기본값 확인
        assertEquals(KeyCode.A, KeyLayout.getLeftKey(PlayerId.PLAYER2));
        assertEquals(KeyCode.SHIFT, KeyLayout.getHardDropKey(PlayerId.PLAYER2));
    }
}
