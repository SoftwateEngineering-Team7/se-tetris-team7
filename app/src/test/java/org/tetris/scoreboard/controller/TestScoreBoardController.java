package org.tetris.scoreboard.controller;

import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.base.NodeMatchers.*;
import static org.testfx.matcher.control.TextInputControlMatchers.hasText;

import org.junit.Test;

import org.testfx.framework.junit.ApplicationTest;
import org.tetris.scoreboard.ScoreBoardFactory;
import org.tetris.scoreboard.model.ScoreBoard;
import org.tetris.shared.MvcBundle;

import javafx.fxml.FXMLLoader;
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
    public void testNameFieldInput() {
        clickOn("#nameField").write("TestPlayer");
        verifyThat("#nameField", hasText("TestPlayer"));
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
