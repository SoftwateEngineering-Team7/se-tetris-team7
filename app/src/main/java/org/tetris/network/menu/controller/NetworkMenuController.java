package org.tetris.network.menu.controller;

import org.tetris.Router;
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

public class NetworkMenuController extends BaseController<NetworkMenu> implements RouterAware{

    private Router router;

    @FXML private ToggleGroup connectionTypeGroup;
    @FXML private RadioButton hostRadio;
    @FXML private RadioButton clientRadio;
    @FXML private TextField ipField;
    @FXML private TextField portField;
    @FXML private ComboBox<String> gameModeCombo;
    @FXML private Button createButton;
    @FXML private TextArea logArea;
    @FXML private Button backButton;
    @FXML private Button clearLogButton;

    public NetworkMenuController(NetworkMenu networkMenu) {
        super(networkMenu);
    }

    @Override
    public void setRouter(Router router) {
        this.router = router;
    }

    @Override
    public void initialize() {
        // ComboBox 초기화
        gameModeCombo.getItems().addAll("일반 모드", "아이템 모드", "타임어택 모드");
        gameModeCombo.setValue("일반 모드");
        
        // ComboBox 스타일 강제 적용 (흰색 텍스트)
        gameModeCombo.setStyle("-fx-background-color: #0f3460; -fx-text-fill: white; -fx-border-color: #533483; -fx-border-radius: 5; -fx-background-radius: 5;");
        
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

        // 초기값 설정
        hostRadio.setSelected(true);
        ipField.setText(model.getIpAddress());
        portField.setText(String.valueOf(model.getPort()));

        // RadioButton 변경 리스너
        connectionTypeGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle == hostRadio) {
                model.setIsHost(true);
                ipField.setDisable(true);
                createButton.setText("Create");
                gameModeCombo.setVisible(true);

                addLog("Host 모드로 설정됨");
            } else if (newToggle == clientRadio) {
                model.setIsHost(false);
                ipField.setDisable(false);
                createButton.setText("Join");
                gameModeCombo.setVisible(false);

                addLog("Client 모드로 설정됨");
            }
        });

        connectionTypeGroup.selectToggle(clientRadio);
        // 초기 로그 메시지
        addLog("네트워크 게임 초기화 완료");
    }

    @FXML
    private void onCreatePressed() {
        try {
            String ip = ipField.getText().trim();
            String portText = portField.getText().trim();
            String gameMode = gameModeCombo.getValue();

            if (ip.isEmpty()) {
                addLog("오류: IP 주소를 입력해주세요");
                return;
            }

            if (portText.isEmpty()) {
                addLog("오류: 포트를 입력해주세요");
                return;
            }

            int port = Integer.parseInt(portText);
            if (port < 1 || port > 65535) {
                addLog("오류: 포트는 1-65535 범위여야 합니다");
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
            } else {
                addLog("서버에 연결 중... IP: " + ip + ", Port: " + port);
                model.join();
            }

            hostRadio.setDisable(true);
            clientRadio.setDisable(true);

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

    /**
     * 로그 메시지를 추가합니다.
     * @param message 추가할 로그 메시지
     */
    public void addLog(String message) {
        String timestamp = java.time.LocalTime.now().format(
            java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")
        );
        logArea.appendText("[" + timestamp + "] " + message + "\n");
        
        // 스크롤을 맨 아래로 이동
        logArea.setScrollTop(Double.MAX_VALUE);
    }

    /**
     * 현재 선택된 게임 모드를 반환합니다.
     * @return 선택된 게임 모드
     */
    public String getSelectedGameMode() {
        return gameModeCombo.getValue();
    }

    /**
     * 연결 상태를 업데이트합니다.
     * @param connected 연결 상태
     * @param message 상태 메시지
     */
    public void updateConnectionStatus(boolean connected, String message) {
        if (connected) {
            addLog("✓ 연결 성공: " + message);
            createButton.setText("DISCONNECT");
            createButton.setStyle("-fx-background-color: #e94560; -fx-text-fill: white; -fx-background-radius: 5;");
        } else {
            addLog("✗ 연결 실패: " + message);
            createButton.setText("CREATE");
            createButton.setStyle("-fx-background-color: #533483; -fx-text-fill: white; -fx-background-radius: 5;");
        }
    }
}
