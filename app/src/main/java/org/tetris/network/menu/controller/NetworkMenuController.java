package org.tetris.network.menu.controller;

import org.tetris.network.menu.model.NetworkMenu;
import org.tetris.shared.BaseController;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;

public class NetworkMenuController extends BaseController<NetworkMenu> {

    public NetworkMenuController(NetworkMenu networkMenu) {
        super(networkMenu);
    }

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

    @Override
    public void initialize() {
        // ComboBox 초기화
        gameModeCombo.getItems().addAll("일반 모드", "아이템 모드", "타임어택 모드");
        gameModeCombo.setValue("일반 모드");

        // 초기값 설정
        hostRadio.setSelected(true);
        ipField.setText(model.getIpAddress());
        portField.setText(String.valueOf(model.getPort()));

        // RadioButton 변경 리스너
        connectionTypeGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle == hostRadio) {
                model.setIsHost(true);
                addLog("Host 모드로 설정됨");
            } else if (newToggle == clientRadio) {
                model.setIsHost(false);
                addLog("Client 모드로 설정됨");
            }
        });

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

            boolean isHost = hostRadio.isSelected();
            if (isHost) {
                addLog("서버 생성 중... IP: " + ip + ", Port: " + port);
                addLog("게임 모드: " + gameMode);
                // TODO: 서버 시작 로직 구현
            } else {
                addLog("서버에 연결 중... IP: " + ip + ", Port: " + port);
                // TODO: 클라이언트 연결 로직 구현
            }

        } catch (NumberFormatException e) {
            addLog("오류: 포트는 숫자여야 합니다");
        } catch (Exception e) {
            addLog("오류: " + e.getMessage());
        }
    }

    @FXML
    private void onBackPressed() {
        addLog("메인 메뉴로 돌아갑니다");
        // TODO: 라우터를 통해 메인 메뉴로 이동
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
     * Host/Client 상태를 반환합니다.
     * @return Host이면 true, Client이면 false
     */
    public boolean isHost() {
        return hostRadio.isSelected();
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
