package org.tetris.menu.start.controller;

import org.tetris.UIRouter;
import org.tetris.menu.start.model.StartMenuModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.MapProperty;
import javafx.collections.MapChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class StartMenuController {

    @FXML
    private StackPane root;
    @FXML
    private Label titleLabel;
    @FXML
    private VBox menuBox;
    @FXML
    private Label wrongInputLabel;

    // 스코어보드 영역
    @FXML
    private VBox scoreboard; // 스코어보드 전체 컨테이너
    @FXML
    private VBox scoreboardBox; // 스코어보드 내 실제 점수 행 컨테이너

    private UIRouter router;
    private StartMenuModel model;
    private final ArrayList<Button> buttons = new ArrayList<>();
    private Timeline hideMessageTimeline;

    // Router가 주입을 끝낸 뒤 호출
    public void init(UIRouter router, StartMenuModel model) {
        this.router = router;
        this.model = model;

        createMenuButtons();
        setupWrongInputLabelText();

        // 첫 번째 버튼 하이라이트
        if (!buttons.isEmpty()) {
            buttons.get(model.getSelectedIndex()).getStyleClass().add("highlighted");
        }

        // 반응형 크기 적용
        applyResponsiveSizing();
    }

    private void createMenuButtons() {
        Button gameStartButton = createMenuButton("게임 시작");
        Button settingButton = createMenuButton("설정");
        Button exitButton = createMenuButton("종료");

        gameStartButton.setOnAction(e -> onGameStart());
        settingButton.setOnAction(e -> onSettings());
        exitButton.setOnAction(e -> onExit());

        buttons.add(gameStartButton);
        buttons.add(settingButton);
        buttons.add(exitButton);

        menuBox.getChildren().addAll(buttons);

        // 마우스 오버 시 해당 버튼으로 하이라이트 이동
        for (int i = 0; i < buttons.size(); i++) {
            final int idx = i;
            buttons.get(i).setOnMouseEntered(ev -> setHighlightedIndex(idx));
        }
    }

    private Button createMenuButton(String text) {
        Button button = new Button(text);
        button.setFocusTraversable(false);
        button.getStyleClass().add("menu-button");
        return button;
    }

    private void setupWrongInputLabelText() {
        wrongInputLabel.setText("잘못된 입력입니다.\n위/아래 방향키와 Enter를 사용하세요.");
    }

    // 키 입력 바인딩 (Router가 Scene 만든 뒤 호출)
    public void bindInput() {
        if (root.getScene() == null) {
            System.err.println("Warning: Scene is null when trying to bind input");
            return;
        }
        root.setFocusTraversable(true);
        root.requestFocus();

        root.getScene().setOnKeyPressed(e -> {
            handleKey(e);
            e.consume();
        });
    }

    private void handleKey(KeyEvent e) {
        if (e.getCode() == KeyCode.UP)
            setHighlightedButton(-1);
        else if (e.getCode() == KeyCode.DOWN)
            setHighlightedButton(+1);
        else if (e.getCode() == KeyCode.ENTER)
            fire();
        else
            showWrongInputLabel();
    }

    private void showWrongInputLabel() {
        wrongInputLabel.setVisible(true);

        if (hideMessageTimeline != null)
            hideMessageTimeline.stop();

        hideMessageTimeline = new Timeline(
                new KeyFrame(Duration.seconds(2), ev -> wrongInputLabel.setVisible(false)));
        hideMessageTimeline.play();
    }

    // 델타 이동
    private void setHighlightedButton(int move) {
        int n = buttons.size();
        if (n == 0)
            return;

        int current = model.getSelectedIndex();
        int next = Math.floorMod(current + move, n);
        updateHighlight(next);
    }

    // 목표 인덱스로 즉시 이동: 마우스 호버에서 사용
    private void setHighlightedIndex(int target) {
        int n = buttons.size();
        if (n == 0)
            return;
        int next = Math.floorMod(target, n);
        updateHighlight(next);
    }

    // 하이라이트/모델 동기화의 단일 진입점
    private void updateHighlight(int newIndex) {
        int n = buttons.size();
        if (n == 0)
            return;

        int prev = model.getSelectedIndex();
        if (prev == newIndex)
            return;

        // 이전 하이라이트 제거(안전하게 중복 제거)
        buttons.get(prev).getStyleClass().remove("highlighted");

        // 모델 인덱스 즉시 갱신
        model.setSelectedIndex(newIndex);

        // 새 하이라이트 추가
        buttons.get(newIndex).getStyleClass().add("highlighted");
    }

    private void fire() {
        buttons.get(model.getSelectedIndex()).fire();
    }

    // FXML onAction
    @FXML
    public void onGameStart() {
        router.showGamePlaceholder();
    }

    @FXML
    public void onSettings() {
        router.showSettings();
    }

    @FXML
    public void onExit() {
        router.exitGame();
    }

    /*
     * =========================
     * 반응형(화면 크기 대응)
     * =========================
     */
    private void applyResponsiveSizing() {
        Platform.runLater(this::updateSizes);
        // 너비가 변경될 때 크기 업데이트되도록 리스너 추가
        root.widthProperty().addListener((obs, oldVal, newVal) -> updateSizes());
        root.heightProperty().addListener((obs, oldVal, newVal) -> updateSizes());
    }

    // 화면 크기에 따라 폰트 크기, 버튼 크기 등을 조정
    private void updateSizes() {
        double w = root.getWidth();
        double h = root.getHeight();
        if (w <= 0 || h <= 0)
            return;

        double base = Math.min(w, h);

        // 타이틀 폰트: 비례 + 클램프
        double titlePx = clamp(base / 6.0, 30, 120);
        titleLabel.setStyle("-fx-font-size: " + Math.round(titlePx) + "px;");

        // 버튼 크기: 비례 + 클램프
        // 1) 버튼 간 간격: 화면 높이에 비례 + 클램프
        double gap = clamp(h * 0.3, 40, 60); // 예: 높이의 4%, 최소 12px, 최대 36px
        menuBox.setSpacing(gap);

        double btnW = clamp(w * 0.3, 180, 420);
        double btnH = clamp(h * 0.12, 40, 80);
        double btnFontPx = clamp(btnH * 0.40, 15, 40);

        for (Button b : buttons) {
            b.setPrefWidth(btnW);
            b.setPrefHeight(btnH);
            // 인라인 스타일로 폰트 크기만 덧입힘(기타 CSS는 그대로 유지)
            b.setStyle("-fx-font-size: " + Math.round(btnFontPx) + "px;");
        }
    }

    private static double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }

    /*
     * =========================
     * 스코어보드 렌더링
     * =========================
     */

    // 정렬된 Top10을 문자열로 받는 버전.
    // 형식: "1. playerId : score"

    public void setTopScores(List<String> rankedLines) {
        if (rankedLines == null) return;

        Runnable apply = () -> scoreboardBox.getChildren().setAll(
                rankedLines.stream().limit(10).map(this::createScoreRowFromLine).toList());

        if (Platform.isFxApplicationThread())
            apply.run();
        else
            Platform.runLater(apply);
    }

    private HBox createScoreRowFromLine(String line) {
        // line 예: "1. abc : 12345"
        Label rowLabel = new Label(line);

        HBox row = new HBox(8, rowLabel);
        row.getStyleClass().add("score-row");
        row.setFillHeight(true);
        // 가로 꽉 차기용 spacer (필요 시)
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        return row;
    }

    // key: playerId, value: score
    // ScoreboardModel에서 바뀔 때마다 자동 갱신됨.

    public void bindScoreboard(MapProperty<String, Integer> sortedScoreMap) {
        if (sortedScoreMap == null)
            return;

        renderScoreboardMap(sortedScoreMap);
        // Scoreboard 변경 감지 리스너 등록
        sortedScoreMap.addListener((MapChangeListener<String, Integer>) ch -> Platform
                .runLater(() -> renderScoreboardMap(sortedScoreMap)));
    }

    // scoreMap을 받아서 스코어보드 갱신
    // MapProperty 변경 감지 리스너에서 호출됨
    // Map은 정렬된 상태로 들어온다고 가정
    // map을 string 리스트로 변환하여 setTopScores 호출
    private void renderScoreboardMap(Map<String, Integer> map) {
        AtomicInteger rank = new AtomicInteger(1);
        List<String> lines = map.entrySet().stream()
                .limit(10)
                .map(e -> rank.getAndIncrement() + ". " + e.getKey() + " : " + e.getValue())
                .toList();

        setTopScores(lines);
    }
}
