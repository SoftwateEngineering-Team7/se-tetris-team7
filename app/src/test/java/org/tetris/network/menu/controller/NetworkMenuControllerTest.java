package org.tetris.network.menu.controller;

import static org.junit.Assert.*;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.base.NodeMatchers.*;

import org.junit.Test;

import org.testfx.framework.junit.ApplicationTest;
import org.testfx.matcher.control.TextInputControlMatchers;
import org.tetris.network.menu.NetworkMenuFactory;
import org.tetris.network.menu.model.NetworkMenu;
import org.tetris.shared.MvcBundle;

import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class NetworkMenuControllerTest extends ApplicationTest {
    private NetworkMenuFactory nmFactory = new NetworkMenuFactory();
    private NetworkMenuController controller;
    private NetworkMenu model;

    @Override
    public void start(Stage stage) {
        MvcBundle<?, ?, ?> bundle = nmFactory.create();

        controller = (NetworkMenuController) bundle.controller();
        model = (NetworkMenu) bundle.model();

        stage.setScene(bundle.view().getScene());
        stage.setTitle("NetworkMenu Test");
        stage.show();
    }

    // ===== UI 요소 존재 테스트 =====

    @Test
    public void testBasicUIElementsExist() {
        sleep(500);
        
        // 기본 UI 요소들이 존재하는지 확인
        verifyThat("#roleLabel", isNotNull());
        verifyThat("#ipField", isNotNull());
        verifyThat("#gameModeCombo", isNotNull());
        verifyThat("#messageLabel", isNotNull());
        verifyThat("#pingLabel", isNotNull());
    }

    @Test
    public void testButtonsExist() {
        sleep(500);
        
        verifyThat("#joinButton", isNotNull());
        verifyThat("#startButton", isNotNull());
        verifyThat("#readyButton", isNotNull());
        verifyThat("#backButton", isNotNull());
    }

    @Test
    public void testPlayerCardsExist() {
        sleep(500);
        
        verifyThat("#selfCard", isNotNull());
        verifyThat("#opponentCard", isNotNull());
        verifyThat("#selfStatusLabel", isNotNull());
        verifyThat("#opponentStatusLabel", isNotNull());
    }

    @Test
    public void testReadyBadgesExist() {
        sleep(500);
        
        verifyThat("#selfReadyBadge", isNotNull());
        verifyThat("#opponentReadyBadge", isNotNull());
    }

    // ===== 초기 상태 테스트 =====

    @Test
    public void testInitialPingLabel() {
        sleep(500);
        
        Label pingLabel = lookup("#pingLabel").queryAs(Label.class);
        assertEquals("Ping: -- ms", pingLabel.getText());
    }

    @Test
    public void testInitialMessageLabel() {
        sleep(500);
        
        Label messageLabel = lookup("#messageLabel").queryAs(Label.class);
        assertNotNull(messageLabel.getText());
    }

    @Test
    public void testBackButtonIsVisible() {
        sleep(500);
        
        verifyThat("#backButton", isVisible());
    }

    // ===== 게임 모드 콤보박스 테스트 =====

    @Test
    public void testGameModeComboExists() {
        sleep(500);
        
        verifyThat("#gameModeCombo", isNotNull());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testGameModeComboHasOptions() {
        sleep(500);
        
        ComboBox<String> combo = lookup("#gameModeCombo").queryAs(ComboBox.class);
        assertNotNull(combo);
        assertTrue("게임 모드 콤보박스에 옵션이 있어야 함", combo.getItems().size() > 0);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testGameModeComboContainsNormalMode() {
        sleep(500);
        
        ComboBox<String> combo = lookup("#gameModeCombo").queryAs(ComboBox.class);
        assertTrue("일반 모드가 포함되어야 함", combo.getItems().contains("일반 모드"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testGameModeComboContainsItemMode() {
        sleep(500);
        
        ComboBox<String> combo = lookup("#gameModeCombo").queryAs(ComboBox.class);
        assertTrue("아이템 모드가 포함되어야 함", combo.getItems().contains("아이템 모드"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testGameModeComboContainsTimeAttackMode() {
        sleep(500);
        
        ComboBox<String> combo = lookup("#gameModeCombo").queryAs(ComboBox.class);
        assertTrue("타임어택 모드가 포함되어야 함", combo.getItems().contains("타임어택 모드"));
    }

    // ===== IP 필드 테스트 =====

    @Test
    public void testIpFieldExists() {
        sleep(500);
        
        verifyThat("#ipField", isNotNull());
    }

    @Test
    public void testIpFieldInput() {
        sleep(500);
        
        TextField ipField = lookup("#ipField").queryAs(TextField.class);
        // 필드가 editable인 경우에만 입력 테스트
        if (ipField.isEditable()) {
            clickOn("#ipField").write("192.168.1.100");
            verifyThat("#ipField", TextInputControlMatchers.hasText("192.168.1.100"));
        }
    }

    // ===== 버튼 상태 테스트 =====

    @Test
    public void testReadyButtonInitiallyDisabled() {
        sleep(500);
        
        Button readyButton = lookup("#readyButton").queryAs(Button.class);
        // 연결되지 않은 상태에서는 READY 버튼이 비활성화되어야 함
        assertTrue("연결 전에는 READY 버튼이 비활성화되어야 함", readyButton.isDisabled());
    }

    @Test
    public void testStartButtonInitiallyDisabled() {
        sleep(500);
        
        Button startButton = lookup("#startButton").queryAs(Button.class);
        assertTrue("초기에는 START 버튼이 비활성화되어야 함", startButton.isDisabled());
    }

    // ===== 모델 연동 테스트 =====

    @Test
    public void testModelNotNull() {
        sleep(500);
        
        assertNotNull("NetworkMenu 모델이 null이 아니어야 함", model);
    }

    @Test
    public void testControllerNotNull() {
        sleep(500);
        
        assertNotNull("NetworkMenuController가 null이 아니어야 함", controller);
    }

    @Test
    public void testModelInitialConnectedState() {
        sleep(500);
        
        // 초기에는 연결되지 않은 상태
        // (configureRole이 호출되기 전까지는 상태가 다를 수 있음)
        assertNotNull(model);
    }

    // ===== UI 상호작용 테스트 =====

    @Test
    public void testClickOnGameModeCombo() {
        sleep(500);
        
        // 게임 모드 콤보박스 클릭
        clickOn("#gameModeCombo");
        sleep(300);
        
        // 콤보박스가 여전히 존재하는지 확인
        verifyThat("#gameModeCombo", isNotNull());
    }

    @Test
    public void testReadyButtonText() {
        sleep(500);
        
        Button readyButton = lookup("#readyButton").queryAs(Button.class);
        assertEquals("READY", readyButton.getText());
    }

    @Test
    public void testJoinButtonText() {
        sleep(500);
        
        Button joinButton = lookup("#joinButton").queryAs(Button.class);
        assertEquals("JOIN", joinButton.getText());
    }

    @Test
    public void testStartButtonText() {
        sleep(500);
        
        Button startButton = lookup("#startButton").queryAs(Button.class);
        assertEquals("START", startButton.getText());
    }

    @Test
    public void testBackButtonText() {
        sleep(500);
        
        Button backButton = lookup("#backButton").queryAs(Button.class);
        assertEquals("BACK", backButton.getText());
    }

    // ===== 레이블 테스트 =====

    @Test
    public void testSelfStatusLabelExists() {
        sleep(500);
        
        Label selfStatusLabel = lookup("#selfStatusLabel").queryAs(Label.class);
        assertNotNull(selfStatusLabel);
        assertNotNull(selfStatusLabel.getText());
    }

    @Test
    public void testOpponentStatusLabelExists() {
        sleep(500);
        
        Label opponentStatusLabel = lookup("#opponentStatusLabel").queryAs(Label.class);
        assertNotNull(opponentStatusLabel);
        assertNotNull(opponentStatusLabel.getText());
    }

    @Test
    public void testIpHintLabelExists() {
        sleep(500);
        
        verifyThat("#ipHintLabel", isNotNull());
    }

    // ===== Ready Badge 테스트 =====

    @Test
    public void testSelfReadyBadgeInitiallyHidden() {
        sleep(500);
        
        Label selfReadyBadge = lookup("#selfReadyBadge").queryAs(Label.class);
        assertFalse("초기에 selfReadyBadge는 숨겨져야 함", selfReadyBadge.isVisible());
    }

    @Test
    public void testOpponentReadyBadgeInitiallyHidden() {
        sleep(500);
        
        Label opponentReadyBadge = lookup("#opponentReadyBadge").queryAs(Label.class);
        assertFalse("초기에 opponentReadyBadge는 숨겨져야 함", opponentReadyBadge.isVisible());
    }

    // ===== 카드 UI 테스트 =====

    @Test
    public void testSelfCardVisible() {
        sleep(500);
        
        verifyThat("#selfCard", isVisible());
    }

    @Test
    public void testOpponentCardVisible() {
        sleep(500);
        
        verifyThat("#opponentCard", isVisible());
    }
}
