package org.tetris.menu.setting.controller;

import org.tetris.Router;
import org.tetris.menu.setting.model.SettingMenuModel;
import org.tetris.shared.BaseController;
import org.tetris.shared.RouterAware;
import org.util.Difficulty;
import org.util.KeyLayout;
import org.util.ScreenPreset;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

public class SettingMenuController extends BaseController<SettingMenuModel> implements RouterAware {
    // CSS 스타일 클래스 상수
    private static final String STYLE_HIGHLIGHTED = "highlighted";
    private static final String STYLE_HIGHLIGHTED_CHILD = "highlighted-child";

    public SettingMenuController(SettingMenuModel model) {
        super(model);
    }

    @FXML
    private BorderPane root;
    @FXML
    private VBox mainVBox;

    // 동적 생성될 컴포넌트들
    private ToggleGroup colorBlindGroup;
    private ToggleGroup keyLayoutGroup;
    private ToggleGroup screenGroup;
    private ToggleGroup difficultyGroup;
    private Button btnResetNormalScoreboard;
    private Button btnResetItemScoreboard;

    // 버튼 (FXML에서 주입)
    @FXML
    private HBox buttonBox;
    @FXML
    private Button btnSave;
    @FXML
    private Button btnReset;
    @FXML
    private Button btnBackToStart;


    private Router router;

    // 포커스 관리 - 단일 인덱스 방식
    private final List<Node> focusableItems = new ArrayList<>();  // 모든 포커스 가능한 항목 (TitledPane, RadioButton, Button)
    private int highlightedItemIndex = -1;  // 현재 하이라이트(포커스) 인덱스

    @FXML
    protected void initialize() {
        super.initialize();

        // 동적 UI 생성
        setupDynamicPanes();
        
        // Pane 리스너 설정
        setupPaneListeners();
        
        // radio button 리스너 설정
        setupRadioButtonListeners();
        
        // 모델 → UI 동기화
        reflectModelToView();

        // 포커스 가능한 항목 리스트 구성
        buildFocusableList();

        // 렌더링 후 수행
        Platform.runLater(() -> {
            bindInput();
            setupMouseEvents();
            setHighlighted(0);
            root.requestFocus();
        });

        // 반응형 크기 조정
        applyResponsiveSizing();
    }

    @Override
    public void setRouter(Router router) {
        this.router = router;
        
        // 버튼 액션 핸들러 등록
        btnSave.setOnAction(e -> onSave());
        btnReset.setOnAction(e -> onReset());
        btnBackToStart.setOnAction(e -> onBackToStart());
    }

    private void setupDynamicPanes() {
        colorBlindGroup = new ToggleGroup();
        keyLayoutGroup = new ToggleGroup();
        screenGroup = new ToggleGroup();
        difficultyGroup = new ToggleGroup();
        
        createColorBlindPane();
        createKeyLayoutPane();
        createScreenSizePane();
        createDifficultyPane();
        createScoreboardPane();
    }

    // 색약 모드 설정 Pane 생성
    private void createColorBlindPane() {
        TitledPane p = new TitledPane("색약 모드", new VBox(12));
        p.setExpanded(true);
        VBox c = (VBox) p.getContent();
        c.getStyleClass().add("group");
        
        HBox r = new HBox(20);

        RadioButton rb1 = new RadioButton("일반 시각");
        rb1.setToggleGroup(colorBlindGroup);
        rb1.setUserData(false);

        RadioButton rb2 = new RadioButton("색약 모드");
        rb2.setToggleGroup(colorBlindGroup);
        rb2.setUserData(true);
        r.getChildren().addAll(rb1, rb2);
        c.getChildren().add(r);
        mainVBox.getChildren().add(p);
    }

    private void createKeyLayoutPane() {
        TitledPane p = new TitledPane("Key Layout", new VBox(12));
        p.setExpanded(true);
        VBox c = (VBox) p.getContent();
        c.getStyleClass().add("group");
        
        HBox r = new HBox(20);
        // KeyLayout 옵션들
        String[] layouts = {KeyLayout.KEY_ARROWS, KeyLayout.KEY_WASD};
        for (String layout : layouts) {
            RadioButton rb = new RadioButton(layout);
            rb.setToggleGroup(keyLayoutGroup);
            rb.setUserData(layout);
            r.getChildren().add(rb);
        }

        c.getChildren().add(r);
        mainVBox.getChildren().add(p);
    }

