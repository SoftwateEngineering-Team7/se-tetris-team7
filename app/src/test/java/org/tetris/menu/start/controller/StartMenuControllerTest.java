package org.tetris.menu.start.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.tetris.UIRouter;
import org.tetris.menu.start.model.StartMenuModel;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("StartMenuController 테스트")
@ExtendWith(MockitoExtension.class)

class StartMenuControllerTest {

    @BeforeAll
    static void initJavaFX() {
        try {
            Platform.startup(() -> {
            });
        } catch (IllegalStateException ignore) {
            // 이미 초기화된 경우
        }
        Platform.setImplicitExit(false);
    }

    @Mock
    private UIRouter router;
    @Mock
    private StartMenuModel model;

    @InjectMocks
    private StartMenuController controller;

    private StackPane rootMock;
    private Label titleLabelReal;
    private VBox menuBoxReal;
    private VBox scoreboardBoxReal;

    private ArrayList<Button> realButtons;

    @BeforeEach
    void setUp() throws Exception {
        // root는 크기 제어를 위해 mock
        rootMock = mock(StackPane.class);
        lenient().when(rootMock.getWidth()).thenReturn(800.0);
        lenient().when(rootMock.getHeight()).thenReturn(600.0);

        titleLabelReal = new Label("TETRIS");
        menuBoxReal = new VBox();
        scoreboardBoxReal = new VBox();

        realButtons = new ArrayList<>(List.of(
                new Button("게임 시작"),
                new Button("설정"),
                new Button("종료")));

        // model selectedIndex 동작을 로컬 상태로 에뮬레이션
        AtomicInteger selected = new AtomicInteger(0);
        lenient().when(model.getSelectedIndex()).thenAnswer(inv -> selected.get());
        lenient().doAnswer(inv -> {
            selected.set(inv.getArgument(0, Integer.class));
            return null;
        })
                .when(model).setSelectedIndex(anyInt());

        // @FXML 필드 주입(리플렉션)
        setPrivateField(controller, "root", rootMock);
        setPrivateField(controller, "titleLabel", titleLabelReal);
        setPrivateField(controller, "menuBox", menuBoxReal);
        setPrivateField(controller, "scoreboardBox", scoreboardBoxReal);
        setPrivateField(controller, "buttons", realButtons);

        // 초기 하이라이트 표시(0번)
        realButtons.get(0).getStyleClass().add("highlighted");
    }

    private static void setPrivateField(Object target, String name, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(name);
        f.setAccessible(true);
        f.set(target, value);
    }

    private static Method getPrivateMethod(Class<?> c, String name, Class<?>... types) throws Exception {
        Method m = c.getDeclaredMethod(name, types);
        m.setAccessible(true);
        return m;
    }

    private static void waitFx() {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(latch::countDown);
        try {
            latch.await(2, TimeUnit.SECONDS);
        } catch (InterruptedException ignored) {
        }
    }

    private static KeyEvent key(KeyCode code) {
        return new KeyEvent(KeyEvent.KEY_PRESSED, "", "", code, false, false, false, false);
    }

    /* ───────── 라우팅 onAction ───────── */
    @Test
    @DisplayName("onGameStart → router.showGamePlaceholder 호출")
    void onGameStartCallsRouter() {
        controller.onGameStart();
        verify(router).showGamePlaceholder();
    }

    @Test
    @DisplayName("onSettings → router.showSettings 호출")
    void onSettingsCallsRouter() {
        controller.onSettings();
        verify(router).showSettings();
    }

    @Test
    @DisplayName("onExit → router.exitGame 호출")
    void onExitCallsRouter() {
        controller.onExit();
        verify(router).exitGame();
    }

    /* ───────── 키 입력 처리 ───────── */
    @Test
    @DisplayName("UP → setHighlightedButton(-1) 경유해 마지막 인덱스로 이동")
    void handleKeyUpMovesToLast() throws Exception {
        Method handleKey = getPrivateMethod(StartMenuController.class, "handleKey", KeyEvent.class);
        handleKey.invoke(controller, key(KeyCode.UP));
        // 0 → UP → 2, 하이라이트 인덱스가 2로 갔는지 확인
        verify(model, atLeastOnce()).setSelectedIndex(2);
        assertFalse(realButtons.get(0).getStyleClass().contains("highlighted"));
        assertTrue(realButtons.get(2).getStyleClass().contains("highlighted"));
    }

    @Test
    @DisplayName("DOWN → setHighlightedButton(+1) 경유해 1로 이동")
    void handleKeyDownMovesToOne() throws Exception {
        Method handleKey = getPrivateMethod(StartMenuController.class, "handleKey", KeyEvent.class);
        handleKey.invoke(controller, key(KeyCode.DOWN));
        verify(model, atLeastOnce()).setSelectedIndex(1);
        assertFalse(realButtons.get(0).getStyleClass().contains("highlighted"));
        assertTrue(realButtons.get(1).getStyleClass().contains("highlighted"));
    }

