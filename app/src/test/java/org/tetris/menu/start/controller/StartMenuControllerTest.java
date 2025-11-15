package org.tetris.menu.start.controller;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import org.junit.Before;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;
import org.tetris.menu.start.model.StartMenuModel;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

/**
 * StartMenuController 단위 테스트
 * Router 관련 기능을 제외한 기본 UI 및 네비게이션 테스트
 * - 4개의 메뉴 버튼 렌더링
 * - 키보드 네비게이션 (UP/DOWN)
 * - 마우스 호버 하이라이트
 * - 잘못된 입력 경고 메시지
 * 
 * Note: ENTER 키 테스트는 Router 의존성 때문에 제외
 */
public class StartMenuControllerTest extends ApplicationTest {
    private static final String FXML_PATH = "/org/tetris/menu/start/view/startmenu.fxml";
    private static final int EXPECTED_BUTTON_COUNT = 6;
    private static final String STYLE_HIGHLIGHTED = "highlighted";

    private Stage stage;
    private StartMenuController controller;

    @Override
    public void start(Stage s) throws Exception {
        stage = s;
        
        // Model과 Controller 직접 생성
        StartMenuModel model = new StartMenuModel(EXPECTED_BUTTON_COUNT);
        controller = new StartMenuController(model);
        
        // FXML 로드
        FXMLLoader loader = new FXMLLoader(getClass().getResource(FXML_PATH));
        loader.setController(controller);
        Parent root = loader.load();
        
        stage.setScene(new Scene(root, 900, 600));
        stage.show();
        stage.toFront();
        
        // UI 이벤트 처리를 충분히 기다림
        WaitForAsyncUtils.waitForFxEvents();
        WaitForAsyncUtils.sleep(100, TimeUnit.MILLISECONDS);
        WaitForAsyncUtils.waitForFxEvents();
    }

    @Before
    public void setUp() {
        // root에 포커스 설정 (키 이벤트가 제대로 처리되도록)
        interact(() -> {
            StackPane root = rootPane();
            if (root != null) {
                root.requestFocus();
            }
            
            // 각 테스트 전에 초기 상태로 리셋: 첫 번째 버튼만 하이라이트
            List<Button> buttons = getButtons();
            if (!buttons.isEmpty()) {
                // 모든 버튼의 하이라이트 제거
                for (Button btn : buttons) {
                    btn.getStyleClass().remove(STYLE_HIGHLIGHTED);
                }
                // 첫 번째 버튼 하이라이트
                buttons.get(0).getStyleClass().add(STYLE_HIGHLIGHTED);
                
                // Controller의 Model도 리셋 (리플렉션 사용)
                try {
                    java.lang.reflect.Field modelField = controller.getClass().getSuperclass().getDeclaredField("model");
                    modelField.setAccessible(true);
                    StartMenuModel model = (StartMenuModel) modelField.get(controller);
                    model.setSelectedIndex(0);
                } catch (Exception e) {
                    System.err.println("Model 리셋 실패: " + e.getMessage());
                }
            }
        });
        
        // Platform.runLater로 설정된 초기 하이라이트가 완전히 적용될 때까지 대기
        WaitForAsyncUtils.waitForFxEvents();
        WaitForAsyncUtils.sleep(50, TimeUnit.MILLISECONDS);
        WaitForAsyncUtils.waitForFxEvents();
    }

    // ------ Helper Methods ------

    private StackPane rootPane() {
        return lookup("#root").query();
    }

    private VBox menuBox() {
        return lookup("#menuBox").query();
    }

    private Label titleLabel() {
        return lookup("#titleLabel").query();
    }

    private Label wrongInputLabel() {
        return lookup("#wrongInputLabel").query();
    }

    private List<Button> getButtons() {
        VBox box = menuBox();
        return box.getChildren().stream()
                .filter(n -> n instanceof Button)
                .map(n -> (Button) n)
                .collect(Collectors.toList());
    }

    private int getHighlightedIndex() {
        List<Button> buttons = getButtons();
        for (int i = 0; i < buttons.size(); i++) {
            if (buttons.get(i).getStyleClass().contains(STYLE_HIGHLIGHTED)) {
                return i;
            }
        }
        return -1;
    }

    private Button getButtonAt(int index) {
        List<Button> buttons = getButtons();
        if (index >= 0 && index < buttons.size()) {
            return buttons.get(index);
        }
        return null;
    }

    private boolean isHighlighted(int index) {
        Button button = getButtonAt(index);
        return button != null && button.getStyleClass().contains(STYLE_HIGHLIGHTED);
    }

    // ------ Tests ------

    @Test
    public void testInitialRendering() {
        // UI 요소들이 제대로 렌더링되었는지 확인
        assertNotNull("루트 패널이 존재해야 합니다.", rootPane());
        assertNotNull("타이틀 레이블이 존재해야 합니다.", titleLabel());
        assertNotNull("메뉴 박스가 존재해야 합니다.", menuBox());
        assertNotNull("잘못된 입력 레이블이 존재해야 합니다.", wrongInputLabel());

        List<Button> buttons = getButtons();
        assertEquals("메뉴 버튼이 6개여야 합니다.", EXPECTED_BUTTON_COUNT, buttons.size());
    }