    private void createScreenSizePane() {
        TitledPane p = new TitledPane("Screen Size", new VBox(12));
        p.setExpanded(true);
        VBox c = (VBox) p.getContent();
        c.getStyleClass().add("group");
        
        HBox r = new HBox(20);
        // ScreenPreset 옵션들
        String[][] presets = {
            {ScreenPreset.SMALL_STRING, ScreenPreset.SMALL_SIZE},
            {ScreenPreset.MIDDLE_STRING, ScreenPreset.MIDDLE_SIZE},
            {ScreenPreset.LARGE_STRING, ScreenPreset.LARGE_SIZE}
        };
        for (String[] preset : presets) {
            RadioButton rb = new RadioButton(preset[1]);
            rb.setToggleGroup(screenGroup);
            rb.setUserData(preset[0]);
            r.getChildren().add(rb);
        }
        c.getChildren().add(r);
        mainVBox.getChildren().add(p);
    }

    private void createDifficultyPane() {
        TitledPane p = new TitledPane("난이도", new VBox(12));
        p.setExpanded(true);
        VBox c = (VBox) p.getContent();
        c.getStyleClass().add("group");
        
        HBox r = new HBox(15);
        // Difficulty 옵션들
        String[] difficulties = {
            Difficulty.EASY_STRING,
            Difficulty.NORMAL_STRING,
            Difficulty.HARD_STRING
        };

        for (String d : difficulties) {
            RadioButton rb = new RadioButton(d);
            rb.setToggleGroup(difficultyGroup);
            rb.setUserData(d);
            r.getChildren().add(rb);
        }
        c.getChildren().add(r);
        mainVBox.getChildren().add(p);
    }

    private void createScoreboardPane() {
        TitledPane p = new TitledPane("Scoreboard", new VBox(15));
        p.setExpanded(true);
        VBox c = (VBox) p.getContent();
        c.getStyleClass().add("group");
        
        // 버튼들을 HBox로 좌우 배치
        HBox buttonContainer = new HBox(20);
        buttonContainer.setAlignment(Pos.CENTER);
        
        btnResetNormalScoreboard = new Button("일반 모드 스코어보드 초기화");
        btnResetNormalScoreboard.setOnAction(e -> onResetNormalScoreboard());
        
        btnResetItemScoreboard = new Button("아이템 모드 스코어보드 초기화");
        btnResetItemScoreboard.setOnAction(e -> onResetItemScoreboard());
        
        buttonContainer.getChildren().addAll(btnResetNormalScoreboard, btnResetItemScoreboard);
        c.getChildren().add(buttonContainer);
        mainVBox.getChildren().add(p);
    }

    private void setupPaneListeners() {
        // TitledPane이 펼쳐지거나 접힐 때 포커스 리스트 재구성
        mainVBox.getChildren().stream()
                .filter(node -> node instanceof TitledPane)
                .map(node -> (TitledPane) node)
                .forEach(pane -> {
                    pane.expandedProperty().addListener((obs, old, isExpanded) -> {
                        int oldIndex = highlightedItemIndex;
                        buildFocusableList();
                        setupMouseEvents();
                        // 이전 포커스 유지 시도
                        if (oldIndex >= 0 && oldIndex < focusableItems.size()) {
                            setHighlighted(oldIndex);
                        } else {
                            setHighlighted(0);
                        }
                    });
                });
    }

    
    // 포커스 가능한 항목 리스트 구성
    private void buildFocusableList() {
        focusableItems.clear();
        
        // 각 TitledPane 처리
        for (Node node : mainVBox.getChildren()) {
            if (node instanceof TitledPane) {
                TitledPane pane = (TitledPane) node;
                focusableItems.add(pane);  // TitledPane 자체 추가
                
                // 펼쳐진 경우 내부의 RadioButton/Button들을 개별적으로 추가
                if (pane.isExpanded()) {
                    Node content = pane.getContent();
                    if (content instanceof Parent) {
                        List<Node> children = getNavigableChildren(content);
                        focusableItems.addAll(children);  // RadioButton, Button들 개별 추가
                    }
                }
            }
        }
        
        // 하단 버튼들 추가
        focusableItems.add(btnBackToStart);
        focusableItems.add(btnReset);
        focusableItems.add(btnSave);
    }

    // 마우스 이벤트 설정
    private void setupMouseEvents() {
        for (int i = 0; i < focusableItems.size(); i++) {
            final int index = i;
            Node item = focusableItems.get(i);

            if (item instanceof TitledPane) {
                setMouseEventOnTitlePane(index, item);
            } else if (item instanceof RadioButton) {
                setMouseEventOnRadioButton(index, item);
            } else if (item instanceof Button) {
                setMouseEventOnButton(index, item);
            }
        }
    }

    private void setMouseEventOnButton(final int index, Node item) {
        // Button: hover + click
        item.setOnMouseEntered(e -> setHighlighted(index));
        item.setOnMouseClicked(e -> {
            setHighlighted(index);
            ((Button) item).fire();
        });
    }

