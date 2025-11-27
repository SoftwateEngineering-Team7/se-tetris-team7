package org.tetris.menu.setting.controller;

import org.tetris.Router;
import org.tetris.menu.setting.model.SettingMenuModel;
import org.tetris.shared.BaseController;
import org.tetris.shared.RouterAware;
import org.util.Difficulty;
import org.util.PlayerId;
import org.util.ScreenPreset;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.*;
import java.util.function.Consumer;

public class SettingMenuController extends BaseController<SettingMenuModel> implements RouterAware {

    private static final String STYLE_HIGHLIGHTED = "highlighted"; // 키보드/마우스 탐색 시 강조에 쓰는 CSS 클래스명

    public SettingMenuController(SettingMenuModel model) {
        super(model);
    }

    @FXML
    private BorderPane root;
    @FXML
    private VBox mainVBox; // 각 설정 그룹(TitledPane)들을 세로로 담는 컨테이너

    // --- Toggle Groups
    private ToggleGroup colorBlindGroup; // 색각/일반 모드 선택 묶음
    private ToggleGroup screenGroup; // 스크린 프리셋 선택 묶음
    private ToggleGroup difficultyGroup; // 난이도 선택 묶음

    // --- Label List
    private final List<Label> playerRowLabels = new ArrayList<>();

    // --- Key Binding Buttons (데이터 주도)
    private final Map<String, Button> keyButtonsP1 = new LinkedHashMap<>(); // P1 방향/동작별 키 바인딩 버튼 레지스트리
    private final Map<String, Button> keyButtonsP2 = new LinkedHashMap<>(); // P2 방향/동작별 키 바인딩 버튼 레지스트리

    // --- Bottom buttons (FXML 주입)
    @FXML
    private HBox buttonBox;
    @FXML
    private Button btnSave;
    @FXML
    private Button btnReset;
    @FXML
    private Button btnBackToStart;

    private Button btnResetNormalScoreboard;
    private Button btnResetItemScoreboard;

    private Router router;

    // --- Focus/Highlight
    private final List<Node> focusableItems = new ArrayList<>(); // 키보드 탐색 대상 노드 목록
    private int highlightedItemIndex = -1; // 현재 강조중인 인덱스

    // --- Key binding state
    private Button currentKeyBindingButton = null; // 현재 키 입력을 기다리는 버튼
    private String currentKeyBindingDirection = null; // 현재 바인딩 대상 동작(예: "LEFT", "UP2" 등)
    private String previousKeyBindingButtonText = null; // 취소/에러 시 복구를 위한 버튼 기존 텍스트

    // --- Config data (데이터 주도 정의)
    private static final String[][] SCREEN_PRESETS = {
            { ScreenPreset.SMALL_STRING, ScreenPreset.SMALL_SIZE },
            { ScreenPreset.MIDDLE_STRING, ScreenPreset.MIDDLE_SIZE },
            { ScreenPreset.LARGE_STRING, ScreenPreset.LARGE_SIZE }
    };
    private static final String[] DIFFICULTIES = {
            Difficulty.EASY_STRING, Difficulty.NORMAL_STRING, Difficulty.HARD_STRING
    };
    private static final String[][] P1_KEYS = {
            { "LEFT", "왼쪽" }, { "RIGHT", "오른쪽" }, { "UP", "회전" }, { "DOWN", "아래" }, { "HARD_DROP", "하드 드롭" }
    };
    private static final String[][] P2_KEYS = {
            { "LEFT2", "왼쪽" }, { "RIGHT2", "오른쪽" }, { "UP2", "회전" }, { "DOWN2", "아래" }, { "HARD_DROP2", "하드 드롭" }
    };

    @FXML
    protected void initialize() {
        super.initialize();
        // 1) Build panes
        setupDynamicPanes(); // 각 설정 그룹(색약/키세팅/스크린/난이도/스코어보드) 동적 구성

        // 2) Wire listeners
        setupPaneListeners(); // TitledPane 접힘/펼침 시 포커스 리스트 재생성 리스너
        setupRadioButtonListeners(); // 라디오버튼 선택 → 모델 반영 리스너

        // 3) Sync model -> view
        reflectModelToView(); // 현재 모델 상태를 UI에 반영

        // 4) Focus list + input
        buildFocusableList(); // 키보드 탐색 대상 목록 구성

        Platform.runLater(() -> {
            bindInput(); // 키보드/마우스 입력 바인딩
            setupMouseEvents(); // 항목별 마우스 Hover/Click 핸들러 설치
            setHighlighted(0); // 첫 항목 강조
            root.requestFocus();// 씬 포커스 가져오기
        });

        // 5) Responsive sizing
        applyResponsiveSizing(); // 창 크기에 따른 폰트/버튼 크기 등 반응형 조절
    }

