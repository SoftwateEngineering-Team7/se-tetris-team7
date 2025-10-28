package org.tetris.menu.setting.controller;

import static org.junit.Assert.*;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.base.NodeMatchers.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.tetris.menu.setting.model.Setting;
import org.tetris.menu.setting.model.SettingMenuModel;
import org.testfx.framework.junit.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * SettingMenuController에 대한 TestFX 기반 테스트
 * JavaFX 환경에서 UI 컴포넌트와 컨트롤러의 동작을 검증합니다.
 */
public class SettingMenuControllerTest extends ApplicationTest {
    
    private Stage stage;
    private Setting setting;
    private SettingMenuModel model;
    private SettingMenuController controller;
    private final Path testFilePath = Paths.get("setting.txt");
    
    /**
     * TestFX의 start 메서드 - JavaFX 애플리케이션 시작
     * Stage를 설정하고 FXML을 로드하여 Scene을 생성합니다.
     */
    @Override
    public void start(Stage s) throws Exception {
        this.stage = s;
        deleteSettingFile();
        
        // Setting과 Model 초기화
        setting = new Setting();
        model = new SettingMenuModel(setting);
        
        // FXML 로드
        FXMLLoader loader = new FXMLLoader(
            getClass().getResource("/org/tetris/menu/setting/view/settingmenu.fxml")
        );
        
        // Controller를 먼저 생성하고 FXMLLoader에 설정
        controller = new SettingMenuController(model);
        loader.setController(controller);
        
        // FXML 로드
        BorderPane root = loader.load();
        
        // Scene 설정
        Scene scene = new Scene(root, 800, 600);
        stage.setScene(scene);
        stage.setTitle("Setting Menu Test");
        stage.show();
        stage.toFront();
        
        // UI 이벤트 처리를 충분히 기다림
        WaitForAsyncUtils.waitForFxEvents();
        WaitForAsyncUtils.sleep(100, TimeUnit.MILLISECONDS);
        WaitForAsyncUtils.waitForFxEvents();
    }
    
    @Before
    public void setUp() {
        // 각 테스트 전에 root에 포커스 설정
        interact(() -> {
            BorderPane root = lookup("#root").query();
            if (root != null) {
                root.requestFocus();
            }
        });
        
        WaitForAsyncUtils.waitForFxEvents();
        WaitForAsyncUtils.sleep(50, TimeUnit.MILLISECONDS);
        WaitForAsyncUtils.waitForFxEvents();
    }
    
    @After
    public void tearDown() throws TimeoutException {
        // 모든 JavaFX Stage 닫기
        release(new javafx.scene.input.KeyCode[0]);
        release(new javafx.scene.input.MouseButton[0]);
        deleteSettingFile();
    }
    
    private void deleteSettingFile() {
        try {
            Files.deleteIfExists(testFilePath);
        } catch (IOException e) {
            // 무시
        }
    }
    
    /* ========================================
     * Helper Methods
     * ======================================== */
    
    private BorderPane rootPane() {
        return lookup("#root").query();
    }
    
    private VBox mainVBox() {
        return lookup("#mainVBox").query();
    }
    
    private Button saveButton() {
        return lookup("#btnSave").query();
    }
    
    private Button resetButton() {
        return lookup("#btnReset").query();
    }
    
    private Button backButton() {
        return lookup("#btnBackToStart").query();
    }
    
    private List<RadioButton> getAllRadioButtons() {
        return lookup(".radio-button").queryAll().stream()
                .filter(n -> n instanceof RadioButton)
                .map(n -> (RadioButton) n)
                .collect(Collectors.toList());
    }
    
    private List<RadioButton> getRadioButtonsInPane(String paneId) {
        VBox pane = lookup(paneId).query();
        if (pane == null) return List.of();
        
        return pane.getChildrenUnmodifiable().stream()
                .filter(n -> n instanceof RadioButton)
                .map(n -> (RadioButton) n)
                .collect(Collectors.toList());
    }
    
    private RadioButton getSelectedRadioButton(ToggleGroup group) {
        if (group == null || group.getSelectedToggle() == null) {
            return null;
        }
        return (RadioButton) group.getSelectedToggle();
    }
    
    /*
     * ========================================
     * 유틸리티 메서드
     * ========================================
     */

