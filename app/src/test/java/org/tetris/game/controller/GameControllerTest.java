package org.tetris.game.controller;

import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.base.NodeMatchers.*;

import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;
import org.tetris.game.GameFactory;
import org.tetris.shared.MvcBundle;

import javafx.stage.Stage;

public class GameControllerTest extends ApplicationTest {

    private GameFactory gameFactory = new GameFactory();

    @Override
    public void start(Stage stage) throws Exception {
        MvcBundle bundle = gameFactory.create();
        stage.setScene(bundle.view().getScene());
        stage.setTitle("Game Test");
        stage.show();
    }

    @Test
    public void testGameSceneDisplay(){
        // 게임 보드가 존재하는지 확인
        verifyThat("#gameBoard", isNotNull());
        
        // 점수 라벨이 존재하는지 확인
        verifyThat("#scoreLabel", isVisible());
        
        // 레벨 라벨이 존재하는지 확인
        verifyThat("#levelLabel", isVisible());
        
        // 라인 라벨이 존재하는지 확인
        verifyThat("#linesLabel", isVisible());
        
        // 일시정지 버튼이 존재하는지 확인
        verifyThat("#pauseButton", isVisible());
        
    }

    @Test
    public void testKeyboardInput() throws InterruptedException {
        // 게임 보드가 로드될 때까지 잠시 대기
        Thread.sleep(1000);
        
        // 키보드 입력 테스트 (왼쪽 화살표)
        press(javafx.scene.input.KeyCode.LEFT);
        Thread.sleep(500);
        
        // 키보드 입력 테스트 (오른쪽 화살표)
        press(javafx.scene.input.KeyCode.RIGHT);
        Thread.sleep(500);
        
        // 키보드 입력 테스트 (아래쪽 화살표)
        press(javafx.scene.input.KeyCode.DOWN);
        Thread.sleep(500);
        
        // 키보드 입력 테스트 (회전)
        press(javafx.scene.input.KeyCode.UP);
        Thread.sleep(500);
        
        // 추가로 7초간 더 표시 (총 10초)
        Thread.sleep(7000);
    }

    @Test
    public void testPauseButton() {
        // 일시정지 버튼 클릭 테스트
        clickOn("#pauseButton");
        // 다시 클릭 (재개)
        clickOn("#pauseButton");
    }

    @Test
    public void testNextBlockPreviewExists() throws InterruptedException {
        Thread.sleep(1000);
        
        // Next Block 미리보기 영역이 존재하는지 확인
        verifyThat("#nextBlockPane", isNotNull());
        verifyThat("#nextBlockPane", isVisible());
    }

    @Test
    public void testNextBlockPreviewUpdates() throws InterruptedException {
        Thread.sleep(1000);
        
        // Next Block 미리보기가 표시되어야 함
        verifyThat("#nextBlockPane", isVisible());
        
        // 하드 드롭으로 블록 변경
        press(javafx.scene.input.KeyCode.SPACE);
        Thread.sleep(500);
        
        // Next Block이 여전히 표시되어야 함
        verifyThat("#nextBlockPane", isVisible());
    }

    @Test
    public void testPauseOverlayInitiallyHidden() throws InterruptedException {
        Thread.sleep(1000);
        
        // 게임 시작 시 일시정지 오버레이가 숨겨져 있어야 함
        verifyThat("#pauseOverlay", isInvisible());
    }

    @Test
    public void testPauseOverlayShownOnPause() throws InterruptedException {
        Thread.sleep(1000);
        
        // 일시정지 버튼 클릭
        clickOn("#pauseButton");
        Thread.sleep(500);
        
        // 일시정지 오버레이가 표시되어야 함
        verifyThat("#pauseOverlay", isVisible());
        
        // RESUME 버튼이 표시되어야 함
        verifyThat("#resumeButton", isVisible());
        
        // MAIN MENU 버튼이 표시되어야 함
        verifyThat("#pauseMenuButton", isVisible());
    }

    @Test
    public void testResumeButtonWorks() throws InterruptedException {
        Thread.sleep(1000);
        
        // 일시정지
        clickOn("#pauseButton");
        Thread.sleep(500);
        
        // 일시정지 오버레이 확인
        verifyThat("#pauseOverlay", isVisible());
        
        // RESUME 버튼 클릭
        clickOn("#resumeButton");
        Thread.sleep(500);
        
        // 일시정지 오버레이가 숨겨져야 함
        verifyThat("#pauseOverlay", isInvisible());
    }

}