    @Override
    public void setRouter(Router router) {
        this.router = router; // 라우터 주입
        btnSave.setOnAction(e -> onSave()); // 저장 동작 바인딩
        btnReset.setOnAction(e -> onReset()); // 초기화 동작 바인딩
        btnBackToStart.setOnAction(e -> onBackToStart()); // 시작 화면 복귀 동작 바인딩
    }

    // ------------------------------
    // UI BUILD (데이터 주도 + 공통 헬퍼)
    // ------------------------------

    private void setupDynamicPanes() {
        // ToggleGroup 인스턴스 생성
        colorBlindGroup = new ToggleGroup();
        screenGroup = new ToggleGroup();
        difficultyGroup = new ToggleGroup();

        // 색약 모드 그룹 생성
        addPane("색약 모드", box -> {
            HBox row = row(20);
            row.getChildren().addAll(
                    radio(row, colorBlindGroup, "일반 시각", false), // userData=false
                    radio(row, colorBlindGroup, "색약 모드", true)); // userData=true
            box.getChildren().add(row);
        });

        // 키 세팅 그룹(P1/P2) 생성
        addPane("키 세팅", box -> {
            javafx.scene.text.Text desc = new javafx.scene.text.Text("버튼 클릭 후 원하는 키를 누르세요 (ESC 취소)");
            desc.setStyle("-fx-fill: #888; -fx-font-size: 12px;");
            box.getChildren().add(desc);

            box.getChildren().add(keyRow("Player 1 : ", P1_KEYS, keyButtonsP1)); // P1 키 버튼 행 생성/등록
            box.getChildren().add(keyRow("Player 2 : ", P2_KEYS, keyButtonsP2)); // P2 키 버튼 행 생성/등록
        });

        // 스크린 사이즈 프리셋 그룹 생성
        addPane("Screen Size", box -> {
            HBox row = row(20);
            for (String[] preset : SCREEN_PRESETS) {
                RadioButton rb = new RadioButton(preset[1]);
                rb.setToggleGroup(screenGroup);
                rb.setUserData(preset[0]);
                rb.setFocusTraversable(false);
                row.getChildren().add(rb);
            }
            box.getChildren().add(row);
        });

        // 난이도 그룹 생성
        addPane("난이도", box -> {
            HBox row = row(15);
            for (String d : DIFFICULTIES) {
                RadioButton rb = new RadioButton(d);
                rb.setToggleGroup(difficultyGroup);
                rb.setUserData(d); // 내부값: EASY/NORMAL/HARD
                rb.setFocusTraversable(false);
                row.getChildren().add(rb);
            }
            box.getChildren().add(row);
        });

        // 스코어보드 초기화 그룹 생성
        addPane("Scoreboard", box -> {
            HBox buttonContainer = row(20);
            btnResetNormalScoreboard = button("일반 모드 스코어보드 초기화", e -> onResetNormalScoreboard());
            btnResetItemScoreboard = button("아이템 모드 스코어보드 초기화", e -> onResetItemScoreboard());
            buttonContainer.getChildren().addAll(btnResetNormalScoreboard, btnResetItemScoreboard);
            box.getChildren().add(buttonContainer);
        });
    }

    private void addPane(String title, Consumer<VBox> contentBuilder) {
        // 주어진 제목과 Builder를 이용해 TitledPane 하나를 생성/삽입
        VBox content = new VBox(12);
        content.getStyleClass().add("group");

        TitledPane p = new TitledPane(title, content);
        p.setExpanded(true); // 기본 펼침
        p.setFocusTraversable(false); // 포커스는 단일 root에 몰아줌

        mainVBox.getChildren().add(p);
        contentBuilder.accept(content); // 내용 구성 콜백 실행
    }

