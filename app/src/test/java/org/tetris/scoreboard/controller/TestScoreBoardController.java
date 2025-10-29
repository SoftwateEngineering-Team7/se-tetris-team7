package org.tetris.scoreboard.controller;

import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.base.NodeMatchers.*;


import org.junit.Test;

import org.testfx.framework.junit.ApplicationTest;
import org.testfx.matcher.control.TextInputControlMatchers;
import org.tetris.scoreboard.ScoreBoardFactory;
import org.tetris.shared.MvcBundle;

import javafx.scene.text.Text;
import javafx.stage.Stage;

public class TestScoreBoardController extends ApplicationTest {

    private ScoreBoardFactory sbFactory = new ScoreBoardFactory();
    @Override
    public void start(Stage stage) {
        MvcBundle bundle = sbFactory.create();
        
        stage.setScene(bundle.view().getScene());
        stage.setTitle("ScoreBoard Test");
        stage.show();
    }

    @Test
    public void testSetFromGame(){
        MvcBundle bundle = sbFactory.create();
        var controller = bundle.controller();
        if(controller instanceof ScoreBoardController sbc){
            
            sbc.setFromGame(true, 1500);
            sleep(1000);
            verifyThat("#inputPane", isVisible());
            verifyThat("#scoreText", (Text text) -> "1500".equals(text.getText()));

            sbc.setFromGame(false, 0);
            sleep(1000);
            verifyThat("#inputPane", isInvisible());
        }
    }

    @Test
    public void testSetItemMode(){
        MvcBundle bundle = sbFactory.create();
        var controller = bundle.controller();
        if(controller instanceof ScoreBoardController sbc){
            
            sbc.setItemMode(true);
            sleep(1000);
            // 아이템 모드용 최고점수 파일이 로드되었는지 확인하는 로직 추가 필요

            sbc.setItemMode(false);
            sleep(1000);
            // 기본 모드용 최고점수 파일이 로드되었는지 확인하는 로직 추가 필요
        }
    }

    @Test
    public void testNameFieldInput() {
        clickOn("#nameField").write("TestPlayer");
        verifyThat("#nameField", TextInputControlMatchers.hasText("TestPlayer"));
    }

    @Test
    public void testScoreTableExists() {
        verifyThat("#scoreTable", isNotNull());
    }

    @Test
    public void testSubmitButtonExists() {
        verifyThat("#submitButton", isVisible());
    }

    @Test
    public void testSubmitButtonClick() {
        clickOn("#nameField").write("Player1");
        clickOn("#submitButton");
    }
}