    private void setMouseEventOnRadioButton(final int index, Node item) {
        // RadioButton: hover + click
        item.setOnMouseEntered(e -> setHighlighted(index));
        item.setOnMouseClicked(e -> {
            setHighlighted(index);
            ((RadioButton) item).setSelected(true);
        });
    }

    private void setMouseEventOnTitlePane(final int index, Node item) {
        // TitledPane의 제목 영역에 hover 이벤트
        Node titleRegion = ((TitledPane) item).lookup(".title");
        if (titleRegion != null) {
            titleRegion.setOnMouseEntered(e -> setHighlighted(index));
            titleRegion.setOnMouseClicked(e -> {
                setHighlighted(index);
                TitledPane pane = (TitledPane) item;
                pane.setExpanded(!pane.isExpanded());
            });
        }
    }

    // 키보드 입력 바인딩
    private void bindInput() {
        if (root.getScene() == null) return;
        
        root.setFocusTraversable(true);
        root.getScene().setOnKeyPressed(e -> {
            handleKeyPress(e.getCode());
            e.consume();
        });
        root.getScene().setOnMouseClicked(e -> root.requestFocus());
    }

    // 키 입력 처리
    private void handleKeyPress(KeyCode key) {
        if (key == KeyLayout.getUpKey() || key == KeyLayout.getLeftKey()) {
            // UP과 LEFT는 이전 항목
            moveTo(-1);
        } else if (key == KeyLayout.getDownKey() || key == KeyLayout.getRightKey()) {
            // DOWN과 RIGHT는 다음 항목
            moveTo(+1);
        } else if (key == KeyCode.ENTER) {
            activateCurrentItem();
        }
    }

    // 키보드로 다음/이전 항목으로 이동
    private void moveTo(int delta) {
        if (focusableItems.isEmpty()) return;
        int size = focusableItems.size();
        int newIndex = ((highlightedItemIndex + delta) % size + size) % size;
        setHighlighted(newIndex);
    }

    // 지정된 인덱스로 포커스를 이동
    private void setHighlighted(int newIndex) {
        if (newIndex < 0 || newIndex >= focusableItems.size()) return;
        if (newIndex == highlightedItemIndex) return;

        // 이전 포커스 제거
        clearHighlight();

        // 새 포커스 설정
        highlightedItemIndex = newIndex;
        Node newItem = focusableItems.get(highlightedItemIndex);
        
        // 모든 항목에 highlighted 스타일 적용
        if (newItem instanceof RadioButton) {
            newItem.getStyleClass().add(STYLE_HIGHLIGHTED_CHILD);
        } else {
            newItem.getStyleClass().add(STYLE_HIGHLIGHTED);
        }
    }

    // 포커스 항목 활성화 -> Enter 키
    private void activateCurrentItem() {
        if (highlightedItemIndex < 0 || highlightedItemIndex >= focusableItems.size()) return;
        
        Node currentItem = focusableItems.get(highlightedItemIndex);
        
        if (currentItem instanceof RadioButton) {
            ((RadioButton) currentItem).setSelected(true);
        } else if (currentItem instanceof Button) {
            ((Button) currentItem).fire();
        } else if (currentItem instanceof TitledPane) {
            TitledPane pane = (TitledPane) currentItem;
            pane.setExpanded(!pane.isExpanded());
        }
    }

    // 모든 포커스 하이라이트를 제거
    private void clearHighlight() {
        if (highlightedItemIndex >= 0 && highlightedItemIndex < focusableItems.size()) {
            Node oldItem = focusableItems.get(highlightedItemIndex);
            oldItem.getStyleClass().removeAll(STYLE_HIGHLIGHTED, STYLE_HIGHLIGHTED_CHILD);
        }
    }

    // 컨테이너 노드에서 탐색 가능한 자식 노드(RadioButton, Button)들을 반환
    // == TitledPane의 Content에서 RadioButton과 Button들을 찾아 반환
    private List<Node> getNavigableChildren(Node container) {
        if (!(container instanceof Parent p))
            return List.of();

        return p.getChildrenUnmodifiable().stream()
                .filter(HBox.class::isInstance)
                .map(HBox.class::cast)
                .flatMap(h -> h.getChildren().stream())
                .filter(n -> n instanceof RadioButton || n instanceof Button)
                .toList();
    }

    private void onSave() {
        model.applyToSetting();
        router.setStageSize();
    }

    private void onReset() {
        model.resetToSetting();
        reflectModelToView();
        router.setStageSize();
    }

    private void onBackToStart() {
        if (router != null) {
            model.updateModelFromSettings();
            reflectModelToView();
            router.showStartMenu();
        }
    }

    private void onResetNormalScoreboard() {
        // 콜백 실행 (필요시)
    }

    private void onResetItemScoreboard() {
        // 콜백 실행 (필요시)
    }

