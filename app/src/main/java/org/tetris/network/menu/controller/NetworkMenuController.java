package org.tetris.network.menu.controller;

import org.tetris.Router;
import org.tetris.game.model.GameMode;
import org.tetris.network.GameClient;
import org.tetris.network.comand.GameMenuCommandExecutor;
import org.tetris.network.comand.ReadyCommand;
import org.tetris.network.dto.MatchSettings;
import org.tetris.network.menu.model.NetworkMenu;
import org.tetris.shared.BaseController;
import org.tetris.shared.RouterAware;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;

/**
 * 
 * TODO: 모드 선택 동기화 - 선택한 모드를 클라이언트에게 전송
 */
public class NetworkMenuController extends BaseController<NetworkMenu>
        implements RouterAware, GameMenuCommandExecutor {

    private Router router;

    @FXML
    private ToggleGroup connectionTypeGroup;
    @FXML
    private RadioButton hostRadio;
    @FXML
    private RadioButton clientRadio;
    @FXML
    private TextField ipField;
    @FXML
    private TextField portField;
    @FXML
    private ComboBox<String> gameModeCombo;
    @FXML
    private Button createButton;
    @FXML
    private TextArea logArea;
    @FXML
    private Button backButton;
    @FXML
    private Button clearLogButton;
    @FXML
    private Button readyButton;

    GameClient client;

    public NetworkMenuController(NetworkMenu networkMenu) {
        super(networkMenu);
        this.client = GameClient.getInstance();
    }

    @Override
    public void setRouter(Router router) {
        this.router = router;
    }

    @Override
    public void initialize() {
        // 모델에 컨트롤러 등록 (Ping 업데이트를 위해)
        model.pingProperty().addListener((observable, oldValue, newValue) -> {
            addLog("Ping: " + newValue + "ms");
        });

        model.otherIsReadyProperty().addListener((observable, oldValue, newValue) -> {
            addLog("다른 플레이어가 " + (newValue ? "준비" : "준비 취소") + "했습니다.");
        });

        // ComboBox 초기화
        gameModeCombo.getItems().addAll("일반 모드", "아이템 모드", "타임어택 모드");
        gameModeCombo.setValue("일반 모드");

        // ComboBox 스타일 강제 적용 (흰색 텍스트)
        gameModeCombo.setStyle(
                "-fx-background-color: #0f3460; -fx-text-fill: white; -fx-border-color: #533483; -fx-border-radius: 5; -fx-background-radius: 5;");

        // ComboBox 버튼 셀과 리스트 셀의 텍스트 색상을 흰색으로 설정
        gameModeCombo.setCellFactory(listView -> {
            return new javafx.scene.control.ListCell<String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item);
                        setStyle("-fx-text-fill: white; -fx-background-color: #0f3460;");
                    }
                }
            };
        });

        // 선택된 항목을 보여주는 버튼 셀도 흰색으로 설정
        gameModeCombo.setButtonCell(new javafx.scene.control.ListCell<String>() {

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    setStyle("-fx-text-fill: white;");
                }
            }

        });

        // RadioButton 변경 리스너
        connectionTypeGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle == hostRadio) {
                model.setIsHost(true);
                ipField.setDisable(true);
                createButton.setText("CREATE");
                gameModeCombo.setVisible(true);

                addLog("Host 모드로 설정됨");
            } else if (newToggle == clientRadio) {
                model.setIsHost(false);
                ipField.setDisable(false);
                createButton.setText("JOIN");
                gameModeCombo.setVisible(false);

                addLog("Client 모드로 설정됨");
            }
        });
        
        // 초기 로그 메시지
        addLog("네트워크 게임 초기화 완료");

        // Register as Menu Executor
        if (client != null) {
            client.setMenuExecutor(this);
        }
    }

    @Override
    public void refresh() {
        model.clear();
        hostRadio.setDisable(false);
        clientRadio.setDisable(false);
        ipField.setText(model.getIpAddress());
        portField.setText(String.valueOf(model.getPort()));
        connectionTypeGroup.selectToggle(clientRadio);
        createButton.setText("JOIN");
        readyButton.setDisable(true);
    }

    @FXML
    private void onCreatePressed() {
        try {
            String ip = ipField.getText().trim();
            String portText = portField.getText().trim();
            int port = Integer.parseInt(portText);

            String gameMode = gameModeCombo.getValue();

            try {
                model.isValidIP(ip);
                model.isValidPort(port);
            } catch (Exception e) {
                addLog("오류: " + e.getMessage());
                return;
            }

            // 모델 업데이트
            model.setIpAddress(ip);
            model.setPort(port);

            boolean isHost = model.getIsHost();
            if (isHost) {
                addLog("서버 생성 중... IP: " + ip + ", Port: " + port);
                addLog("게임 모드: " + gameMode);
                model.create();
                this.ipField.setText(model.getIpAddress());
                addLog("서버가 시작되었습니다. 클라이언트 접속을 기다리는 중...");
            } else {
                addLog("서버에 연결 중... IP: " + ip + ", Port: " + port);
                model.join();
                addLog("서버에 성공적으로 연결되었습니다.");
            }

            hostRadio.setDisable(true);
            clientRadio.setDisable(true);
            readyButton.setDisable(false);

        } catch (NumberFormatException e) {
            addLog("오류: 포트는 숫자여야 합니다");
        } catch (Exception e) {
            addLog("오류: " + e.getMessage());
        }
    }

    @FXML
    private void onBackPressed() {
        addLog("메인 메뉴로 돌아갑니다");
        if (router != null) {
            model.clear();
            router.showStartMenu();
        } else {
            addLog("오류: Router가 초기화되지 않았습니다. 메인 메뉴로 이동할 수 없습니다.");
        }
    }

    @FXML
    private void onClearLogPressed() {
        logArea.clear();
        addLog("로그가 초기화되었습니다");
    }

    @FXML
    private void onReadyPressed() {
        model.setIsReady(!model.isReady());
        if (model.isReady()) {
            addLog("준비 완료");
            readyButton.setText("CANCEL");
        } else {
            addLog("준비 취소");
            readyButton.setText("READY");
        }
        client.sendCommand(new ReadyCommand(model.isReady()));
    }

    @Override
    public void onReady(boolean others) {
        javafx.application.Platform.runLater(() -> {
            if (model == null) {
                System.out.println("[CLIENT-ENGINE] NetworkMenu is not set. Ignoring onReady.");
                return;
            }

            model.setOtherIsReady(others);
            if (others) {
                addLog("상대방 준비 완료");
                System.out.println("[CLIENT-ENGINE] Other Player is ready.");
            } else {
                addLog("상대방 준비 취소");
                System.out.println("[CLIENT-ENGINE] Other Player is not ready.");
            }
        });
    }

    @Override
    public void gameStart(MatchSettings settings) {
        javafx.application.Platform.runLater(() -> {
            System.out.println("[CLIENT-ENGINE] Both players are ready. Starting game...");
            router.showP2PGamePlaceholder(mapGameModeLabelToGameMode(gameModeCombo.getValue()), settings);
            model.setIsReady(false);
            readyButton.setText("READY");
        });
    }

    @Override
    public synchronized void updatePing(long ping) {
        System.out.println("[CLIENT-ENGINE] Ping: " + ping + "ms");
        if (model != null) {
            model.setPing(ping);
        }
    }

    /**
     * 로그 메시지를 추가합니다.
     * 
     * @param message 추가할 로그 메시지
     */
    public void addLog(String message) {
        String timestamp = java.time.LocalTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));
        logArea.appendText("[" + timestamp + "] " + message + "\n");

        // 스크롤을 맨 아래로 이동
        logArea.setScrollTop(Double.MAX_VALUE);
    }

    /**
     * 현재 선택된 게임 모드를 반환합니다.
     * 
     * @return 선택된 게임 모드
     */
    public String getSelectedGameMode() {
        return gameModeCombo.getValue();
    }

    private GameMode mapGameModeLabelToGameMode(String label) {
        switch (label) {
            case "일반 모드":
                return GameMode.NORMAL;
            case "아이템 모드":
                return GameMode.ITEM;
            case "타임어택 모드":
                return GameMode.TIME_ATTACK;
            default:
                return GameMode.NORMAL;
        }
    }
}
