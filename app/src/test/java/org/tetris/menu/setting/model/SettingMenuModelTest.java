package org.tetris.menu.setting.model;

import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

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
        assertEquals("초기 색맹 모드는 false여야 합니다", false, model.isColorBlind());
        assertEquals("초기 키 레이아웃은 ARROWS여야 합니다", "ARROWS", model.getKeyLayout());
        assertEquals("초기 화면 프리셋은 SMALL이어야 합니다", "SMALL", model.getScreen());
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
    public void testSetKeyLayout() {
        model.setKeyLayout("WASD");
        assertEquals("키 레이아웃이 WASD로 설정되어야 합니다", "WASD", model.getKeyLayout());
        
        model.setKeyLayout("ARROWS");
        assertEquals("키 레이아웃이 ARROWS로 설정되어야 합니다", "ARROWS", model.getKeyLayout());
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
        model.setKeyLayout("WASD");
        model.setScreen("LARGE");
        model.setDifficulty("HARD");
        
        // Setting에 반영
        model.applyToSetting();
        
        // Setting의 값이 변경되었는지 확인
        assertEquals("Setting의 색맹 모드가 반영되어야 합니다", 
                    true, setting.isColorBlind());
        assertEquals("Setting의 키 레이아웃이 반영되어야 합니다", 
                    "WASD", setting.getKeyLayout());
        assertEquals("Setting의 화면 프리셋이 반영되어야 합니다", 
                    "LARGE", setting.getScreenPreset());
        assertEquals("Setting의 난이도가 반영되어야 합니다", 
                    "HARD", setting.getDifficulty());
    }
    
    @Test
    public void testResetToSetting() {
        // 모델 값 변경
        model.setColorBlind(true);
        model.setKeyLayout("WASD");
        model.setScreen("LARGE");
        model.setDifficulty("HARD");
        
        // 기본값으로 리셋
        model.resetToSetting();
        
        // 모델이 기본값으로 초기화되었는지 확인
        assertEquals("리셋 후 색맹 모드는 false여야 합니다", 
                    false, model.isColorBlind());
        assertEquals("리셋 후 키 레이아웃은 ARROWS여야 합니다", 
                    "ARROWS", model.getKeyLayout());
        assertEquals("리셋 후 화면 프리셋은 SMALL이어야 합니다", 
                    "SMALL", model.getScreen());
        assertEquals("리셋 후 난이도는 EASY여야 합니다", 
                    "EASY", model.getDifficulty());
    }
    
    @Test
    public void testUpdateModelFromSettings() {
        // Setting 값 변경
        setting.setColorBlind(true);
        setting.setKeyLayout("WASD");
        setting.setScreenPreset("MIDDLE");
        setting.setDifficulty("NORMAL");
        
        // 모델 업데이트
        model.updateModelFromSettings();
        
        // 모델이 Setting의 값과 일치하는지 확인
        assertEquals("모델의 색맹 모드가 Setting과 일치해야 합니다", 
                    true, model.isColorBlind());
        assertEquals("모델의 키 레이아웃이 Setting과 일치해야 합니다", 
                    "WASD", model.getKeyLayout());
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