    @Test
    @DisplayName("ENTER → 현재 선택 버튼 fire() 호출")
    void handleKeyEnterFiresSelected() throws Exception {
        // 0번 버튼만 spy로 바꿔 fire() 호출 검증
        Button spy0 = spy(realButtons.get(0));
        realButtons.set(0, spy0);
        setPrivateField(controller, "buttons", realButtons);

        Method handleKey = getPrivateMethod(StartMenuController.class, "handleKey", KeyEvent.class);
        handleKey.invoke(controller, key(KeyCode.ENTER));
        verify(spy0, times(1)).fire();
    }

    @Test
    @DisplayName("updateSizes → 타이틀/버튼/간격: 창 크기(800x600) 기준으로 정확히 계산/적용")
    void updateSizesApplies() throws Exception {
        // 1) 이 테스트에서만 창 크기 스텁 (불필요 스텁 이슈 방지)
        lenient().when(rootMock.getWidth()).thenReturn(800.0);
        lenient().when(rootMock.getHeight()).thenReturn(600.0);

        // 2) 호출
        Method updateSizes = getPrivateMethod(StartMenuController.class, "updateSizes");
        updateSizes.invoke(controller);

        // 3) 기대값 계산 (현재 코드 기준)
        // base = min(800,600)=600
        // titlePx = clamp(600/6=100, 30,120) = 100
        assertTrue(titleLabelReal.getStyle().contains("-fx-font-size: 100px"),
                "타이틀 폰트는 100px이어야 합니다.");

        // gap = clamp(h * 0.3, 40, 60) = clamp(180, 40, 60) = 60
        // (참고: 주석엔 4%라고 쓰여있는데 코드가 0.3(30%)입니다. 의도 4%라면 0.04로 고치세요.)
        assertEquals(60.0, menuBoxReal.getSpacing(), 0.01, "버튼 간격");

        // 버튼: w*0.3=240 → clamp(180,420)=240 / h*0.12=72 → clamp(40,80)=72
        // 폰트: 72*0.40=28.8 → round=29
        for (Button b : realButtons) {
            assertEquals(240.0, b.getPrefWidth(), 0.01, "버튼 폭");
            assertEquals(72.0, b.getPrefHeight(), 0.01, "버튼 높이");
            assertTrue(b.getStyle().contains("-fx-font-size: 29px"), "버튼 폰트 29px");
        }
    }

    /* ───────── 마우스 호버/직접 인덱스 지정 ───────── */
    @Test
    @DisplayName("setHighlightedIndex(2) → 하이라이트/모델 동기화")
    void setHighlightedIndexSyncs() throws Exception {
        Method setHighlightedIndex = getPrivateMethod(StartMenuController.class, "setHighlightedIndex", int.class);
        setHighlightedIndex.invoke(controller, 2);
        verify(model, atLeastOnce()).setSelectedIndex(2);
        assertTrue(realButtons.get(2).getStyleClass().contains("highlighted"));
        assertFalse(realButtons.get(0).getStyleClass().contains("highlighted"));
    }

    /* ───────── 스코어보드 렌더링 ───────── */
    @Test
    @DisplayName("setTopScores → Top10만 렌더")
    void setTopScoresRendersTop10() {
        List<String> lines = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            lines.add(i + ". player" + i + " : " + (10000 - i));
        }
        controller.setTopScores(lines);
        waitFx();

        assertEquals(10, scoreboardBoxReal.getChildren().size());
        var row0 = (javafx.scene.layout.HBox) scoreboardBoxReal.getChildren().get(0);
        var label0 = (Label) row0.getChildren().get(0);
        var row9 = (javafx.scene.layout.HBox) scoreboardBoxReal.getChildren().get(9);
        var label9 = (Label) row9.getChildren().get(0);
        assertTrue(label0.getText().startsWith("1."));
        assertTrue(label9.getText().startsWith("10."));
    }

    @Test
    @DisplayName("bindScoreboard → Map 변경 시 자동 렌더")
    void bindScoreboardReactsToMapChanges() {
        var backing = new LinkedHashMap<String, Integer>(); // 순서 유지
        var observable = FXCollections.observableMap(backing);
        var scoresProp = new javafx.beans.property.SimpleMapProperty<>(observable);

        controller.bindScoreboard(scoresProp);
        waitFx();

        scoresProp.put("a", 12000);
        scoresProp.put("b", 11000);
        scoresProp.put("c", 10000);
        waitFx();
        assertEquals(3, scoreboardBoxReal.getChildren().size());

        // 12개 더 추가 → 항상 Top10
        for (int i = 0; i < 12; i++)
            scoresProp.put("p" + i, 9000 - i);
        waitFx();
        assertEquals(10, scoreboardBoxReal.getChildren().size());
    }
}
