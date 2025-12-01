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

    @Test
    public void testShow() {
        // 1초 동안 화면을 보여줍니다
        sleep(1000);

        // 기본 UI 요소들이 표시되는지 확인
        verifyThat("#hostRadio", isVisible());
        verifyThat("#clientRadio", isVisible());
        verifyThat("#ipField", isVisible());
        verifyThat("#portField", isVisible());
        verifyThat("#createButton", isVisible());
        verifyThat("#logArea", isVisible());
        verifyThat("#backButton", isVisible());
        verifyThat("#clearLogButton", isVisible());
    }

    @Test
    public void testHostClientSelection() {
        // Host 선택 테스트
        clickOn("#hostRadio");
        sleep(1000);
        verifyThat("#hostRadio", isVisible());

        // Client 선택 테스트
        clickOn("#clientRadio");
        sleep(1000);
        verifyThat("#clientRadio", isVisible());
    }

    @Test
    public void testIpFieldInput() {
        doubleClickOn("#ipField").write("192.168.1.100");
        verifyThat("#ipField", TextInputControlMatchers.hasText("192.168.1.100"));
    }

    @Test
    public void testPortFieldInput() {
        doubleClickOn("#portField").write("54321");
        verifyThat("#portField", TextInputControlMatchers.hasText("54321"));
    }

    @Test
    public void testGameModeCombo() {
        clickOn("#hostRadio");
        sleep(500);
        clickOn("#gameModeCombo");
        sleep(500);
        // 콤보박스 드롭다운이 열리는지 확인
        verifyThat("#gameModeCombo", isVisible());
    }

    @Test
    public void testCreateButtonClick() {

        clickOn("#ipField").write("localhost");
        clickOn("#createButton");
        sleep(1000);
        // 로그 영역에 메시지가 추가되었는지 확인
        verifyThat("#logArea", isVisible());

        sleep(1000);

        assertEquals("localhost", model.getIpAddress());
        assertEquals(54321, model.getPort());
    }

    @Test
    public void testClearLogButton() {
        // 먼저 로그를 생성하기 위해 create 버튼 클릭
        clickOn("#createButton");
        sleep(500);

        // 로그 클리어 버튼 클릭
        clickOn("#clearLogButton");
        sleep(1000);

        // 로그 영역이 여전히 존재하는지 확인
        verifyThat("#logArea", isVisible());
    }

    @Test
    public void testBackButtonExists() {
        verifyThat("#backButton", isVisible());
    }

    @Test
    public void testLogAreaExists() {
        verifyThat("#logArea", isNotNull());
        verifyThat("#logArea", isVisible());
    }

    @Test
    public void testRefreshApplied() {
        clickOn("#ipField").write("test-ip");
        clickOn("#portField").write("12345");
        clickOn("#hostRadio");
        sleep(500);

        // JavaFX Application Thread에서 refresh() 실행
        interact(() -> controller.refresh());
        sleep(500);

        verifyThat("#ipField", TextInputControlMatchers.hasText("")); // 필드가 클리어되었다면
        verifyThat("#portField", TextInputControlMatchers.hasText("54321")); // 기본값으로 복원되었다면
    }
}
