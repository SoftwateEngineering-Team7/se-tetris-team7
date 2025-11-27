package org.tetris.menu.setting.model;

import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.util.PlayerId;

public class SettingMenuModelTest {
    
    private Setting setting;
    private SettingMenuModel model;
    private final Path testFilePath = Paths.get("setting.txt");
    
    @Before
    public void setUp() {
        deleteSettingFile();
        setting = new Setting();
        model = new SettingMenuModel(setting);
    }
    
    @After
    public void tearDown() {
        deleteSettingFile();
    }
    
    private void deleteSettingFile() {
        try {
            Files.deleteIfExists(testFilePath);
        } catch (IOException e) {
            // 무시
        }
    }
    
    @Test
    public void testModelCreation() {
        assertNotNull("모델이 null이 아니어야 합니다", model);
        // 초기값이 Setting의 기본값과 일치하는지 확인
        assertEquals("초기 색맹 모드는 false여야 합니다", false, model.isColorBlind());
        // Player 1 키 바인딩 초기값 확인
        assertEquals("초기 왼쪽 키는 LEFT여야 합니다", "LEFT", model.getKeyLeft(PlayerId.PLAYER1));
        assertEquals("초기 오른쪽 키는 RIGHT여야 합니다", "RIGHT", model.getKeyRight(PlayerId.PLAYER1));
        assertEquals("초기 아래 키는 DOWN이어야 합니다", "DOWN", model.getKeyDown(PlayerId.PLAYER1));
        assertEquals("초기 위 키는 UP이어야 합니다", "UP", model.getKeyUp(PlayerId.PLAYER1));
        assertEquals("초기 하드드롭 키는 SPACE여야 합니다", "SPACE", model.getKeyHardDrop(PlayerId.PLAYER1));
        // Player 2 키 바인딩 초기값 확인
        assertEquals("초기 Player 2 왼쪽 키는 A여야 합니다", "A", model.getKeyLeft(PlayerId.PLAYER2));
        assertEquals("초기 Player 2 오른쪽 키는 D여야 합니다", "D", model.getKeyRight(PlayerId.PLAYER2));
        assertEquals("초기 Player 2 아래 키는 S여야 합니다", "S", model.getKeyDown(PlayerId.PLAYER2));
        assertEquals("초기 Player 2 위 키는 W여야 합니다", "W", model.getKeyUp(PlayerId.PLAYER2));
        assertEquals("초기 Player 2 하드드롭 키는 SHIFT여야 합니다", "SHIFT", model.getKeyHardDrop(PlayerId.PLAYER2));
        // 화면 프리셋 초기값 확인
        assertEquals("초기 화면 프리셋은 SMALL이어야 합니다", "SMALL", model.getScreen());
        // 난이도 초기값 확인
        assertEquals("초기 난이도는 EASY여야 합니다", "EASY", model.getDifficulty());
    }
    
    @Test
    public void testSetColorBlind() {
        model.setColorBlind(true);
        assertTrue("색맹 모드가 true로 설정되어야 합니다", model.isColorBlind());
        
        model.setColorBlind(false);
        assertFalse("색맹 모드가 false로 설정되어야 합니다", model.isColorBlind());
    }
    
    @Test
    public void testSetPlayer1KeyBindings() {
        model.setKeyLeft(PlayerId.PLAYER1, "A");
        assertEquals("왼쪽 키가 A로 설정되어야 합니다", "A", model.getKeyLeft(PlayerId.PLAYER1));
        
        model.setKeyRight(PlayerId.PLAYER1, "D");
        assertEquals("오른쪽 키가 D로 설정되어야 합니다", "D", model.getKeyRight(PlayerId.PLAYER1));
        
        model.setKeyDown(PlayerId.PLAYER1, "S");
        assertEquals("아래 키가 S로 설정되어야 합니다", "S", model.getKeyDown(PlayerId.PLAYER1));
        
        model.setKeyUp(PlayerId.PLAYER1, "W");
        assertEquals("위 키가 W로 설정되어야 합니다", "W", model.getKeyUp(PlayerId.PLAYER1));
        
        model.setKeyHardDrop(PlayerId.PLAYER1, "SHIFT");
        assertEquals("하드드롭 키가 SHIFT로 설정되어야 합니다", "SHIFT", model.getKeyHardDrop(PlayerId.PLAYER1));
    }
    
    @Test
    public void testSetPlayer2KeyBindings() {
        model.setKeyLeft(PlayerId.PLAYER2, "Q");
        assertEquals("Player 2 왼쪽 키가 Q로 설정되어야 합니다", "Q", model.getKeyLeft(PlayerId.PLAYER2));
        
        model.setKeyRight(PlayerId.PLAYER2, "E");
        assertEquals("Player 2 오른쪽 키가 E로 설정되어야 합니다", "E", model.getKeyRight(PlayerId.PLAYER2));
        
        model.setKeyDown(PlayerId.PLAYER2, "F");
        assertEquals("Player 2 아래 키가 F로 설정되어야 합니다", "F", model.getKeyDown(PlayerId.PLAYER2));
        
        model.setKeyUp(PlayerId.PLAYER2, "R");
        assertEquals("Player 2 위 키가 R로 설정되어야 합니다", "R", model.getKeyUp(PlayerId.PLAYER2));
        
        model.setKeyHardDrop(PlayerId.PLAYER2, "CONTROL");
        assertEquals("Player 2 하드드롭 키가 CONTROL로 설정되어야 합니다", "CONTROL", model.getKeyHardDrop(PlayerId.PLAYER2));
    }
    