    @Test
    public void testDownKey() {
        // 초기 상태: 첫 번째 버튼 하이라이트
        int initialIndex = getHighlightedIndex();
        assertTrue("첫 번째 버튼이 하이라이트 인덱스여야 합니다 (actual: " + initialIndex + ")", initialIndex == 0);
        
        press(KeyCode.DOWN).release(KeyCode.DOWN);
        WaitForAsyncUtils.waitForFxEvents();
        int afterDownIndex = getHighlightedIndex();
        assertTrue("두 번째 버튼이 하이라이트되어야 합니다 (actual: " + afterDownIndex + ")", isHighlighted(1));
    }

    @Test
    public void testUpKey() {
        // 초기 상태: 첫 번째 버튼 하이라이트
        assertTrue("첫 번째 버튼이 하이라이트 인덱스여야 합니다.", getHighlightedIndex() == 0);
        
        // UP 키 입력 (wrapping 발생)
        press(KeyCode.UP).release(KeyCode.UP);
        WaitForAsyncUtils.waitForFxEvents();
        
        // 마지막 버튼으로 이동 (Math.floorMod)
        assertTrue("마지막 버튼이 하이라이트되어야 합니다.", isHighlighted(EXPECTED_BUTTON_COUNT - 1));
    }

    @Test
    public void testMultipleKeyPresses() {
        List<Button> buttons = getButtons();
        int n = buttons.size();
        
        // 초기 인덱스
        assertTrue("첫 번째 버튼이 하이라이트 인덱스여야 합니다.", getHighlightedIndex() == 0);
        
        // UP 키 - 마지막으로 wrap
        press(KeyCode.UP).release(KeyCode.UP);
        WaitForAsyncUtils.waitForFxEvents();
        assertTrue("마지막 버튼이 하이라이트되어야 합니다.", isHighlighted(n - 1));
        
        // DOWN 키 - 다시 첫 번째로 wrap
        press(KeyCode.DOWN).release(KeyCode.DOWN);
        WaitForAsyncUtils.waitForFxEvents();
        assertTrue("첫 번째 버튼이 하이라이트되어야 합니다.", isHighlighted(0));
    }

    @Test
    public void testMouseInput() {
        List<Button> buttons = getButtons();
        assertTrue("두 개 이상의 버튼이 필요합니다.", buttons.size() >= 2);

        // 초기: 첫 번째 버튼 하이라이트
        assertTrue("첫 번째 버튼이 하이라이트되어야 합니다.", isHighlighted(0));
        
        // 두 번째 버튼에 마우스 호버
        Button secondButton = buttons.get(1);
        moveTo(secondButton);
        WaitForAsyncUtils.waitForFxEvents();
        
        // 두 번째 버튼이 하이라이트되어야 함
        assertTrue("두 번째 버튼이 하이라이트되어야 합니다.", isHighlighted(1));
    }

    // ENTER 키 테스트는 Router 의존성 때문에 제외

    @Test
    public void testInvalidKeyShowsWarning() {
        Label warning = wrongInputLabel();
        
        // 초기: 경고 메시지 숨김
        assertFalse("경고 메시지는 초기에는 숨겨져 있어야 합니다.", warning.isVisible());

        // 잘못된 키 입력 (P키)
        press(KeyCode.P).release(KeyCode.P);
        WaitForAsyncUtils.waitForFxEvents();
        
        // 경고 메시지 표시
        assertTrue("경고 메시지가 표시되어야 합니다.", warning.isVisible());
    }

    @Test
    public void testWarningAutoHides() {
        Label warning = wrongInputLabel();
        
        // 잘못된 키 입력
        press(KeyCode.P).release(KeyCode.P);
        WaitForAsyncUtils.waitForFxEvents();
        assertTrue("경고 메시지가 표시되어야 합니다.", warning.isVisible());
        
        // 2초 후 자동으로 숨김
        WaitForAsyncUtils.sleep(2300, TimeUnit.MILLISECONDS);
        WaitForAsyncUtils.waitForFxEvents();

        assertFalse("경고 메시지는 2초 후에 자동으로 숨겨져야 합니다.", warning.isVisible());
    }

    @Test
    public void testOnlyOneButtonHighlightAtATime() {
        List<Button> buttons = getButtons();
        
        // 여러 번 이동하면서 항상 하나의 버튼만 하이라이트되는지 확인
        for (int i = 0; i < buttons.size() * 2; i++) {
            press(KeyCode.DOWN).release(KeyCode.DOWN);
            WaitForAsyncUtils.waitForFxEvents();
            
            int highlightCount = 0;
            for (Button btn : buttons) {
                if (btn.getStyleClass().contains(STYLE_HIGHLIGHTED)) {
                    highlightCount++;
                }
            }

            assertEquals("하이라이트된 버튼은 하나만 있어야 합니다.", 1, highlightCount);
        }
    }