    // 모델의 상태를 UI에 반영
    private void reflectModelToView() {
        for (Toggle toggle : colorBlindGroup.getToggles())
            if (toggle.getUserData().equals(model.isColorBlind())) {
                toggle.setSelected(true);
                break;
            }
        for (Toggle toggle : keyLayoutGroup.getToggles())
            if (toggle.getUserData().equals(model.getKeyLayout())) {
                toggle.setSelected(true);
                break;
            }
        for (Toggle toggle : screenGroup.getToggles())
            if (toggle.getUserData().equals(model.getScreen())) {
                toggle.setSelected(true);
                break;
            }
        for (Toggle toggle : difficultyGroup.getToggles())
            if (toggle.getUserData().equals(model.getDifficulty())) {
                toggle.setSelected(true);
                break;
            }
    }

    // radio button들의 선택 변경 리스너 설정 -> 모델 업데이트
    // setting 반영은 저장 시점에 수행
    private void setupRadioButtonListeners() {
        colorBlindGroup.selectedToggleProperty().addListener((obs, o, n) -> {
            if (n != null)
                model.setColorBlind((Boolean) n.getUserData());
        });
        keyLayoutGroup.selectedToggleProperty().addListener((obs, o, n) -> {
            if (n != null)
                model.setKeyLayout((String) n.getUserData());
        });
        screenGroup.selectedToggleProperty().addListener((obs, o, n) -> {
            if (n != null)
                model.setScreen((String) n.getUserData());
        });
        difficultyGroup.selectedToggleProperty().addListener((obs, o, n) -> {
            if (n != null)
                model.setDifficulty((String) n.getUserData());
        });
    }

    // 반응형 크기 조정
    private void applyResponsiveSizing() {
        Platform.runLater(this::updateSizes);
        root.widthProperty().addListener((obs, o, n) -> updateSizes());
        root.heightProperty().addListener((obs, o, n) -> updateSizes());
    }

    private void updateSizes() {
        double w = root.getWidth(), h = root.getHeight();
        if (w <= 0 || h <= 0)
            return;
        double val = Math.min(w, h);

        updateTitlePaneSize(w, h, val);
        
        updateScoreBoardButtonSize(w, h);
        
        updateBottomButtonSize(w, h);
    }

    private void updateTitlePaneSize(double w, double h, double val) {
        mainVBox.setSpacing(clamp(val * 0.04, 10, 50));

        // TitledPane 크기 설정
        double paneWidth = clamp(w * 0.7, 200, 800);
        double paneMinHeight = clamp(h * 0.12, 50, 150);
        String s = "-fx-font-size:" + Math.round(clamp(val / 40.0, 12, 22)) + "px;";

        mainVBox.getChildren().forEach(n -> {
            if (n instanceof TitledPane) {
                ((TitledPane) n).setPrefWidth(paneWidth);
                ((TitledPane) n).setMinHeight(paneMinHeight);
                ((TitledPane) n).setStyle(s);
                applyFontToPane((TitledPane) n, s);
            }
        });
    }

    private void updateScoreBoardButtonSize(double w, double h) {
        // Scoreboard 버튼 크기 설정
        if (btnResetNormalScoreboard != null && btnResetItemScoreboard != null) {
            List<Button> p = List.of(btnResetNormalScoreboard, btnResetItemScoreboard);
            double bW = clamp(w * 0.35, 180, 420);
            double bH = clamp(h * 0.08, 20, 50);
            double bFont = clamp(bW * 0.05, 10, 20);
            for (Button x : p) {
                x.setPrefWidth(bW);
                x.setPrefHeight(bH);
                x.setStyle("-fx-font-size:" + bFont + "px;");
            }
        }
    }

    private void updateBottomButtonSize(double w, double h) {
        buttonBox.setSpacing(clamp(w * 0.05, 30, 150));

        // 하단 버튼 크기 설정
        List<Button> b = List.of(btnSave, btnReset, btnBackToStart);
        double bW = clamp(w * 0.35, 100, 400);
        double bH = clamp(h * 0.1, 10, 60);
        double bFont = clamp(bW * 0.1, 10, 20);
        for (Button x : b) {
            x.setPrefWidth(bW);
            x.setPrefHeight(bH);
            x.setStyle("-fx-font-size:" + bFont + "px;");
        }
    }

    private void applyFontToPane(TitledPane p, String s) {
        if (p.getContent() instanceof VBox)
            ((VBox) p.getContent()).getChildren().forEach(n -> {
                if (n instanceof HBox) {
                    ((HBox) n).getChildren().forEach(c -> {
                        if (c instanceof RadioButton)
                            c.setStyle(s);
                    });
                }
            });
    }

    private static double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }
}