    private HBox keyRow(String label, String[][] defs, Map<String, Button> registry) {
        // "Player X :" 라벨과 방향별 키 바인딩 버튼들을 한 줄로 생성하고 레지스트리에 보관
        HBox row = row(15);
        Label playerLabel = new Label(label); // ← Label로 변경
        playerLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
        playerRowLabels.add(playerLabel); // 라벨 목록에 추가 -> 동일 폭 적용을 위한 저장
        
        row.getChildren().add(playerLabel);

        for (String[] def : defs) {
            String dir = def[0], text = def[1];
            Button b = keyButton(text, dir); // dir에 해당하는 키 바인딩 버튼 생성
            registry.put(dir, b); // 레지스트리에 저장
            row.getChildren().add(b);
        }
        return row;
    }

    private HBox row(double gap) {
        // gap 간격의 HBox 생성(좌측 정렬)
        HBox r = new HBox(gap);
        r.setAlignment(Pos.CENTER_LEFT);
        return r;
    }

    private RadioButton radio(Node owner, ToggleGroup group, String label, Object userData) {
        // 공통 라디오버튼 생성 유틸(userData로 내부값 보관)
        RadioButton rb = new RadioButton(label);
        rb.setToggleGroup(group);
        rb.setUserData(userData);
        rb.setFocusTraversable(false);
        return rb;
    }

    private Button button(String label, javafx.event.EventHandler<javafx.event.ActionEvent> onClick) {
        // 공통 버튼 생성 유틸(포커스탐색 제외)
        Button b = new Button(label);
        b.setFocusTraversable(false);
        b.setOnAction(onClick);
        return b;
    }

    private Button keyButton(String label, String direction) {
        // 키 바인딩용 버튼 생성(클릭 시 startKeyBinding 진입)
        Button btn = new Button(label + ":");
        btn.setFocusTraversable(false);
        btn.getStyleClass().add("key-binding-button");
        btn.setMinWidth(100);
        btn.setPrefWidth(150);
        btn.setMaxWidth(200);
        btn.setOnAction(e -> {
            e.consume();
            startKeyBinding(btn, direction); // 해당 방향에 대해 키 입력 대기 상태로 전환
        });
        return btn;
    }

    // ------------------------------
    // Mouse + Keyboard Wiring
    // ------------------------------

    private void setupPaneListeners() {
        // TitledPane이 접히거나 펼쳐질 때 포커스 대상 목록을 재구성하고 기존 강조를 최대한 유지
        mainVBox.getChildren().stream()
                .filter(n -> n instanceof TitledPane)
                .map(n -> (TitledPane) n)
                .forEach(pane -> pane.expandedProperty().addListener((obs, o, n) -> {
                    int old = highlightedItemIndex;
                    buildFocusableList();
                    setupMouseEvents();
                    if (old >= 0 && old < focusableItems.size())
                        setHighlighted(old);
                    else
                        setHighlighted(0);
                }));
    }

    private void setupMouseEvents() {
        // focusableItems 목록을 순회하며 타입에 따라 적절한 마우스 이벤트 핸들러 부착
        for (int i = 0; i < focusableItems.size(); i++) {
            final int index = i;
            Node item = focusableItems.get(i);

            if (item instanceof TitledPane) {
                setMouseEventOnTitlePane(index, item);
            } else if (item instanceof RadioButton) {
                setMouseEventOnRadioButton(index, item);
            } else if (item instanceof Button) {
                Button b = (Button) item;
                if (b.getStyleClass().contains("key-binding-button")) {
                    setMouseEventOnKeyBindingButton(index, b); // 키바인딩 버튼 전용 핸들러
                } else {
                    setMouseEventOnButton(index, item);
                }
            }
        }
    }