    @Test
    public void testButtonsHaveCorrectStyleClass() {
        List<Button> buttons = getButtons();
        
        for (Button button : buttons) {
            assertTrue("각 버튼은 menu-button 스타일 클래스를 가져야 합니다.",
                    button.getStyleClass().contains("menu-button"));
        }
    }

    @Test
    public void testResponsiveSizing() {
        List<Button> buttons = getButtons();
        Button firstButton = buttons.get(0);
        Label title = titleLabel();
        
        // 테스트 케이스 1: 1200x800 창 크기
        interact(() -> {
            stage.setWidth(1200);
            stage.setHeight(800);
        });
        WaitForAsyncUtils.waitForFxEvents();
        WaitForAsyncUtils.sleep(100, TimeUnit.MILLISECONDS);
        WaitForAsyncUtils.waitForFxEvents();
        
        // 예상 값 계산 (Controller 로직 기반)
        // 버튼: clamp(w * 0.28, 180, 380) = clamp(336, 180, 380) = 336
        // 버튼 높이: clamp(h * 0.1, 60, 120) = clamp(80, 60, 120) = 80
        // 스코어보드 너비: clamp(w * 0.20, 200, 400) = clamp(240, 200, 400) = 240
        // 스코어보드 높이: clamp(h * 0.7, 400, 800) = clamp(560, 400, 800) = 560
        // 타이틀 폰트: clamp(min(1200,800) / 6.0, 30, 120) = clamp(133.3, 30, 120) = 120
        
        // 버튼 크기 확인
        assertEquals("1200x800에서 버튼 너비는 336이어야 합니다.", 336.0, firstButton.getPrefWidth(), 1.0);
        assertEquals("1200x800에서 버튼 높이는 80이어야 합니다.", 80.0, firstButton.getPrefHeight(), 1.0);
        // Title 폰트 크기 확인 (style에서 추출)
        String titleStyle = title.getStyle();
        assertTrue("타이틀에 폰트 크기가 설정되어야 합니다.", titleStyle.contains("-fx-font-size:"));
        assertTrue("1200x800에서 타이틀 폰트는 120px이어야 합니다.", titleStyle.contains("120px"));
        
        // 테스트 케이스 2: 600x400 창 크기
        interact(() -> {
            stage.setWidth(600);
            stage.setHeight(400);
        });
        WaitForAsyncUtils.waitForFxEvents();
        WaitForAsyncUtils.sleep(100, TimeUnit.MILLISECONDS);
        WaitForAsyncUtils.waitForFxEvents();
        
        // 예상 값 계산
        // 버튼: clamp(600 * 0.28, 180, 380) = clamp(168, 180, 380) = 180 (최소값)
        // 버튼 높이: clamp(400 * 0.1, 60, 120) = clamp(40, 60, 120) = 60 (최소값)
        // 스코어보드 너비: clamp(600 * 0.20, 200, 400) = clamp(120, 200, 400) = 200 (최소값)
        // 스코어보드 높이: clamp(400 * 0.7, 400, 800) = clamp(280, 400, 800) = 400 (최소값)
        // 타이틀 폰트: clamp(min(600,400) / 6.0, 30, 120) = clamp(66.7, 30, 120) = 67
        
        // 버튼 크기 확인
        assertEquals("600x400에서 버튼 너비는 180이어야 합니다.", 180.0, firstButton.getPrefWidth(), 1.0);
        assertEquals("600x400에서 버튼 높이는 60이어야 합니다.", 60.0, firstButton.getPrefHeight(), 1.0);
        // Title 폰트 크기 확인
        titleStyle = title.getStyle();
        assertTrue("600x400에서 타이틀 폰트는 67px이어야 합니다.", titleStyle.contains("67px"));
        
        // 테스트 케이스 3: 900x600 창 크기 (초기 크기)
        interact(() -> {
            stage.setWidth(900);
            stage.setHeight(600);
        });
        WaitForAsyncUtils.waitForFxEvents();
        WaitForAsyncUtils.sleep(100, TimeUnit.MILLISECONDS);
        WaitForAsyncUtils.waitForFxEvents();
        
        // 예상 값 계산
        // 버튼: clamp(900 * 0.28, 180, 380) = clamp(252, 180, 380) = 252
        // 버튼 높이: clamp(600 * 0.1, 60, 120) = clamp(60, 60, 120) = 60
        // 스코어보드 너비: clamp(900 * 0.20, 200, 400) = clamp(180, 200, 400) = 200 (최소값)
        // 스코어보드 높이: clamp(600 * 0.7, 400, 800) = clamp(420, 400, 800) = 420
        // 타이틀 폰트: clamp(min(900,600) / 6.0, 30, 120) = clamp(100, 30, 120) = 100
        
        // 버튼 크기 확인
        assertEquals("900x600에서 버튼 너비는 252이어야 합니다.", 252.0, firstButton.getPrefWidth(), 1.0);
        assertEquals("900x600에서 버튼 높이는 60이어야 합니다.", 60.0, firstButton.getPrefHeight(), 1.0);
        // Title 폰트 크기 확인
        titleStyle = title.getStyle();
        assertTrue("900x600에서 타이틀 폰트는 100px이어야 합니다.", titleStyle.contains("100px"));
    }
}