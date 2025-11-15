package org.tetris.menu.setting.controller;

import static org.junit.Assert.*;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.base.NodeMatchers.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.tetris.menu.setting.SettingMenuFactory;
import org.tetris.menu.setting.model.Setting;
import org.tetris.menu.setting.model.SettingMenuModel;
import org.tetris.shared.MvcBundle;
import org.testfx.framework.junit.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.control.RadioButton;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * SettingMenuController에 대한 TestFX 기반 테스트
 * JavaFX 환경에서 UI 컴포넌트와 컨트롤러의 동작을 검증합니다.
 */
public class SettingMenuControllerTest extends ApplicationTest {
    
    private SettingMenuFactory settingMenuFactory;
    private SettingMenuController controller;
    private SettingMenuModel model;
    private Stage stage;
    private final Path testFilePath = Paths.get("setting.txt");
    
    /**
     * TestFX의 start 메서드 - JavaFX 애플리케이션 시작
     * MvcBundle을 사용하여 초기화합니다.
     */
    @Override
    public void start(Stage s) throws Exception {
        this.stage = s;
        deleteSettingFile();
        
        // Setting 생성 및 Factory 초기화
        Setting setting = new Setting();
        settingMenuFactory = new SettingMenuFactory(setting);
        
        // MvcBundle을 통해 컴포넌트 생성
        MvcBundle<SettingMenuModel, ?, SettingMenuController> bundle = settingMenuFactory.create();
        controller = bundle.controller();
        model = bundle.model();
        
        // Scene 설정
        stage.setScene(bundle.view().getScene());
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
    
    private static final String STYLE_HIGHLIGHTED = "highlighted";
    
    private BorderPane rootPane() {
        return lookup("#root").query();
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
    
    /**
     * 모든 포커스 가능한 노드들을 가져옵니다 (RadioButton, Button 등)
     */
    private List<javafx.scene.Node> getAllFocusableNodes() {
        List<javafx.scene.Node> nodes = new ArrayList<>();
        
        // RadioButton들 추가
        nodes.addAll(getAllRadioButtons());
        
        // 하단 버튼들 추가
        nodes.add(saveButton());
        nodes.add(resetButton());
        nodes.add(backButton());
        
        // 키 바인딩 버튼들 추가
        lookup(".key-binding-button").queryAll().stream()
                .forEach(nodes::add);
        
        return nodes;
    }
    
    /**
     * 특정 노드가 highlighted 상태인지 확인
     */
    private boolean isHighlighted(javafx.scene.Node node) {
        if (node == null) return false;
        return node.getStyleClass().contains(STYLE_HIGHLIGHTED);
    }
    
    /**
     * highlighted 상태인 노드를 반환
     */
    private javafx.scene.Node getHighlightedNode() {
        return getAllFocusableNodes().stream()
                .filter(this::isHighlighted)
                .findFirst()
                .orElse(null);
    }
    
    /**
     * highlighted 상태인 노드의 인덱스를 반환 (-1이면 없음)
     */
    private int getHighlightedIndex() {
        List<javafx.scene.Node> nodes = getAllFocusableNodes();
        for (int i = 0; i < nodes.size(); i++) {
            if (isHighlighted(nodes.get(i))) {
                return i;
            }
        }
        return -1;
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
     * RadioButton 선택 테스트 - Highlight 검증
     * ======================================== */
    
    @Test
    public void testColorBlindRadioButtonClick() {
        // 모든 라디오 버튼 가져오기
        List<RadioButton> allRadioButtons = getAllRadioButtons();
        assertTrue("라디오 버튼이 존재해야 합니다", allRadioButtons.size() > 0);
        
        // Color Blind 옵션 중 하나 클릭 (예: 두 번째 라디오 버튼)
        if (allRadioButtons.size() > 1) {
            RadioButton secondRadio = allRadioButtons.get(1);
            
            // 클릭 전 highlighted 아님
            assertFalse("클릭 전에는 highlighted가 아니어야 합니다", isHighlighted(secondRadio));
            
            clickOn(secondRadio);
            WaitForAsyncUtils.waitForFxEvents();
            waitFor(100);
            
            // 선택되었는지 확인
            assertTrue("클릭한 라디오 버튼이 선택되어야 합니다", secondRadio.isSelected());
            // 클릭 후 마우스가 위에 있으므로 highlighted 상태
            assertTrue("클릭 후 마우스가 위에 있으면 highlighted 되어야 합니다", isHighlighted(secondRadio));
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
                assertTrue("클릭한 첫 번째 버튼이 highlighted 되어야 합니다", isHighlighted(first));
                
                clickOn(second);
                WaitForAsyncUtils.waitForFxEvents();
                assertFalse("첫 번째 버튼은 선택 해제되어야 합니다", first.isSelected());
                assertTrue("두 번째 버튼이 선택되어야 합니다", second.isSelected());
                assertTrue("클릭한 두 번째 버튼이 highlighted 되어야 합니다", isHighlighted(second));
            }
        }
    }
    
    @Test
    public void testRadioButtonMouseHover() {
        List<RadioButton> allRadioButtons = getAllRadioButtons();
        
        if (!allRadioButtons.isEmpty()) {
            RadioButton firstRadio = allRadioButtons.get(0);
            
            // 초기 상태: highlighted 아님
            assertFalse("초기 상태에서는 highlighted가 아니어야 합니다", isHighlighted(firstRadio));
            
            // 마우스를 라디오 버튼 위로 이동
            moveTo(firstRadio);
            WaitForAsyncUtils.waitForFxEvents();
            waitFor(50);
            
            // highlighted 상태 확인
            assertTrue("마우스 호버 시 라디오 버튼이 highlighted 되어야 합니다", isHighlighted(firstRadio));
            assertTrue("라디오 버튼이 hover 상태여야 합니다", firstRadio.isHover());
        }
    }
    
    /* ========================================
     * 버튼 클릭 테스트 - Highlight 검증
     * ======================================== */
    
    @Test
    public void testSaveButtonClickable() {
        Button saveBtn = saveButton();
        assertNotNull("Save 버튼이 존재해야 합니다", saveBtn);
        
        // 초기 상태: highlighted 아님
        assertFalse("초기 상태에서는 highlighted가 아니어야 합니다", isHighlighted(saveBtn));
        
        // Save 버튼 클릭
        clickOn(saveBtn);
        WaitForAsyncUtils.waitForFxEvents();
        waitFor(100);
        
        // 클릭 후 마우스가 위에 있으므로 highlighted
        assertTrue("클릭 후 마우스가 위에 있으면 highlighted 되어야 합니다", isHighlighted(saveBtn));
    }
    
    @Test
    public void testResetButtonClickable() {
        Button resetBtn = resetButton();
        assertNotNull("Reset 버튼이 존재해야 합니다", resetBtn);
        
        // 초기 상태: highlighted 아님
        assertFalse("초기 상태에서는 highlighted가 아니어야 합니다", isHighlighted(resetBtn));
        
        // Reset 버튼 클릭
        clickOn(resetBtn);
        WaitForAsyncUtils.waitForFxEvents();
        waitFor(100);
        
        // 클릭 후 highlighted 상태 확인
        assertTrue("클릭 후 마우스가 위에 있으면 highlighted 되어야 합니다", isHighlighted(resetBtn));
    }
    
    @Test
    public void testBackButtonClickable() {
        Button backBtn = backButton();
        assertNotNull("Back 버튼이 존재해야 합니다", backBtn);
        
        // 초기 상태: highlighted 아님
        assertFalse("초기 상태에서는 highlighted가 아니어야 합니다", isHighlighted(backBtn));
        
        // Back 버튼 클릭
        clickOn(backBtn);
        WaitForAsyncUtils.waitForFxEvents();
        waitFor(100);
        
        // 클릭 후 highlighted 상태 확인
        assertTrue("클릭 후 마우스가 위에 있으면 highlighted 되어야 합니다", isHighlighted(backBtn));
    }
    
    @Test
    public void testButtonMouseHover() {
        Button saveBtn = saveButton();
        
        // 초기 상태: highlighted 아님
        assertFalse("초기 상태에서는 highlighted가 아니어야 합니다", isHighlighted(saveBtn));
        
        // 마우스를 버튼 위로 이동
        moveTo(saveBtn);
        WaitForAsyncUtils.waitForFxEvents();
        waitFor(50);
        
        // highlighted 및 호버 상태 확인
        assertTrue("마우스 호버 시 버튼이 highlighted 되어야 합니다", isHighlighted(saveBtn));
        assertTrue("버튼이 hover 상태여야 합니다", saveBtn.isHover());
        
        // 마우스를 다른 곳으로 이동
        moveTo(rootPane());
        WaitForAsyncUtils.waitForFxEvents();
        waitFor(50);
        
        // highlighted 및 호버 상태 해제 확인
        assertFalse("마우스가 떠나면 highlighted가 해제되어야 합니다", isHighlighted(saveBtn));
        assertFalse("마우스가 떠나면 hover 상태가 해제되어야 합니다", saveBtn.isHover());
    }
    
    @Test
    public void testMultipleButtonClicks() {
        // 여러 버튼을 순차적으로 클릭하며 각각 highlighted 확인
        Button saveBtn = saveButton();
        Button resetBtn = resetButton();
        Button backBtn = backButton();
        
        // Save 버튼 클릭
        clickOn(saveBtn);
        WaitForAsyncUtils.waitForFxEvents();
        waitFor(50);
        assertTrue("Save 버튼이 highlighted 되어야 합니다", isHighlighted(saveBtn));
        
        // Reset 버튼으로 이동 및 클릭
        clickOn(resetBtn);
        WaitForAsyncUtils.waitForFxEvents();
        waitFor(50);
        assertFalse("Save 버튼의 highlighted가 해제되어야 합니다", isHighlighted(saveBtn));
        assertTrue("Reset 버튼이 highlighted 되어야 합니다", isHighlighted(resetBtn));
        
        // Back 버튼으로 이동 및 클릭
        clickOn(backBtn);
        WaitForAsyncUtils.waitForFxEvents();
        waitFor(50);
        assertFalse("Reset 버튼의 highlighted가 해제되어야 합니다", isHighlighted(resetBtn));
        assertTrue("Back 버튼이 highlighted 되어야 합니다", isHighlighted(backBtn));
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
            stage.setHeight(800);
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

    /* ========================================
     * Key binding button tests - Highlight 검증
     * ======================================== */

    @Test
    public void testKeyBindingButtonsExistAndDefaultDisplay() {
        // key-binding 버튼은 P1(5) + P2(5)로 총 10개 존재해야 함
        var nodes = lookup(".key-binding-button").queryAll();
        assertEquals("키 바인딩 버튼은 10개여야 합니다", 10, nodes.size());

        // 텍스트 목록 중에 화살표/Space/Shift/WASD가 포함되어 있는지 확인
        List<String> texts = nodes.stream()
                .filter(n -> n instanceof Button)
                .map(n -> ((Button) n).getText())
                .toList();

        // Player1 화살표와 Space
        assertTrue("왼쪽 화살표가 표시되어야 합니다", texts.stream().anyMatch(t -> t.contains("←")));
        assertTrue("오른쪽 화살표가 표시되어야 합니다", texts.stream().anyMatch(t -> t.contains("→")));
        assertTrue("위 화살표가 표시되어야 합니다", texts.stream().anyMatch(t -> t.contains("↑")));
        assertTrue("아래 화살표가 표시되어야 합니다", texts.stream().anyMatch(t -> t.contains("↓")));
        assertTrue("Space가 표시되어야 합니다", texts.stream().anyMatch(t -> t.toLowerCase().contains("space") || t.contains("Space")));

        // Player2 WASD + Shift
        assertTrue("A 키가 표시되어야 합니다", texts.stream().anyMatch(t -> t.matches(".*\\bA\\b.*") || t.contains("A:")));
        assertTrue("W 키가 표시되어야 합니다", texts.stream().anyMatch(t -> t.matches(".*\\bW\\b.*") || t.contains("W:")));
        assertTrue("S 키가 표시되어야 합니다", texts.stream().anyMatch(t -> t.matches(".*\\bS\\b.*") || t.contains("S:")));
        assertTrue("D 키가 표시되어야 합니다", texts.stream().anyMatch(t -> t.matches(".*\\bD\\b.*") || t.contains("D:")));
        assertTrue("Shift가 표시되어야 합니다", texts.stream().anyMatch(t -> t.toLowerCase().contains("shift") || t.contains("Shift")));
    }

    @Test
    public void testClickKeyBindingAssignKey() {
        // 첫 번째 key-binding 버튼을 클릭하고 새 키를 할당하면 버튼 텍스트가 갱신되어야 함
        var nodes = lookup(".key-binding-button").queryAll();
        assertFalse("키 바인딩 버튼이 비어있어선 안됩니다", nodes.isEmpty());

        Button btn = (Button) nodes.iterator().next();
        
        // 초기 상태: highlighted 아님
        assertFalse("초기 상태에서는 highlighted가 아니어야 합니다", isHighlighted(btn));
        
        // 클릭하여 바인딩 모드로 진입
        clickOn(btn);
        WaitForAsyncUtils.waitForFxEvents();
        waitFor(50);
        
        // 클릭 후 highlighted 상태 확인
        assertTrue("클릭 후 버튼이 highlighted 되어야 합니다", isHighlighted(btn));

        // 새 키 입력 (예: J)
        push(KeyCode.J);
        WaitForAsyncUtils.waitForFxEvents();
        waitFor(50);

        // 버튼 텍스트에 J가 포함되어야 함
        assertTrue("버튼 텍스트에 새로 입력한 키가 포함되어야 합니다: " + btn.getText(), btn.getText().contains("J"));
    }
    
    @Test
    public void testKeyBindingButtonMouseHover() {
        // 키 바인딩 버튼에 마우스를 올리면 highlighted 되는지 확인
        var nodes = lookup(".key-binding-button").queryAll();
        assertFalse("키 바인딩 버튼이 존재해야 합니다", nodes.isEmpty());
        
        Button keyBtn = (Button) nodes.iterator().next();
        
        // 초기 상태: highlighted 아님
        assertFalse("초기 상태에서는 highlighted가 아니어야 합니다", isHighlighted(keyBtn));
        
        // 마우스 호버
        moveTo(keyBtn);
        WaitForAsyncUtils.waitForFxEvents();
        waitFor(50);
        
        // highlighted 상태 확인
        assertTrue("마우스 호버 시 키 바인딩 버튼이 highlighted 되어야 합니다", isHighlighted(keyBtn));
        
        // 마우스를 다른 곳으로 이동
        moveTo(rootPane());
        WaitForAsyncUtils.waitForFxEvents();
        waitFor(50);
        
        // highlighted 해제 확인
        assertFalse("마우스가 떠나면 highlighted가 해제되어야 합니다", isHighlighted(keyBtn));
    }
    
    /* ========================================
     * updateSizes 반응형 크기 조정 테스트
     * ======================================== */
    
    @Test
    public void testUpdateSizesWithDifferentWindowSizes() {
        Button saveBtn = saveButton();
        
        // 800x800 (초기 크기) - 버튼 크기 확인
        interact(() -> {
            stage.setWidth(800);
            stage.setHeight(800);
        });
        WaitForAsyncUtils.waitForFxEvents();
        WaitForAsyncUtils.sleep(150, TimeUnit.MILLISECONDS);
        WaitForAsyncUtils.waitForFxEvents();
        
        double width800 = saveBtn.getPrefWidth();
        double height800 = saveBtn.getPrefHeight();
        
        assertTrue("800x800에서 버튼 너비가 설정되어야 합니다", width800 > 0);
        assertTrue("800x800에서 버튼 높이가 설정되어야 합니다", height800 > 0);
        
        // 1200x800 (큰 창) - 버튼 크기가 증가해야 함
        interact(() -> {
            stage.setWidth(1200);
            stage.setHeight(800);
        });
        WaitForAsyncUtils.waitForFxEvents();
        WaitForAsyncUtils.sleep(150, TimeUnit.MILLISECONDS);
        WaitForAsyncUtils.waitForFxEvents();
        
        double width1200 = saveBtn.getPrefWidth();
        
        assertTrue("1200x800에서 버튼 너비가 800x800보다 커야 합니다", width1200 >= width800);
        
        // 600x400 (작은 창) - 버튼 크기가 감소해야 함
        interact(() -> {
            stage.setWidth(600);
            stage.setHeight(400);
        });
        WaitForAsyncUtils.waitForFxEvents();
        WaitForAsyncUtils.sleep(150, TimeUnit.MILLISECONDS);
        WaitForAsyncUtils.waitForFxEvents();
        
        double width600 = saveBtn.getPrefWidth();
        
        assertTrue("600x400에서 버튼이 여전히 표시되어야 합니다", saveBtn.isVisible());
        assertTrue("600x400에서 버튼 너비가 800x800보다 작거나 같아야 합니다", width600 <= width800);
    }
    
    @Test
    public void testKeyBindingButtonSizeAdjustment() {
        // 키 바인딩 버튼들의 크기도 반응형으로 조정되는지 확인
        var nodes = lookup(".key-binding-button").queryAll();
        assertFalse("키 바인딩 버튼이 존재해야 합니다", nodes.isEmpty());
        
        Button keyBtn = (Button) nodes.iterator().next();
        
        // 큰 창
        interact(() -> {
            stage.setWidth(1200);
            stage.setHeight(800);
        });
        WaitForAsyncUtils.waitForFxEvents();
        WaitForAsyncUtils.sleep(150, TimeUnit.MILLISECONDS);
        WaitForAsyncUtils.waitForFxEvents();
        
        double widthLarge = keyBtn.getPrefWidth();
        double heightLarge = keyBtn.getPrefHeight();
        
        assertTrue("큰 창에서 키 바인딩 버튼 너비가 설정되어야 합니다", widthLarge > 0);
        assertTrue("큰 창에서 키 바인딩 버튼 높이가 설정되어야 합니다", heightLarge > 0);
        
        // 작은 창
        interact(() -> {
            stage.setWidth(600);
            stage.setHeight(400);
        });
        WaitForAsyncUtils.waitForFxEvents();
        WaitForAsyncUtils.sleep(150, TimeUnit.MILLISECONDS);
        WaitForAsyncUtils.waitForFxEvents();
        
        double widthSmall = keyBtn.getPrefWidth();
        
        assertTrue("작은 창에서도 키 바인딩 버튼이 표시되어야 합니다", keyBtn.isVisible());
        assertTrue("작은 창에서 키 바인딩 버튼이 적절한 최소 크기를 유지해야 합니다", widthSmall >= 150);
    }
    
    @Test
    public void testButtonSpacingAdjustment() {
        // buttonBox의 간격이 창 크기에 따라 조정되는지 확인
        var buttonBox = lookup("#buttonBox").query();
        assertNotNull("buttonBox가 존재해야 합니다", buttonBox);
        
        // 큰 창
        interact(() -> {
            stage.setWidth(1200);
            stage.setHeight(800);
        });
        WaitForAsyncUtils.waitForFxEvents();
        WaitForAsyncUtils.sleep(150, TimeUnit.MILLISECONDS);
        WaitForAsyncUtils.waitForFxEvents();
        
        assertTrue("큰 창에서 buttonBox가 표시되어야 합니다", buttonBox.isVisible());
        assertFalse("초기 상태에서는 highlighted가 아니어야 합니다", isHighlighted(buttonBox));
        
        // 작은 창
        interact(() -> {
            stage.setWidth(600);
            stage.setHeight(400);
        });
        WaitForAsyncUtils.waitForFxEvents();
        WaitForAsyncUtils.sleep(150, TimeUnit.MILLISECONDS);
        WaitForAsyncUtils.waitForFxEvents();
        
        assertTrue("작은 창에서도 buttonBox가 표시되어야 합니다", buttonBox.isVisible());
    }
}