    private void setMouseEventOnTitlePane(final int index, Node item) {
        // TitledPane의 title 영역에 Hover/Click 시 강조/포커스 반환
        TitledPane pane = (TitledPane) item;
        Runnable install = () -> {
            Node title = pane.lookup(".title");
            if (title == null)
                return;

            title.addEventHandler(MouseEvent.MOUSE_ENTERED, e -> setHighlighted(index));
            // 기본 토글 동작은 유지하되, 클릭 시 강조 인덱스 반영 및 포커스 회수
            title.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
                setHighlighted(index);
                root.requestFocus();
            });
            title.setPickOnBounds(true);
        };
        if (pane.getScene() == null)
            Platform.runLater(install); // 씬 장착 이후 조회 필요 시 지연 설치
        else
            install.run();
    }

    private void setMouseEventOnButton(final int index, Node item) {
        // 일반 버튼의 Hover/Click 처리 및 바인딩 중이면 선취소
        Button b = (Button) item;

        b.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_PRESSED, e -> {
            if (currentKeyBindingButton != null) {
                cancelKeyBinding(); // 다른 버튼 클릭 시 기존 바인딩 취소
            }
        });

        b.setOnMouseEntered(e -> setHighlighted(index));
        b.setOnMouseClicked(e -> {
            setHighlighted(index);
            b.fire(); // 직접 클릭과 동일하게 Action 트리거
        });
    }

    private void setMouseEventOnRadioButton(final int index, Node item) {
        // 라디오 버튼의 Hover/Click 처리 및 바인딩 중이면 선취소
        RadioButton rb = (RadioButton) item;

        rb.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_PRESSED, e -> {
            if (currentKeyBindingButton != null) {
                cancelKeyBinding();
            }
        });

        rb.setOnMouseEntered(e -> setHighlighted(index));
        rb.setOnMouseClicked(e -> {
            setHighlighted(index);
            rb.setSelected(true); // 클릭 시 선택 확정
        });
    }

    private void setMouseEventOnKeyBindingButton(final int index, Button btn) {
        // 키 바인딩 버튼 전용: Hover 시 강조, 다른 바인딩 진행 중이면 클릭 직전에 취소
        btn.setOnMouseEntered(e -> setHighlighted(index));

        btn.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_PRESSED, e -> {
            if (currentKeyBindingButton != null && currentKeyBindingButton != btn) {
                cancelKeyBinding(); // 다른 버튼을 누르는 경우 기존 대기 취소
            }
        });
    }

    private void bindInput() {
        // Scene 키보드/마우스 입력 바인딩(포커스를 root가 가져야 동작)
        if (root.getScene() == null)
            return;

        root.setFocusTraversable(true);

        root.getScene().setOnKeyPressed(e -> {
            if (currentKeyBindingButton != null && currentKeyBindingDirection != null) {
                handleKeyBinding(e.getCode(), currentKeyBindingDirection); // 바인딩 대기 중: 입력 키를 해당 동작에 할당
                e.consume();
                return;
            }
            handleKeyPress(e.getCode()); // 일반 탐색/확정 처리
            e.consume();
        });

        // 빈 공간 클릭 시 포커스 회수(키 탐색 유지)
        root.getScene().setOnMouseClicked(e -> {
            if (currentKeyBindingButton == null)
                root.requestFocus();
        });

        // 하단 버튼들은 포커스 순회 대상에서 제외
        btnBackToStart.setFocusTraversable(false);
        btnReset.setFocusTraversable(false);
        btnSave.setFocusTraversable(false);
    }

    private void handleKeyPress(KeyCode key) {
        // 키보드 탐색: ↑/←은 이전, ↓/→는 다음, Enter는 활성화
        if (key == KeyCode.UP || key == KeyCode.LEFT) {
            moveTo(-1);
        } else if (key == KeyCode.DOWN || key == KeyCode.RIGHT) {
            moveTo(+1);
        } else if (key == KeyCode.ENTER) {
            activateCurrentItem();
        }
    }

    // ------------------------------
    // Highlight / Focus
    // ------------------------------

    private void buildFocusableList() {
        // mainVBox의 TitledPane 및 펼쳐진 내용물 중 Button/RadioButton을 순회 대상으로 수집
        focusableItems.clear();

        for (Node node : mainVBox.getChildren()) {
            if (node instanceof TitledPane pane) {
                focusableItems.add(pane);
                if (pane.isExpanded()) {
                    Node content = pane.getContent();
                    if (content instanceof Parent p) {
                        getNavigableChildren(p).forEach(focusableItems::add);
                    }
                }
            }
        }
        // 하단 버튼 3개를 끝에 추가
        focusableItems.addAll(Arrays.asList(btnBackToStart, btnReset, btnSave));
    }

    private List<Node> getNavigableChildren(Parent p) {
        // 자식 중 HBox → 그 자식들 중 RadioButton/Button만 뽑아 순회 대상 구성
        return p.getChildrenUnmodifiable().stream()
                .filter(HBox.class::isInstance)
                .map(HBox.class::cast)
                .flatMap(h -> h.getChildren().stream())
                .filter(n -> n instanceof RadioButton || n instanceof Button)
                .toList();
    }

    private void setHighlighted(int newIndex) {
        // 강조 인덱스 갱신(CSS 클래스 추가/제거)
        if (newIndex < 0 || newIndex >= focusableItems.size())
            return;
        if (newIndex == highlightedItemIndex)
            return;

        clearHighlight();
        highlightedItemIndex = newIndex;

        Node n = focusableItems.get(highlightedItemIndex);
        if (!n.getStyleClass().contains(STYLE_HIGHLIGHTED))
            n.getStyleClass().add(STYLE_HIGHLIGHTED);
    }

    private void clearHighlight() {
        // 기존 강조 노드에서 강조 CSS 클래스 제거
        if (highlightedItemIndex >= 0 && highlightedItemIndex < focusableItems.size()) {
            focusableItems.get(highlightedItemIndex).getStyleClass().remove(STYLE_HIGHLIGHTED);
        }
    }

    private void moveTo(int delta) {
        // 강조 인덱스를 순환 이동(앞/뒤)
        if (focusableItems.isEmpty())
            return;
        int size = focusableItems.size();
        int newIndex = ((highlightedItemIndex + delta) % size + size) % size;
        setHighlighted(newIndex);
    }

    private void activateCurrentItem() {
        // 현재 강조 항목을 타입에 맞게 활성화(라디오 선택/버튼 클릭/타이틀 토글)
        if (highlightedItemIndex < 0 || highlightedItemIndex >= focusableItems.size())
            return;

        Node cur = focusableItems.get(highlightedItemIndex);
        if (cur instanceof RadioButton rb) {
            rb.setSelected(true);
        } else if (cur instanceof Button b) {
            b.fire();
        } else if (cur instanceof TitledPane pane) {
            pane.setExpanded(!pane.isExpanded());
        }
    }

    // ------------------------------
    // Key Binding
    // ------------------------------

    private void startKeyBinding(Button btn, String direction) {
        // 기존 대기 상태를 정리하고, 새 버튼을 '키 입력 대기' 상태로 진입시킴
        if (currentKeyBindingButton != null) {
            currentKeyBindingButton.getStyleClass().remove("waiting-for-key");
            if (previousKeyBindingButtonText != null) {
                currentKeyBindingButton.setText(previousKeyBindingButtonText);
            }
        }
        previousKeyBindingButtonText = btn.getText();
        currentKeyBindingButton = btn;
        currentKeyBindingDirection = direction;
        btn.getStyleClass().add("waiting-for-key");
        btn.setText("키 입력");
        root.requestFocus(); // 키 입력이 Scene으로 들어오도록 포커스 회수
    }

    private void handleKeyBinding(KeyCode key, String direction) {
        // 키 입력을 받아 해당 동작에 매핑하고, 중복 검사/표시 업데이트 처리
        if (currentKeyBindingButton == null)
            return;

        if (key == null || key == KeyCode.UNDEFINED) {
            showKeyBindingError("지원 불가 키"); // 정의되지 않은 키는 바인딩 불가
            return;
        }

        if (key == KeyCode.ESCAPE) {
            cancelKeyBinding(); // ESC로 바인딩 취소
            return;
        }

        String keyName = key.name();

        // Player 1: 중복 검사 후 모델 저장
        if (!direction.endsWith("2")) {
            if (isKeyAlreadyBound(keyName, direction)) {
                showKeyBindingError("이미 사용 중"); // 중복일 경우 1초간 안내 후 복구
                return;
            }
            switch (direction) {
                case "LEFT" -> model.setKeyLeft(PlayerId.PLAYER1, keyName);
                case "RIGHT" -> model.setKeyRight(PlayerId.PLAYER1, keyName);
                case "DOWN" -> model.setKeyDown(PlayerId.PLAYER1, keyName);
                case "UP" -> model.setKeyUp(PlayerId.PLAYER1, keyName);
                case "HARD_DROP" -> model.setKeyHardDrop(PlayerId.PLAYER1, keyName);
            }
        } else {
            if (isKeyAlreadyBound(keyName, direction)) {
                showKeyBindingError("이미 사용 중"); // 중복일 경우 1초간 안내 후 복구
                return;
            }
            switch (direction) {
                case "LEFT2" -> model.setKeyLeft(PlayerId.PLAYER2, keyName);
                case "RIGHT2" -> model.setKeyRight(PlayerId.PLAYER2, keyName);
                case "DOWN2" -> model.setKeyDown(PlayerId.PLAYER2, keyName);
                case "UP2" -> model.setKeyUp(PlayerId.PLAYER2, keyName);
                case "HARD_DROP2" -> model.setKeyHardDrop(PlayerId.PLAYER2, keyName);
            }
        }

        // 상태 해제 + 버튼 라벨 갱신
        currentKeyBindingButton.getStyleClass().remove("waiting-for-key");
        currentKeyBindingButton = null;
        currentKeyBindingDirection = null;
        previousKeyBindingButtonText = null;

        updateKeyBindingButtons(); // 모든 키 바인딩 버튼 텍스트 새로고침
    }

    private void cancelKeyBinding() {
        // '키 입력 대기' 상태를 취소하고 버튼 텍스트를 이전 값으로 복원
        if (currentKeyBindingButton != null) {
            currentKeyBindingButton.getStyleClass().remove("waiting-for-key");
            if (previousKeyBindingButtonText != null)
                currentKeyBindingButton.setText(previousKeyBindingButtonText);
            currentKeyBindingButton = null;
        }
        currentKeyBindingDirection = null;
        previousKeyBindingButtonText = null;
    }

    private boolean isKeyAlreadyBound(String keyName, String exceptDirection) {
        // Player 1의 키와 겹치는지 검사
        boolean p1Overlap = 
                (!"LEFT".equals(exceptDirection) && keyEquals(model.getKeyLeft(PlayerId.PLAYER1), keyName)) ||
                (!"RIGHT".equals(exceptDirection) && keyEquals(model.getKeyRight(PlayerId.PLAYER1), keyName)) ||
                (!"DOWN".equals(exceptDirection) && keyEquals(model.getKeyDown(PlayerId.PLAYER1), keyName)) ||
                (!"UP".equals(exceptDirection) && keyEquals(model.getKeyUp(PlayerId.PLAYER1), keyName)) ||
                (!"HARD_DROP".equals(exceptDirection) && keyEquals(model.getKeyHardDrop(PlayerId.PLAYER1), keyName));

        // Player 2의 키와 겹치는지 검사
        boolean p2Overlap = 
                (!"LEFT2".equals(exceptDirection) && keyEquals(model.getKeyLeft(PlayerId.PLAYER2), keyName)) ||
                (!"RIGHT2".equals(exceptDirection) && keyEquals(model.getKeyRight(PlayerId.PLAYER2), keyName)) ||
                (!"DOWN2".equals(exceptDirection) && keyEquals(model.getKeyDown(PlayerId.PLAYER2), keyName)) ||
                (!"UP2".equals(exceptDirection) && keyEquals(model.getKeyUp(PlayerId.PLAYER2), keyName)) ||
                (!"HARD_DROP2".equals(exceptDirection) && keyEquals(model.getKeyHardDrop(PlayerId.PLAYER2), keyName));

        // 둘 중 하나라도 겹치면 true 반환
        return p1Overlap || p2Overlap;
    }

    private boolean keyEquals(String a, String b) {
        // null 안전 동등성 비교
        return a != null && a.equals(b);
    }

    private void showKeyBindingError(String message) {
        // 중복 키일 때 잠시 에러 문구를 보여주고, 1초 뒤 UI 복원
        if (currentKeyBindingButton == null)
            return;
        currentKeyBindingButton.setText(message);
        new Thread(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {
            }
            Platform.runLater(() -> {
                updateKeyBindingButtons();
                cancelKeyBinding();
            });
        }).start();
    }

    private String getKeyDisplayName(KeyCode key) {
        // 키코드를 사용자 친화적인 표기로 변환(화살표/스페이스/엔터/수정키 등)
        return switch (key) {
            case UP -> "↑";
            case DOWN -> "↓";
            case LEFT -> "←";
            case RIGHT -> "→";
            case CONTROL -> "Ctrl";
            case BACK_SPACE -> "Back";
            case CAPS -> "Caps";
            case WINDOWS -> "Win";
            case PAGE_UP -> "PgUp";
            case PAGE_DOWN -> "PgDn";
            case DELETE -> "Del";
            default -> truncateToCodePoints(key.getName(), 6); // ← 여기서 6글자 제한
        };
    }

    // 문자열을 최대 max 개로 자르는 헬퍼
    private static String truncateToCodePoints(String s, int max) {
        if (s == null) {
            return "";
        }

        // 1차 최적화: 문자열 길이(char 개수)가 max보다 작거나 같으면
        // 코드포인트 개수도 무조건 max 이하이므로 바로 반환 (O(1))
        if (s.length() <= max) {
            return s;
        }

        // 2차 검사: 실제 코드포인트 개수가 max 이하인 경우 바로 반환
        // (이모지 등이 섞여 있어 길이는 길지만 실제 글자 수는 적은 경우 대비)
        if (s.codePointCount(0, s.length()) <= max) {
            return s;
        }

        // max 개수만큼의 코드포인트가 위치한 인덱스를 찾아서 자름
        int endIndex = s.offsetByCodePoints(0, max);
        return s.substring(0, endIndex);
    }

    private void updateKeyBindingButtons() {
        // 모델의 현재 바인딩 값을 읽어 모든 키 버튼 라벨을 갱신
        setKeyText(keyButtonsP1.get("LEFT"), "왼쪽", model.getKeyLeft(PlayerId.PLAYER1));
        setKeyText(keyButtonsP1.get("RIGHT"), "오른쪽", model.getKeyRight(PlayerId.PLAYER1));
        setKeyText(keyButtonsP1.get("DOWN"), "아래", model.getKeyDown(PlayerId.PLAYER1));
        setKeyText(keyButtonsP1.get("UP"), "회전", model.getKeyUp(PlayerId.PLAYER1));
        setKeyText(keyButtonsP1.get("HARD_DROP"), "하드 드롭", model.getKeyHardDrop(PlayerId.PLAYER1));

        setKeyText(keyButtonsP2.get("LEFT2"), "왼쪽", model.getKeyLeft(PlayerId.PLAYER2));
        setKeyText(keyButtonsP2.get("RIGHT2"), "오른쪽", model.getKeyRight(PlayerId.PLAYER2));
        setKeyText(keyButtonsP2.get("DOWN2"), "아래", model.getKeyDown(PlayerId.PLAYER2));
        setKeyText(keyButtonsP2.get("UP2"), "회전", model.getKeyUp(PlayerId.PLAYER2));
        setKeyText(keyButtonsP2.get("HARD_DROP2"), "하드 드롭", model.getKeyHardDrop(PlayerId.PLAYER2));
    }

    private void setKeyText(Button b, String label, String keyName) {
        String pretty = getKeyDisplayName(KeyCode.valueOf(keyName));
        b.setText(label + ": " + pretty);
    }

    // ------------------------------
    // Model <-> View
    // ------------------------------

    private void reflectModelToView() {
        // 모델값을 읽어 라디오/키 버튼 상태를 UI에 반영
        for (Toggle t : colorBlindGroup.getToggles())
            if (Objects.equals(t.getUserData(), model.isColorBlind())) {
                t.setSelected(true);
                break;
            }

        updateKeyBindingButtons();

        for (Toggle t : screenGroup.getToggles())
            if (Objects.equals(t.getUserData(), model.getScreen())) {
                t.setSelected(true);
                break;
            }

        for (Toggle t : difficultyGroup.getToggles())
            if (Objects.equals(t.getUserData(), model.getDifficulty())) {
                t.setSelected(true);
                break;
            }
    }

    private void setupRadioButtonListeners() {
        // 라디오 버튼 변경 시 모델에 즉시 반영
        colorBlindGroup.selectedToggleProperty().addListener((obs, o, n) -> {
            if (n != null)
                model.setColorBlind((Boolean) n.getUserData());
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

    // ------------------------------
    // Actions
    // ------------------------------

    private void onSave() {
        // 현재 UI/모델 상태를 실제 설정에 반영하고 스테이지 크기 재적용
        model.applyToSetting();
        router.setStageSize();
    }

    private void onReset() {
        // 현재 설정값으로 모델을 되돌리고 UI 반영, 스테이지 크기 재적용
        model.resetToSetting();
        reflectModelToView();
        router.setStageSize();
    }

    private void onBackToStart() {
        // 모델을 Settings로부터 최신화하고 UI 반영, 시작 메뉴로 전환
        if (router != null) {
            model.updateModelFromSettings();
            reflectModelToView();
            router.showStartMenu();
        }
    }

    private void onResetNormalScoreboard() {
        // 일반 모드 스코어보드 초기화 요청
        router.getScoreBoardController().clearScoreBoard(false);
    }

    private void onResetItemScoreboard() {
        // 아이템 모드 스코어보드 초기화 요청
        router.getScoreBoardController().clearScoreBoard(true);
    }

    // ------------------------------
    // Responsive
    // ------------------------------

    private void applyResponsiveSizing() {
        // 최초 1회와 크기 변경 시마다 updateSizes() 호출되도록 바인딩
        Platform.runLater(this::updateSizes);
        root.widthProperty().addListener((obs, o, n) -> updateSizes());
        root.heightProperty().addListener((obs, o, n) -> updateSizes());
    }

    private void updateSizes() {
        // 창 크기에 비례해 폰트/버튼/간격을 계산해 적용
        double w = root.getWidth(), h = root.getHeight();
        if (w <= 0 || h <= 0)
            return;
        double val = Math.min(w, h);

        mainVBox.setSpacing(clamp(val * 0.02, 15, 50));

        double paneWidth = clamp(w * 0.7, 200, 800);
        double paneMinHeight = clamp(h * 0.12, 50, 150);
        String s = "-fx-font-size:" + Math.round(clamp(val / 40.0, 12, 22)) + "px;";

        mainVBox.getChildren().forEach(n -> {
            if (n instanceof TitledPane tp) {
                tp.setPrefWidth(paneWidth);
                tp.setMinHeight(paneMinHeight);
                tp.setStyle(s);
                applyFontToPane(tp, s);
            }
        });

        // Player 라벨 폭을 창 크기에 비례해 통일
        double labelW = clamp(w * 0.1, 80, 160); // 비율/최소/최대는 상황에 맞게 조정
        for (Label L : playerRowLabels) {
            L.setMinWidth(labelW);
            L.setPrefWidth(labelW);
            L.setStyle("-fx-text-fill: white; -fx-font-size:" + Math.round(clamp(val / 40.0, 14, 22))
                    + "px; -fx-font-weight: bold;");
        }

        // 키 바인딩 버튼 통합 사이즈 조절
        Consumer<Collection<Button>> sizeButtons = buttons -> {
            double bW = clamp(w * 0.2, 150, 180);
            double bH = clamp(h * 0.06, 30, 50);
            double bFont = clamp(bW * 0.06, 9, 15);
            for (Button x : buttons) {
                x.setPrefWidth(bW);
                x.setPrefHeight(bH);
                x.setStyle("-fx-font-size:" + bFont + "px;");
            }
        };
        sizeButtons.accept(keyButtonsP1.values());
        sizeButtons.accept(keyButtonsP2.values());

        // 스코어보드 초기화 버튼 사이즈
        List<Button> sb = Arrays.asList(btnResetNormalScoreboard, btnResetItemScoreboard);
        double bW = clamp(w * 0.35, 180, 420);
        double bH = clamp(h * 0.08, 20, 50);
        double bFont = clamp(bW * 0.05, 10, 20);
        for (Button x : sb)
            if (x != null) {
                x.setPrefWidth(bW);
                x.setPrefHeight(bH);
                x.setStyle("-fx-font-size:" + bFont + "px;");
            }

        // 하단 버튼(저장/초기화/뒤로) 사이즈 및 간격
        buttonBox.setSpacing(clamp(w * 0.05, 30, 150));
        List<Button> bottom = Arrays.asList(btnSave, btnReset, btnBackToStart);
        double bw = clamp(w * 0.35, 100, 400);
        double bh = clamp(h * 0.1, 10, 60);
        double bf = clamp(bw * 0.1, 10, 20);
        for (Button x : bottom) {
            x.setPrefWidth(bw);
            x.setPrefHeight(bh);
            x.setStyle("-fx-font-size:" + bf + "px;");
        }
    }

    private void applyFontToPane(TitledPane p, String s) {
        // TitledPane 내부의 라디오버튼 폰트 크기를 일괄 적용
        if (p.getContent() instanceof VBox v) {
            v.getChildren().forEach(n -> {
                if (n instanceof HBox h) {
                    h.getChildren().forEach(c -> {
                        if (c instanceof RadioButton)
                            c.setStyle(s);
                    });
                }
            });
        }
    }

    private static double clamp(double v, double min, double max) {
        // v를 [min, max] 범위로 제한
        return Math.max(min, Math.min(max, v));
    }
}
