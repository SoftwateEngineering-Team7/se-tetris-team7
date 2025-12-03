package org.tetris.network.menu.controller;

import org.tetris.Router;
import org.tetris.game.model.GameMode;
import org.tetris.menu.setting.model.Setting;
import org.tetris.network.GameClient;
import org.tetris.network.GameServer;
import org.tetris.network.comand.GameMenuCommandExecutor;
import org.tetris.network.comand.ReadyCommand;
import org.tetris.network.comand.RequestSyncCommand;
import org.tetris.network.dto.MatchSettings;
import org.tetris.network.menu.model.NetworkMenu;
import org.tetris.shared.BaseController;
import org.tetris.shared.RouterAware;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class NetworkMenuController extends BaseController<NetworkMenu>
        implements RouterAware, GameMenuCommandExecutor {

    private Router router;
    private Setting routerSetting;
    private final GameClient client;
    private boolean isHost;  // 기본값 제거 - Model에서 동기화
    private boolean shouldReleaseResources;

    @FXML
    private Label roleLabel;
    @FXML
    private Label ipHintLabel;
    @FXML
    private Label messageLabel;
    @FXML
    private Label pingLabel;
    @FXML
    private TextField ipField;
    @FXML
    private ComboBox<String> gameModeCombo;
    @FXML
    private Button joinButton;
    @FXML
    private Button startButton;
    @FXML
    private Button readyButton;
    @FXML
    private Button backButton;
    @FXML
    private Label selfStatusLabel;
    @FXML
    private Label opponentStatusLabel;
    @FXML
    private Label selfReadyBadge;
    @FXML
    private Label opponentReadyBadge;
    @FXML
    private VBox opponentCard;
    @FXML
    private VBox selfCard;

    public NetworkMenuController(NetworkMenu networkMenu) {
        super(networkMenu);
        this.client = GameClient.getInstance();
    }

    @Override
    public void setRouter(Router router) {
        this.router = router;
        routerSetting = router.getSetting();
    }

    @Override
    public void initialize() {
        setupGameModeCombo();
        setupModelListeners();
        resetUi();

        // isHost는 configureRole()에서 설정됨 - 여기서는 설정하지 않음

        if (client != null) {
            client.setMenuExecutor(this);
            // 연결 끊김 콜백 설정 - 상대방이 나갔을 때 감지
            client.setOnDisconnectCallback(() -> {
                Platform.runLater(() -> {
                    if (!shouldReleaseResources) {
                        // 내가 직접 끊은 게 아니면 상대방이 나간 것
                        handleOpponentDisconnected();
                    }
                });
            });
        }
    }

    public void configureRole(boolean isHost, boolean preserveConnection) {
        this.isHost = isHost;
        shouldReleaseResources = false;
        model.clear(preserveConnection);
        Platform.runLater(() -> {
            resetUi();
            model.setIsHost(isHost);
            model.resetReadyStates();

            roleLabel.setText(isHost ? "HOST MODE" : "CLIENT MODE");
            ipField.setEditable(!isHost);
            gameModeCombo.setDisable(!isHost);
            startButton.setManaged(isHost);
            startButton.setVisible(isHost);
            joinButton.setManaged(!isHost);
            joinButton.setVisible(!isHost);
            ipHintLabel.setText(isHost ? "내 IP를 공유하세요." : "호스트 IP를 입력해 접속하세요.");

            if (preserveConnection) {
                model.setConnected(true);
                joinButton.setDisable(true);
                readyButton.setDisable(false);
                readyButton.setText(model.isReady() ? "CANCEL" : "READY");
                ipField.setText(model.getIpAddress());
                setMessage("연결을 유지했습니다. READY를 다시 눌러주세요.", false);
                
                // 게임에서 메뉴로 돌아올 때 서버에 상대방 Ready 상태 동기화 요청
                if (client != null && client.isConnected()) {
                    client.sendCommand(new RequestSyncCommand());
                }
                return;
            }

            messageLabel.setText(isHost ? "호스트 세션을 준비 중..." : "접속할 호스트 IP를 입력하세요.");
            if (isHost) {
                autoCreateHost();
            } else {
                readyButton.setDisable(true);
                ipField.setText(routerSetting.getLastVisitedIP());
            }
        });
    }

    private void setupModelListeners() {
        model.pingProperty().addListener((obs, o, n) -> updatePingLabel(n.longValue()));
        model.otherIsReadyProperty().addListener((obs, o, n) -> {
            updateOpponentReady(n);
            updateStartButtonState();
        });
        model.connectedProperty().addListener((obs, o, n) -> updateConnectionState());
        model.opponentConnectedProperty().addListener((obs, o, n) -> {
            updateOpponentPresence(n);
            updateConnectionState();
            if (!n)
                handleOpponentDisconnected();
        });
    }

    private void setupGameModeCombo() {
        gameModeCombo.getItems().addAll("일반 모드", "아이템 모드", "타임어택 모드");
        gameModeCombo.setValue("일반 모드");
        gameModeCombo.setStyle(
                "-fx-background-color: #0b2451; -fx-text-fill: white; -fx-border-color: #274690; " +
                        "-fx-border-radius: 10; -fx-background-radius: 10; -fx-prompt-text-fill: white;");

        gameModeCombo.setCellFactory(listView -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item);
                setStyle("-fx-text-fill: white; -fx-background-color: #0b2451;");
            }
        });
        gameModeCombo.setButtonCell(new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item);
                setStyle("-fx-text-fill: white;");
            }
        });
    }

    private void autoCreateHost() {
        try {
            model.create();
            ipField.setText(model.getIpAddress());
            readyButton.setDisable(true);
            startButton.setDisable(true);
            selfStatusLabel.setText("서버 온라인");
            setMessage("서버를 열었습니다. 상대를 기다리는 중...", false);
            updateConnectionState();
        } catch (Exception e) {
            setMessage("호스트 생성 실패: " + e.getMessage(), true);
            readyButton.setDisable(true);
            startButton.setDisable(true);
        }
    }

    @FXML
    private void onJoinPressed() {
        String ip = ipField.getText().trim();
        if (ip.isEmpty()) {
            setMessage("IP를 입력해주세요.", true);
            return;
        }

        try {
            model.setIsHost(false);
            model.isValidIP(ip);
            model.setIpAddress(ip);
            model.join();

            readyButton.setDisable(false);
            joinButton.setDisable(true);
            ipField.setEditable(false);
            selfStatusLabel.setText("연결됨");
            opponentStatusLabel.setText("호스트 확인됨");
            setMessage("서버에 연결되었습니다.", false);
            updateConnectionState();
        } catch (Exception e) {
            setMessage("입장 실패: " + e.getMessage(), true);
            joinButton.setDisable(false);
            ipField.setEditable(true);
        }
    }

    @FXML
    private void onReadyPressed() {
        if (!model.isConnected()) {
            setMessage("연결 후 READY를 눌러주세요.", true);
            return;
        }

        model.setIsReady(!model.isReady());
        boolean ready = model.isReady();
        readyButton.setText(ready ? "CANCEL" : "READY");
        selfReadyBadge.setVisible(ready);
        setMessage(ready ? "준비 완료" : "준비 취소", false);
        updateStartButtonState();
        client.sendCommand(new ReadyCommand(ready));
    }

    @FXML
    private void onStartPressed() {
        if (!isHost)
            return;

        if (!model.isReady() || !model.getOtherIsReady()) {
            setMessage("양쪽 모두 READY 상태여야 시작할 수 있습니다.", true);
            return;
        }

        boolean started = GameServer.getInstance().startGameIfReady();
        setMessage(started ? "게임을 시작합니다..." : "두 플레이어가 모두 연결되어 있는지 확인해주세요.", !started);
    }

    @FXML
    private void onBackPressed() {
        shouldReleaseResources = true;
        // graceful disconnect로 상대방에게 연결 끊김 알림
        client.disconnectGracefully();
        if (router != null)
            router.showStartMenu();
    }

    @Override
    public void onReady(boolean others) {
        Platform.runLater(() -> {
            model.setOtherIsReady(others);
            updateOpponentReady(others);
            updateStartButtonState();
            setMessage(others ? "상대가 READY 상태입니다." : "상대가 READY를 해제했습니다.", false);
        });
    }

    @Override
    public void onPlayerConnectionChanged(boolean opponentConnected) {
        Platform.runLater(() -> model.setOpponentConnected(opponentConnected));
    }

    @Override
    public void gameStart(MatchSettings settings) {
        Platform.runLater(() -> {
            routerSetting.setLastVisitedIP(ipField.getText().trim());
            router.showP2PGamePlaceholder(mapGameModeLabelToGameMode(gameModeCombo.getValue()), settings);
        });
    }

    @Override
    public synchronized void updatePing(long ping) {
        if (model != null)
            model.setPing(ping);
    }

    @Override
    public void cleanup() {
        resetUi();
        if (shouldReleaseResources) {
            model.clear();
            shouldReleaseResources = false;
        }
    }

    private void updateOpponentReady(boolean ready) {
        opponentReadyBadge.setVisible(ready);
        opponentStatusLabel.setText(ready ? "READY" : (model.isOpponentConnected() ? "접속됨" : "대기 중"));
    }

    private void updateConnectionState() {
        boolean connected = model.isConnected();
        selfStatusLabel.setText(connected ? "연결됨" : "연결 대기");
        readyButton.setDisable(!connected || !model.isOpponentConnected());
        if (!connected) {
            readyButton.setText("READY");
            selfReadyBadge.setVisible(false);
        }
        updateStartButtonState();
    }

    private void updateOpponentPresence(boolean connected) {
        opponentCard.setOpacity(connected ? 1.0 : 0.6);
        opponentStatusLabel.setText(connected ? "접속됨" : "대기 중");
        if (!connected)
            opponentReadyBadge.setVisible(false);
    }

    private void updateStartButtonState() {
        boolean readyToStart = isHost && model.isConnected() &&
                model.isOpponentConnected() && model.isReady() && model.getOtherIsReady();
        startButton.setDisable(!readyToStart);
    }

    private void handleOpponentDisconnected() {
        model.setOtherIsReady(false);
        updateOpponentReady(false);
        opponentReadyBadge.setVisible(false);

        model.setIsReady(false);
        selfReadyBadge.setVisible(false);
        readyButton.setText("READY");
        readyButton.setDisable(true);

        if (isHost) {
            selfStatusLabel.setText("서버 온라인");
            opponentStatusLabel.setText("대기 중");
            setMessage("상대가 나갔습니다. 새로운 플레이어를 기다리는 중...", false);
        } else {
            model.setConnected(false);
            selfStatusLabel.setText("연결 대기");
            opponentStatusLabel.setText("대기 중");

            joinButton.setDisable(false);
            joinButton.setVisible(true);
            joinButton.setManaged(true);
            ipField.setEditable(true);
            setMessage("호스트와의 연결이 끊어졌습니다. 다시 접속해 주세요.", true);
        }
        updateStartButtonState();
    }

    private void updatePingLabel(long ping) {
        if (pingLabel != null)
            pingLabel.setText("Ping: " + ping + " ms");
    }

    private void resetUi() {
        if (messageLabel != null) {
            messageLabel.setText("역할을 선택하세요.");
            messageLabel.setStyle("-fx-text-fill: #e4e9ff;");
        }
        if (pingLabel != null)
            pingLabel.setText("Ping: -- ms");
        if (selfReadyBadge != null)
            selfReadyBadge.setVisible(false);
        if (opponentReadyBadge != null)
            opponentReadyBadge.setVisible(false);
        if (readyButton != null) {
            readyButton.setText("READY");
            readyButton.setDisable(true);
        }
        if (startButton != null)
            startButton.setDisable(true);
        if (joinButton != null) {
            joinButton.setDisable(false);
            joinButton.setVisible(true);
            joinButton.setManaged(true);
        }
        if (ipField != null) {
            ipField.clear();
            ipField.setEditable(true);
        }
        updateOpponentPresence(false);
        updateConnectionState();
    }

    private void setMessage(String text, boolean isError) {
        if (messageLabel != null) {
            messageLabel.setText(text);
            messageLabel.setStyle(isError ? "-fx-text-fill: #ff8080;" : "-fx-text-fill: #e4e9ff;");
        }
    }

    private GameMode mapGameModeLabelToGameMode(String label) {
        return switch (label) {
            case "아이템 모드" -> GameMode.ITEM;
            case "타임어택 모드" -> GameMode.TIME_ATTACK;
            default -> GameMode.NORMAL;
        };
    }
}
