package org.tetris.menu.setting.model;

import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
    
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.util.Difficulty;
import org.util.GameColor;
import org.util.KeyLayout;
import org.util.ScreenPreset;

import javafx.scene.input.KeyCode;

public class SettingTest {
    
    private Setting setting;
    private final Path testFilePath = Paths.get("setting.txt");
    
    @Before
    public void setUp() {
        // 테스트 전에 기존 설정 파일 삭제
        deleteSettingFile();
        setting = new Setting();
    }
    
    @After
    public void tearDown() {
        // 테스트 후 설정 파일 삭제
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
    public void testDefaultValues() {
        assertEquals("기본 색맹 모드는 false여야 합니다", false, setting.isColorBlind());
        assertEquals("기본 화면 프리셋은 SMALL이어야 합니다", "SMALL", setting.getScreenPreset());
        assertEquals("기본 난이도는 EASY여야 합니다", "EASY", setting.getDifficulty());
    }

    @Test
    public void testDefaultPlayer1Keys() {
        assertEquals("Player 1 왼쪽 키 기본값은 LEFT", "LEFT", setting.getKeyLeft());
        assertEquals("Player 1 오른쪽 키 기본값은 RIGHT", "RIGHT", setting.getKeyRight());
        assertEquals("Player 1 위 키 기본값은 UP", "UP", setting.getKeyUp());
        assertEquals("Player 1 아래 키 기본값은 DOWN", "DOWN", setting.getKeyDown());
        assertEquals("Player 1 하드드롭 키 기본값은 SPACE", "SPACE", setting.getKeyHardDrop());
    }

    @Test
    public void testDefaultPlayer2Keys() {
        assertEquals("Player 2 왼쪽 키 기본값은 A", "A", setting.getKeyLeft2());
        assertEquals("Player 2 오른쪽 키 기본값은 D", "D", setting.getKeyRight2());
        assertEquals("Player 2 위 키 기본값은 W", "W", setting.getKeyUp2());
        assertEquals("Player 2 아래 키 기본값은 S", "S", setting.getKeyDown2());
        assertEquals("Player 2 하드드롭 키 기본값은 SHIFT", "SHIFT", setting.getKeyHardDrop2());
    }

    @Test
    public void testSaveAndLoadPlayer1Keys() {
        setting.setKeyLeft("A");
        setting.setKeyRight("D");
        setting.setKeyUp("W");
        setting.setKeyDown("S");
        setting.setKeyHardDrop("Q");

        // 새 인스턴스로 로드
        Setting newSetting = new Setting();
        assertEquals("저장된 Player 1 왼쪽 키", "A", newSetting.getKeyLeft());
        assertEquals("저장된 Player 1 오른쪽 키", "D", newSetting.getKeyRight());
        assertEquals("저장된 Player 1 위 키", "W", newSetting.getKeyUp());
        assertEquals("저장된 Player 1 아래 키", "S", newSetting.getKeyDown());
        assertEquals("저장된 Player 1 하드드롭 키", "Q", newSetting.getKeyHardDrop());
    }

    @Test
    public void testSaveAndLoadPlayer2Keys() {
        setting.setKeyLeft2("J");
        setting.setKeyRight2("L");
        setting.setKeyUp2("I");
        setting.setKeyDown2("K");
        setting.setKeyHardDrop2("U");

        // 새 인스턴스로 로드
        Setting newSetting = new Setting();
        assertEquals("저장된 Player 2 왼쪽 키", "J", newSetting.getKeyLeft2());
        assertEquals("저장된 Player 2 오른쪽 키", "L", newSetting.getKeyRight2());
        assertEquals("저장된 Player 2 위 키", "I", newSetting.getKeyUp2());
        assertEquals("저장된 Player 2 아래 키", "K", newSetting.getKeyDown2());
        assertEquals("저장된 Player 2 하드드롭 키", "U", newSetting.getKeyHardDrop2());
    }

    @Test
    public void testKeyLayoutSyncWithPlayer1() {
        // Player 1 키 변경
        setting.setKeyLeft("A");
        setting.setKeyRight("D");
        setting.setKeyUp("W");
        setting.setKeyDown("S");
        setting.setKeyHardDrop("SHIFT");

        // KeyLayout이 Player 1 키로 동기화되었는지 확인
        assertEquals("KeyLayout 왼쪽 키가 A로 동기화", KeyCode.A, KeyLayout.getLeftKey());
        assertEquals("KeyLayout 오른쪽 키가 D로 동기화", KeyCode.D, KeyLayout.getRightKey());
        assertEquals("KeyLayout 위 키가 W로 동기화", KeyCode.W, KeyLayout.getUpKey());
        assertEquals("KeyLayout 아래 키가 S로 동기화", KeyCode.S, KeyLayout.getDownKey());
        assertEquals("KeyLayout 하드드롭 키가 SHIFT로 동기화", KeyCode.SHIFT, KeyLayout.getHardDropKey());
    }
    
    @Test
    public void testSetColorBlind() {
        setting.setColorBlind(true);
        assertTrue("색맹 모드가 true로 설정되어야 합니다", setting.isColorBlind());
        assertTrue("GameColor의 색맹 모드도 동기화되어야 합니다", 
                  GameColor.getColorBlind());
        
        setting.setColorBlind(false);
        assertFalse("색맹 모드가 false로 설정되어야 합니다", setting.isColorBlind());
        assertFalse("GameColor의 색맹 모드도 동기화되어야 합니다", 
                   GameColor.getColorBlind());
    }
    
    @Test
    public void testSetScreenPreset() {
        setting.setScreenPreset("MIDDLE");
        assertEquals("화면 프리셋이 MIDDLE로 설정되어야 합니다", "MIDDLE", setting.getScreenPreset());
        assertEquals("ScreenPreset의 현재 프리셋도 동기화되어야 합니다", 
                    "MIDDLE", ScreenPreset.getCurrentPreset());
        
        setting.setScreenPreset("LARGE");
        assertEquals("화면 프리셋이 LARGE로 설정되어야 합니다", "LARGE", setting.getScreenPreset());
        assertEquals("ScreenPreset의 현재 프리셋도 동기화되어야 합니다", 
                    "LARGE", ScreenPreset.getCurrentPreset());
    }
    
    @Test
    public void testSetDifficulty() {
        setting.setDifficulty("NORMAL");
        assertEquals("난이도가 NORMAL로 설정되어야 합니다", "NORMAL", setting.getDifficulty());
        assertEquals("Difficulty의 현재 난이도도 동기화되어야 합니다", 
                    "NORMAL", Difficulty.getCurrentDifficulty());
        
        setting.setDifficulty("HARD");
        assertEquals("난이도가 HARD로 설정되어야 합니다", "HARD", setting.getDifficulty());
        assertEquals("Difficulty의 현재 난이도도 동기화되어야 합니다", 
                    "HARD", Difficulty.getCurrentDifficulty());
    }
    
    @Test
    public void testSetDefaultsAndSave() {
        // 먼저 값을 변경
        setting.setColorBlind(true);
        setting.setScreenPreset("LARGE");
        setting.setDifficulty("HARD");
        
        // 기본값으로 초기화
        setting.setDefaultsAndSave();
        
        assertEquals("기본값으로 초기화 후 색맹 모드는 false여야 합니다", 
                    false, setting.isColorBlind());
        assertEquals("기본값으로 초기화 후 키 레이아웃은 ARROWS여야 합니다", 
                    "SMALL", setting.getScreenPreset());
        assertEquals("기본값으로 초기화 후 난이도는 EASY여야 합니다", 
                    "EASY", setting.getDifficulty());
    }
    
    @Test
    public void testSettingFilePersistence() {
        // 설정 변경
        setting.setColorBlind(true);
        setting.setScreenPreset("MIDDLE");
        setting.setDifficulty("NORMAL");
        
        // 새로운 Setting 객체 생성 (파일에서 로드)
        Setting newSetting = new Setting();
        
        assertEquals("파일에서 로드된 색맹 모드가 일치해야 합니다", 
                    true, newSetting.isColorBlind());
        assertEquals("파일에서 로드된 화면 프리셋이 일치해야 합니다", 
                    "MIDDLE", newSetting.getScreenPreset());
        assertEquals("파일에서 로드된 난이도가 일치해야 합니다", 
                    "NORMAL", newSetting.getDifficulty());
    }
}