    @Test
    public void testSetScreen() {
        model.setScreen("MIDDLE");
        assertEquals("화면 프리셋이 MIDDLE로 설정되어야 합니다", "MIDDLE", model.getScreen());
        
        model.setScreen("LARGE");
        assertEquals("화면 프리셋이 LARGE로 설정되어야 합니다", "LARGE", model.getScreen());
    }
    
    @Test
    public void testSetDifficulty() {
        model.setDifficulty("NORMAL");
        assertEquals("난이도가 NORMAL로 설정되어야 합니다", "NORMAL", model.getDifficulty());
        
        model.setDifficulty("HARD");
        assertEquals("난이도가 HARD로 설정되어야 합니다", "HARD", model.getDifficulty());
    }
    
    @Test
    public void testApplyToSetting() {
        // 모델 값 변경
        model.setColorBlind(true);
        model.setKeyLeft(PlayerId.PLAYER1, "A");
        model.setKeyRight(PlayerId.PLAYER1, "D");
        model.setKeyDown(PlayerId.PLAYER1, "S");
        model.setKeyUp(PlayerId.PLAYER1, "W");
        model.setKeyHardDrop(PlayerId.PLAYER1, "SHIFT");
        model.setKeyLeft(PlayerId.PLAYER2, "Q");
        model.setKeyRight(PlayerId.PLAYER2, "E");
        model.setKeyDown(PlayerId.PLAYER2, "F");
        model.setKeyUp(PlayerId.PLAYER2, "R");
        model.setKeyHardDrop(PlayerId.PLAYER2, "CONTROL");
        model.setScreen("LARGE");
        model.setDifficulty("HARD");
        
        // Setting에 반영
        model.applyToSetting();
        
        // Setting의 값이 변경되었는지 확인
        assertEquals("Setting의 색맹 모드가 반영되어야 합니다", 
                    true, setting.isColorBlind());
        assertEquals("Setting의 왼쪽 키가 반영되어야 합니다", 
                    "A", setting.getKeyLeft(PlayerId.PLAYER1));
        assertEquals("Setting의 오른쪽 키가 반영되어야 합니다", 
                    "D", setting.getKeyRight(PlayerId.PLAYER1));
        assertEquals("Setting의 아래 키가 반영되어야 합니다", 
                    "S", setting.getKeyDown(PlayerId.PLAYER1));
        assertEquals("Setting의 위 키가 반영되어야 합니다", 
                    "W", setting.getKeyUp(PlayerId.PLAYER1));
        assertEquals("Setting의 하드드롭 키가 반영되어야 합니다", 
                    "SHIFT", setting.getKeyHardDrop(PlayerId.PLAYER1));
        assertEquals("Setting의 Player 2 왼쪽 키가 반영되어야 합니다", 
                    "Q", setting.getKeyLeft(PlayerId.PLAYER2));
        assertEquals("Setting의 Player 2 오른쪽 키가 반영되어야 합니다", 
                    "E", setting.getKeyRight(PlayerId.PLAYER2));
        assertEquals("Setting의 Player 2 아래 키가 반영되어야 합니다", 
                    "F", setting.getKeyDown(PlayerId.PLAYER2));
        assertEquals("Setting의 Player 2 위 키가 반영되어야 합니다", 
                    "R", setting.getKeyUp(PlayerId.PLAYER2));
        assertEquals("Setting의 Player 2 하드드롭 키가 반영되어야 합니다", 
                    "CONTROL", setting.getKeyHardDrop(PlayerId.PLAYER2));
        assertEquals("Setting의 화면 프리셋이 반영되어야 합니다", 
                    "LARGE", setting.getScreenPreset());
        assertEquals("Setting의 난이도가 반영되어야 합니다", 
                    "HARD", setting.getDifficulty());
    }
    
