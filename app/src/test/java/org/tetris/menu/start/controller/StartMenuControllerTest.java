package org.tetris.menu.start.controller;

import org.junit.Before;
import org.junit.Test;
import org.tetris.menu.start.model.StartMenuModel;

import static org.junit.Assert.*;

public class StartMenuControllerTest {

    private StartMenuController controller;
    private StartMenuModel model;

    @Before
    public void setUp() {
        controller = new StartMenuController();
        model = new StartMenuModel(3);
    }

    @Test
    public void testControllerCreation() {
        assertNotNull("Controller가 생성되어야 합니다", controller);
    }

    @Test
    public void testModelIntegration() {
        assertEquals("초기 인덱스는 0", 0, model.getSelectedIndex());
        model.move(1);
        assertEquals("인덱스는 1", 1, model.getSelectedIndex());
        model.move(2);
        assertEquals("인덱스는 0으로 순환", 0, model.getSelectedIndex());
    }

    @Test
    public void testPackageStructure() {
        assertEquals("패키지명이 일치해야 합니다",
                "org.tetris.menu.start.controller",
                StartMenuController.class.getPackage().getName());
    }

    @Test
    public void testFXMLActionMethodsExist() {
        try {
            StartMenuController.class.getDeclaredMethod("onGameStart");
            StartMenuController.class.getDeclaredMethod("onSettings");
            StartMenuController.class.getDeclaredMethod("onExit");
        } catch (NoSuchMethodException e) {
            fail("필요한 FXML Action 메서드 누락: " + e.getMessage());
        }
    }
}
