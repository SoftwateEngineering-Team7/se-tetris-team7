package org.tetris.menu.setting;

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
        assertEquals("기본 키 레이아웃은 ARROWS여야 합니다", "ARROWS", setting.getKeyLayout());
        assertEquals("기본 화면 프리셋은 SMALL이어야 합니다", "SMALL", setting.getScreenPreset());
        assertEquals("기본 난이도는 EASY여야 합니다", "EASY", setting.getDifficulty());
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
    public void testSetKeyLayout() {
        setting.setKeyLayout("WASD");
        assertEquals("키 레이아웃이 WASD로 설정되어야 합니다", "WASD", setting.getKeyLayout());
        assertEquals("KeyLayout의 현재 레이아웃도 동기화되어야 합니다", 
                    "WASD", KeyLayout.getCurrentLayout());
        
        setting.setKeyLayout("ARROWS");
        assertEquals("키 레이아웃이 ARROWS로 설정되어야 합니다", "ARROWS", setting.getKeyLayout());
        assertEquals("KeyLayout의 현재 레이아웃도 동기화되어야 합니다", 
                    "ARROWS", KeyLayout.getCurrentLayout());
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
        setting.setKeyLayout("WASD");
        setting.setScreenPreset("LARGE");
        setting.setDifficulty("HARD");
        
        // 기본값으로 초기화
        setting.setDefaultsAndSave();
        
        assertEquals("기본값으로 초기화 후 색맹 모드는 false여야 합니다", 
                    false, setting.isColorBlind());
        assertEquals("기본값으로 초기화 후 키 레이아웃은 ARROWS여야 합니다", 
                    "ARROWS", setting.getKeyLayout());
        assertEquals("기본값으로 초기화 후 화면 프리셋은 SMALL이어야 합니다", 
                    "SMALL", setting.getScreenPreset());
        assertEquals("기본값으로 초기화 후 난이도는 EASY여야 합니다", 
                    "EASY", setting.getDifficulty());
    }
    
    @Test
    public void testSettingFilePersistence() {
        // 설정 변경
        setting.setColorBlind(true);
        setting.setKeyLayout("WASD");
        setting.setScreenPreset("MIDDLE");
        setting.setDifficulty("NORMAL");
        
        // 새로운 Setting 객체 생성 (파일에서 로드)
        Setting newSetting = new Setting();
        
        assertEquals("파일에서 로드된 색맹 모드가 일치해야 합니다", 
                    true, newSetting.isColorBlind());
        assertEquals("파일에서 로드된 키 레이아웃이 일치해야 합니다", 
                    "WASD", newSetting.getKeyLayout());
        assertEquals("파일에서 로드된 화면 프리셋이 일치해야 합니다", 
                    "MIDDLE", newSetting.getScreenPreset());
        assertEquals("파일에서 로드된 난이도가 일치해야 합니다", 
                    "NORMAL", newSetting.getDifficulty());
    }
}
