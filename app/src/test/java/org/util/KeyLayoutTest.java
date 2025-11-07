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
        KeyLayout.resetToDefault();
    }
    
    @After
    public void tearDown() {
        // 각 테스트 후에 기본값으로 복원
        KeyLayout.resetToDefault();
    }
    
    @Test
    public void testDefaultKeys() {
        // 기본 키 설정 확인 (방향키 + SPACE)
        assertEquals(KeyCode.LEFT, KeyLayout.getLeftKey());
        assertEquals(KeyCode.RIGHT, KeyLayout.getRightKey());
        assertEquals(KeyCode.DOWN, KeyLayout.getDownKey());
        assertEquals(KeyCode.UP, KeyLayout.getUpKey());
        assertEquals(KeyCode.SPACE, KeyLayout.getHardDropKey());
    }
    
    @Test
    public void testSetLeftKey() {
        KeyLayout.setLeftKey(KeyCode.A);
        assertEquals(KeyCode.A, KeyLayout.getLeftKey());
        
        // 다른 키는 변경되지 않아야 함
        assertEquals(KeyCode.RIGHT, KeyLayout.getRightKey());
        assertEquals(KeyCode.DOWN, KeyLayout.getDownKey());
        assertEquals(KeyCode.UP, KeyLayout.getUpKey());
        assertEquals(KeyCode.SPACE, KeyLayout.getHardDropKey());
    }
    
    @Test
    public void testSetRightKey() {
        KeyLayout.setRightKey(KeyCode.D);
        assertEquals(KeyCode.D, KeyLayout.getRightKey());
        
        // 다른 키는 변경되지 않아야 함
        assertEquals(KeyCode.LEFT, KeyLayout.getLeftKey());
    }
    
    @Test
    public void testSetDownKey() {
        KeyLayout.setDownKey(KeyCode.S);
        assertEquals(KeyCode.S, KeyLayout.getDownKey());
        
        // 다른 키는 변경되지 않아야 함
        assertEquals(KeyCode.LEFT, KeyLayout.getLeftKey());
        assertEquals(KeyCode.RIGHT, KeyLayout.getRightKey());
    }
    
    @Test
    public void testSetUpKey() {
        KeyLayout.setUpKey(KeyCode.W);
        assertEquals(KeyCode.W, KeyLayout.getUpKey());
        
        // 다른 키는 변경되지 않아야 함
        assertEquals(KeyCode.LEFT, KeyLayout.getLeftKey());
    }
    
    @Test
    public void testSetHardDropKey() {
        KeyLayout.setHardDropKey(KeyCode.SHIFT);
        assertEquals(KeyCode.SHIFT, KeyLayout.getHardDropKey());
        
        // 다른 키는 변경되지 않아야 함
        assertEquals(KeyCode.LEFT, KeyLayout.getLeftKey());
        assertEquals(KeyCode.RIGHT, KeyLayout.getRightKey());
    }
    
    @Test
    public void testSetKeysMethod() {
        // setKeys 메서드로 한 번에 설정
        KeyLayout.setKeys(KeyCode.A, KeyCode.D, KeyCode.S, KeyCode.W, KeyCode.SHIFT);
        
        assertEquals(KeyCode.A, KeyLayout.getLeftKey());
        assertEquals(KeyCode.D, KeyLayout.getRightKey());
        assertEquals(KeyCode.S, KeyLayout.getDownKey());
        assertEquals(KeyCode.W, KeyLayout.getUpKey());
        assertEquals(KeyCode.SHIFT, KeyLayout.getHardDropKey());
    }
    
    @Test
    public void testSetNullKeys() {
        // null 설정 시 변경되지 않아야 함
        KeyLayout.setLeftKey(null);
        assertEquals(KeyCode.LEFT, KeyLayout.getLeftKey());
        
        KeyLayout.setRightKey(null);
        assertEquals(KeyCode.RIGHT, KeyLayout.getRightKey());
        
        KeyLayout.setDownKey(null);
        assertEquals(KeyCode.DOWN, KeyLayout.getDownKey());
        
        KeyLayout.setUpKey(null);
        assertEquals(KeyCode.UP, KeyLayout.getUpKey());
        
        KeyLayout.setHardDropKey(null);
        assertEquals(KeyCode.SPACE, KeyLayout.getHardDropKey());
    }
    
    @Test
    public void testResetToDefault() {
        // 키를 변경한 후
        KeyLayout.setKeys(KeyCode.A, KeyCode.D, KeyCode.S, KeyCode.W, KeyCode.SHIFT);
        
        // resetToDefault 호출
        KeyLayout.resetToDefault();
        
        // 기본값으로 복원되어야 함
        assertEquals(KeyCode.LEFT, KeyLayout.getLeftKey());
        assertEquals(KeyCode.RIGHT, KeyLayout.getRightKey());
        assertEquals(KeyCode.DOWN, KeyLayout.getDownKey());
        assertEquals(KeyCode.UP, KeyLayout.getUpKey());
        assertEquals(KeyCode.SPACE, KeyLayout.getHardDropKey());
    }
    
    @Test
    public void testMultipleChanges() {
        // 여러 번 변경 후 최종 값 확인
        KeyLayout.setLeftKey(KeyCode.A);
        KeyLayout.setRightKey(KeyCode.D);
        
        assertEquals(KeyCode.A, KeyLayout.getLeftKey());
        assertEquals(KeyCode.D, KeyLayout.getRightKey());
        
        KeyLayout.setLeftKey(KeyCode.Q);
        assertEquals(KeyCode.Q, KeyLayout.getLeftKey());
        
        KeyLayout.resetToDefault();
        assertEquals(KeyCode.LEFT, KeyLayout.getLeftKey());
        assertEquals(KeyCode.RIGHT, KeyLayout.getRightKey());
    }
}