    @Test
    public void testResetToSetting() {
        // 모델 값 변경
        model.setColorBlind(true);
        model.setKeyLeft(PlayerId.PLAYER1, "A");
        model.setKeyRight(PlayerId.PLAYER1, "D");
        model.setKeyHardDrop(PlayerId.PLAYER1, "SHIFT");
        model.setKeyLeft(PlayerId.PLAYER2, "Q");
        model.setScreen("LARGE");
        model.setDifficulty("HARD");
        
        // 기본값으로 리셋
        model.resetToSetting();
        
        // 모델이 기본값으로 초기화되었는지 확인
        assertEquals("리셋 후 색맹 모드는 false여야 합니다", 
                    false, model.isColorBlind());
        assertEquals("리셋 후 왼쪽 키는 LEFT여야 합니다", 
                    "LEFT", model.getKeyLeft(PlayerId.PLAYER1));
        assertEquals("리셋 후 오른쪽 키는 RIGHT여야 합니다", 
                    "RIGHT", model.getKeyRight(PlayerId.PLAYER1));
        assertEquals("리셋 후 아래 키는 DOWN이어야 합니다", 
                    "DOWN", model.getKeyDown(PlayerId.PLAYER1));
        assertEquals("리셋 후 위 키는 UP이어야 합니다", 
                    "UP", model.getKeyUp(PlayerId.PLAYER1));
        assertEquals("리셋 후 하드드롭 키는 SPACE여야 합니다", 
                    "SPACE", model.getKeyHardDrop(PlayerId.PLAYER1));
        assertEquals("리셋 후 Player 2 왼쪽 키는 A여야 합니다", 
                    "A", model.getKeyLeft(PlayerId.PLAYER2));
        assertEquals("리셋 후 Player 2 오른쪽 키는 D여야 합니다", 
                    "D", model.getKeyRight(PlayerId.PLAYER2));
        assertEquals("리셋 후 Player 2 하드드롭 키는 SHIFT여야 합니다", 
                    "SHIFT", model.getKeyHardDrop(PlayerId.PLAYER2));
        assertEquals("리셋 후 화면 프리셋은 SMALL이어야 합니다", 
                    "SMALL", model.getScreen());
        assertEquals("리셋 후 난이도는 EASY여야 합니다", 
                    "EASY", model.getDifficulty());
    }
    
    @Test
    public void testUpdateModelFromSettings() {
        // Setting 값 변경
        setting.setColorBlind(true);
        setting.setKeyLeft(PlayerId.PLAYER1, "J");
        setting.setKeyRight(PlayerId.PLAYER1, "L");
        setting.setKeyDown(PlayerId.PLAYER1, "K");
        setting.setKeyUp(PlayerId.PLAYER1, "I");
        setting.setKeyHardDrop(PlayerId.PLAYER1, "O");
        setting.setKeyLeft(PlayerId.PLAYER2, "Q");
        setting.setKeyRight(PlayerId.PLAYER2, "E");
        setting.setKeyDown(PlayerId.PLAYER2, "F");
        setting.setKeyUp(PlayerId.PLAYER2, "R");
        setting.setKeyHardDrop(PlayerId.PLAYER2, "T");
        setting.setScreenPreset("MIDDLE");
        setting.setDifficulty("NORMAL");
        
        // 모델 업데이트
        model.updateModelFromSettings();
        
        // 모델이 Setting의 값과 일치하는지 확인
        assertEquals("모델의 색맹 모드가 Setting과 일치해야 합니다", 
                    true, model.isColorBlind());
        assertEquals("모델의 왼쪽 키가 Setting과 일치해야 합니다", 
                    "J", model.getKeyLeft(PlayerId.PLAYER1));
        assertEquals("모델의 오른쪽 키가 Setting과 일치해야 합니다", 
                    "L", model.getKeyRight(PlayerId.PLAYER1));
        assertEquals("모델의 아래 키가 Setting과 일치해야 합니다", 
                    "K", model.getKeyDown(PlayerId.PLAYER1));
        assertEquals("모델의 위 키가 Setting과 일치해야 합니다", 
                    "I", model.getKeyUp(PlayerId.PLAYER1));
        assertEquals("모델의 하드드롭 키가 Setting과 일치해야 합니다", 
                    "O", model.getKeyHardDrop(PlayerId.PLAYER1));
        assertEquals("모델의 Player 2 왼쪽 키가 Setting과 일치해야 합니다", 
                    "Q", model.getKeyLeft(PlayerId.PLAYER2));
        assertEquals("모델의 Player 2 오른쪽 키가 Setting과 일치해야 합니다", 
                    "E", model.getKeyRight(PlayerId.PLAYER2));
        assertEquals("모델의 Player 2 아래 키가 Setting과 일치해야 합니다", 
                    "F", model.getKeyDown(PlayerId.PLAYER2));
        assertEquals("모델의 Player 2 위 키가 Setting과 일치해야 합니다", 
                    "R", model.getKeyUp(PlayerId.PLAYER2));
        assertEquals("모델의 Player 2 하드드롭 키가 Setting과 일치해야 합니다", 
                    "T", model.getKeyHardDrop(PlayerId.PLAYER2));
        assertEquals("모델의 화면 프리셋이 Setting과 일치해야 합니다", 
                    "MIDDLE", model.getScreen());
        assertEquals("모델의 난이도가 Setting과 일치해야 합니다", 
                    "NORMAL", model.getDifficulty());
    }
    
    @Test
    public void testGetSetting() {
        assertNotNull("Setting 객체가 null이 아니어야 합니다", model.getSetting());
        assertSame("Setting 객체가 동일해야 합니다", setting, model.getSetting());
    }
}
