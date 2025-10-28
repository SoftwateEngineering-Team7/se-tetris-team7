package org.tetris.menu.start.controller;

import org.tetris.Router;

import org.tetris.menu.start.model.StartMenuModel;
import org.tetris.shared.BaseController;
import org.tetris.shared.RouterAware;

import java.util.ArrayList;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class StartMenuController extends BaseController<StartMenuModel> implements RouterAware {
    // CSS 스타일 클래스 상수
    private static final String STYLE_HIGHLIGHTED = "highlighted";
    private static final String STYLE_MENU_BUTTON = "menu-button";
    
    // 메뉴 버튼 텍스트 상수
    private static final String TEXT_NORMAL_MODE = "일반 모드\n게임 시작";
    private static final String TEXT_ITEM_MODE = "아이템 모드\n게임 시작";
    private static final String TEXT_SETTINGS = "설정";
    private static final String TEXT_EXIT = "종료";
    
    // 메시지 텍스트 상수
    private static final String TEXT_WRONG_INPUT = "잘못된 입력입니다.\n위/아래 방향키와 Enter를 사용하세요.";

    private static final String STYLE_TITLE = "-fx-font-size: ";
    private static final String PX_STRING = "px;";
    
    public StartMenuController(StartMenuModel model) {
        super(model);
    }

    @FXML
    private StackPane root;
    @FXML
    private Label titleLabel;
    @FXML
    private VBox menuBox;
    @FXML
    private Label wrongInputLabel;

    // 스코어보드 영역 - Item (왼쪽)
    @FXML
    private VBox scoreboardItem;
    @FXML
    private Label scoreboardItemTitle;
    @FXML
    private VBox scoreboardItemBox;

    // 스코어보드 영역 - Normal (오른쪽)
    @FXML
    private VBox scoreboardNormal;
    @FXML
    private Label scoreboardNormalTitle;
    @FXML
    private VBox scoreboardNormalBox;


    private Router router;
    private final ArrayList<Button> buttons = new ArrayList<>();
    private Timeline hideMessageTimeline;

    @FXML
    protected void initialize() {
        super.initialize();

        createMenuButtons();
        setupWrongInputLabelText();

        // 첫 번째 버튼 하이라이트
        if (!buttons.isEmpty()) {
            buttons.get(model.getSelectedIndex()).getStyleClass().add(STYLE_HIGHLIGHTED);
        }

        bindInput();

        // 반응형 크기 적용
        applyResponsiveSizing();
    }

    @Override
    public void setRouter(Router router) {
        this.router = router;

        // 버튼 액션 핸들러 등록 (라우터 등록 후 등록 가능)
        buttons.get(0).setOnAction(e -> onGameStart());
        buttons.get(1).setOnAction(e -> onItemGameStart());
        buttons.get(2).setOnAction(e -> onSettings());
        buttons.get(3).setOnAction(e -> onExit());
    }

    private void createMenuButtons() {
        Button normalStartButton = createMenuButton(TEXT_NORMAL_MODE);
        Button itemStarButton = createMenuButton(TEXT_ITEM_MODE);
        Button settingButton = createMenuButton(TEXT_SETTINGS);
        Button exitButton = createMenuButton(TEXT_EXIT);

        buttons.add(normalStartButton);
        buttons.add(itemStarButton);
        buttons.add(settingButton);
        buttons.add(exitButton);

        menuBox.getChildren().addAll(buttons);
    }

    private Button createMenuButton(String text) {
        Button button = new Button(text);
        button.setFocusTraversable(false);
        button.getStyleClass().add(STYLE_MENU_BUTTON);
        return button;
    }

    private void setupWrongInputLabelText() {
        wrongInputLabel.setText(TEXT_WRONG_INPUT);
    }

    public void bindInput() {
        // Scene이 설정될 때까지 대기
        if (root.getScene() == null) {
            root.sceneProperty().addListener((obs, oldScene, newScene) -> {
                if (newScene != null) {
                    setupKeyboardInput(newScene);
                }
            });
        } else {
            setupKeyboardInput(root.getScene());
        }

        // 마우스 오버 시 해당 버튼으로 하이라이트 이동
        for (int i = 0; i < buttons.size(); i++) {
            final int idx = i;
            buttons.get(i).setOnMouseEntered(ev -> setHighlightedIndex(idx));
        }
    }

    private void setupKeyboardInput(Scene scene) {
        root.setFocusTraversable(true);
        root.requestFocus();

        scene.setOnKeyPressed(e -> {
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
        int prev = model.getSelectedIndex();
        if (prev == newIndex)
            return;

        // 이전 하이라이트 제거(안전하게 중복 제거)
        buttons.get(prev).getStyleClass().remove(STYLE_HIGHLIGHTED);

        // 모델 인덱스 즉시 갱신
        model.setSelectedIndex(newIndex);

        // 새 하이라이트 추가
        buttons.get(newIndex).getStyleClass().add(STYLE_HIGHLIGHTED);
    }

    private void fire() {
        buttons.get(model.getSelectedIndex()).fire();
    }

    // FXML onAction
    @FXML
    public void onGameStart() {
        router.showGamePlaceholder(false);
    }

    @FXML
    public void onItemGameStart() {
        router.showGamePlaceholder(true);
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

        updateTitleSizes(w, h);

        updateButtonSizes(w, h);

        updateScoreboardSizes(w, h);
    }

    private void updateTitleSizes(double w, double h) {
        double base = Math.min(w, h);

        // 타이틀 폰트: 비례 + 클램프
        double titlePx = clamp(base / 6.0, 30, 120);
        titleLabel.setStyle(STYLE_TITLE + Math.round(titlePx) + PX_STRING);
    }

    private void updateButtonSizes(double w, double h) {
        // 버튼 크기: 비례 + 클램프
        double gap = clamp(h * 0.03, 12, 50);
        menuBox.setSpacing(gap);

        double btnW = clamp(w * 0.28, 180, 380);
        double btnH = clamp(h * 0.1, 60, 120);
        double btnFontPx = clamp(btnH * 0.18, 14, 28);

        for (Button b : buttons) {
            b.setPrefWidth(btnW);
            b.setPrefHeight(btnH);
            // 인라인 스타일로 폰트 크기만 덧입힘(기타 CSS는 그대로 유지)
            b.setStyle(STYLE_TITLE + Math.round(btnFontPx) + PX_STRING);
        }
    }

    private void updateScoreboardSizes(double screenW, double screenH) {
        // 스코어보드 너비: 화면 너비에 비례, 클램프
        double sbWidth = clamp(screenW * 0.20, 200, 400);
        
        // 스코어보드 높이: 화면 높이에 비례, 클램프
        double sbHeight = clamp(screenH * 0.7, 400, 800);
        
        // 스코어보드 타이틀 폰트 크기
        double sbTitleFontPx = clamp(screenW * 0.012, 11, 16);

        // Item 스코어보드 적용
        if (scoreboardItem != null) {
            scoreboardItem.setPrefWidth(sbWidth);
            scoreboardItem.setPrefHeight(sbHeight);
            scoreboardItem.setMaxWidth(sbWidth);
            scoreboardItem.setMaxHeight(sbHeight);
        }
        if (scoreboardItemTitle != null) {
            scoreboardItemTitle.setStyle(STYLE_TITLE + Math.round(sbTitleFontPx) + PX_STRING);
        }

        // Normal 스코어보드 적용
        if (scoreboardNormal != null) {
            scoreboardNormal.setPrefWidth(sbWidth);
            scoreboardNormal.setPrefHeight(sbHeight);
            scoreboardNormal.setMaxWidth(sbWidth);
            scoreboardNormal.setMaxHeight(sbHeight);
        }
        if (scoreboardNormalTitle != null) {
            scoreboardNormalTitle.setStyle(STYLE_TITLE + Math.round(sbTitleFontPx) + PX_STRING);
        }

        // 스코어 행 폰트는 동적 생성 시 적용되므로 여기서는 변수만 저장
        // (필요시 ScoreRow 생성 시 참조할 수 있도록 필드에 저장)
    }

    private static double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }
}