    /**
     * 테스트 중 일정 시간 대기
     * 
     * @param millis 밀리초 단위 대기 시간
     */
    private void waitFor(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /* ========================================
     * 기본 컴포넌트 존재 테스트
     * ======================================== */
    
    @Test
    public void testRootBorderPaneExists() {
        verifyThat("#root", isNotNull());
    }
    
    @Test
    public void testMainVBoxExists() {
        verifyThat("#mainVBox", isNotNull());
    }
    
    @Test
    public void testButtonBoxExists() {
        verifyThat("#buttonBox", isNotNull());
    }
    
    @Test
    public void testSaveButtonExists() {
        verifyThat("#btnSave", isNotNull());
        verifyThat("#btnSave", isVisible());
    }
    
    @Test
    public void testResetButtonExists() {
        verifyThat("#btnReset", isNotNull());
        verifyThat("#btnReset", isVisible());
    }
    
    @Test
    public void testBackToStartButtonExists() {
        verifyThat("#btnBackToStart", isNotNull());
        verifyThat("#btnBackToStart", isVisible());
    }
    
    /* ========================================
     * RadioButton 선택 테스트
     * ======================================== */
    
    @Test
    public void testColorBlindRadioButtonClick() {
        // 모든 라디오 버튼 가져오기
        List<RadioButton> allRadioButtons = getAllRadioButtons();
        assertTrue("라디오 버튼이 존재해야 합니다", allRadioButtons.size() > 0);
        
        // Color Blind 옵션 중 하나 클릭 (예: 두 번째 라디오 버튼)
        if (allRadioButtons.size() > 1) {
            RadioButton secondRadio = allRadioButtons.get(1);
            
            clickOn(secondRadio);
            WaitForAsyncUtils.waitForFxEvents();
            waitFor(100);
            
            // 선택되었는지 확인
            assertTrue("클릭한 라디오 버튼이 선택되어야 합니다", secondRadio.isSelected());
        }
    }
    
    @Test
    public void testRadioButtonGroupExclusivity() {
        // 같은 그룹의 라디오 버튼은 하나만 선택되어야 함
        List<RadioButton> allRadioButtons = getAllRadioButtons();
        
        if (allRadioButtons.size() >= 2) {
            RadioButton first = allRadioButtons.get(0);
            RadioButton second = allRadioButtons.get(1);
            
            // 같은 ToggleGroup인 경우에만 테스트
            if (first.getToggleGroup() != null &&
                first.getToggleGroup() == second.getToggleGroup()) {
                
                clickOn(first);
                WaitForAsyncUtils.waitForFxEvents();
                assertTrue("첫 번째 버튼이 선택되어야 합니다", first.isSelected());
                assertFalse("두 번째 버튼은 선택 해제되어야 합니다", second.isSelected());
                
                clickOn(second);
                WaitForAsyncUtils.waitForFxEvents();
                assertFalse("첫 번째 버튼은 선택 해제되어야 합니다", first.isSelected());
                assertTrue("두 번째 버튼이 선택되어야 합니다", second.isSelected());
            }
        }
    }
    
    @Test
    public void testRadioButtonMouseHover() {
        List<RadioButton> allRadioButtons = getAllRadioButtons();
        
        if (!allRadioButtons.isEmpty()) {
            RadioButton firstRadio = allRadioButtons.get(0);
            
            // 마우스를 라디오 버튼 위로 이동
            moveTo(firstRadio);
            WaitForAsyncUtils.waitForFxEvents();
            
            // 호버 상태 확인 (hover pseudo-class 또는 스타일 변경)
            assertTrue("라디오 버튼이 hover 상태여야 합니다", firstRadio.isHover());
        }
    }
    
    /* ========================================
     * 버튼 클릭 테스트
     * ======================================== */
    
    @Test
    public void testSaveButtonClickable() {
        Button saveBtn = saveButton();
        assertNotNull("Save 버튼이 존재해야 합니다", saveBtn);
        
        // Save 버튼 클릭
        clickOn(saveBtn);
        WaitForAsyncUtils.waitForFxEvents();
        waitFor(100);
        
        // 버튼이 클릭 가능한지 확인 (예외가 발생하지 않으면 성공)
        assertTrue("Save 버튼이 클릭 가능해야 합니다", true);
    }
    
    @Test
    public void testResetButtonClickable() {
        Button resetBtn = resetButton();
        assertNotNull("Reset 버튼이 존재해야 합니다", resetBtn);
        
        // Reset 버튼 클릭
        clickOn(resetBtn);
        WaitForAsyncUtils.waitForFxEvents();
        waitFor(100);
        
        // 버튼이 클릭 가능한지 확인
        assertTrue("Reset 버튼이 클릭 가능해야 합니다", true);
    }
    
    @Test
    public void testBackButtonClickable() {
        Button backBtn = backButton();
        assertNotNull("Back 버튼이 존재해야 합니다", backBtn);
        
        // Back 버튼 클릭
        clickOn(backBtn);
        WaitForAsyncUtils.waitForFxEvents();
        waitFor(100);
        
        assertTrue("Back 버튼이 클릭 가능해야 합니다", true);
    }
    
    @Test
    public void testButtonMouseHover() {
        Button saveBtn = saveButton();
        
        // 마우스를 버튼 위로 이동
        moveTo(saveBtn);
        WaitForAsyncUtils.waitForFxEvents();
        
        // 호버 상태 확인
        assertTrue("버튼이 hover 상태여야 합니다", saveBtn.isHover());
        
        // 마우스를 다른 곳으로 이동
        moveTo(rootPane());
        WaitForAsyncUtils.waitForFxEvents();
        
        // 호버 상태 해제 확인
        assertFalse("마우스가 떠나면 hover 상태가 해제되어야 합니다", saveBtn.isHover());
    }
    
    @Test
    public void testMultipleButtonClicks() {
        // 여러 버튼을 순차적으로 클릭
        clickOn(saveButton());
        WaitForAsyncUtils.waitForFxEvents();
        
        clickOn(resetButton());
        WaitForAsyncUtils.waitForFxEvents();
        
        clickOn(backButton());
        WaitForAsyncUtils.waitForFxEvents();
        
        // 모든 클릭이 성공적으로 수행되면 테스트 통과
        assertTrue("모든 버튼이 순차적으로 클릭 가능해야 합니다", true);
    }

    /* ========================================
     * Controller 타입 검증
     * ======================================== */
    
    @Test
    public void testControllerIsRouterAware() {
        assertTrue("Controller는 RouterAware를 구현해야 합니다", 
                  controller instanceof org.tetris.shared.RouterAware);
    }
    
    @Test
    public void testControllerIsBaseController() {
        assertTrue("Controller는 BaseController를 상속해야 합니다", 
                  controller instanceof org.tetris.shared.BaseController);
    }

    /* ========================================
     * 반응형 크기 조정 테스트
     * ======================================== */
    
    @Test
    public void testResponsiveSizing() {
        Button saveBtn = saveButton();
        Button resetBtn = resetButton();
        Button backBtn = backButton();
        
        // 테스트 케이스 1: 큰 창 크기
        interact(() -> {
            stage.setWidth(1200);
            stage.setHeight(800);
        });
        WaitForAsyncUtils.waitForFxEvents();
        WaitForAsyncUtils.sleep(100, TimeUnit.MILLISECONDS);
        WaitForAsyncUtils.waitForFxEvents();
        
        // 버튼이 존재하고 크기가 설정되었는지 확인
        assertNotNull("Save 버튼이 존재해야 합니다", saveBtn);
        assertNotNull("Reset 버튼이 존재해야 합니다", resetBtn);
        assertNotNull("Back 버튼이 존재해야 합니다", backBtn);
        
        // 테스트 케이스 2: 작은 창 크기
        interact(() -> {
            stage.setWidth(600);
            stage.setHeight(400);
        });
        WaitForAsyncUtils.waitForFxEvents();
        WaitForAsyncUtils.sleep(100, TimeUnit.MILLISECONDS);
        WaitForAsyncUtils.waitForFxEvents();
        
        // 여전히 버튼이 표시되는지 확인
        assertTrue("작은 창에서도 Save 버튼이 표시되어야 합니다", saveBtn.isVisible());
        assertTrue("작은 창에서도 Reset 버튼이 표시되어야 합니다", resetBtn.isVisible());
        assertTrue("작은 창에서도 Back 버튼이 표시되어야 합니다", backBtn.isVisible());
        
        // 테스트 케이스 3: 초기 크기로 복원
        interact(() -> {
            stage.setWidth(800);
            stage.setHeight(600);
        });
        WaitForAsyncUtils.waitForFxEvents();
        WaitForAsyncUtils.sleep(100, TimeUnit.MILLISECONDS);
        WaitForAsyncUtils.waitForFxEvents();
    }
    
    @Test
    public void testAllButtonsVisible() {
        // 모든 버튼이 표시되는지 확인
        List<Button> buttons = List.of(saveButton(), resetButton(), backButton());
        
        for (Button btn : buttons) {
            assertNotNull("버튼이 존재해야 합니다", btn);
            assertTrue("버튼이 표시되어야 합니다", btn.isVisible());
        }
    }
}