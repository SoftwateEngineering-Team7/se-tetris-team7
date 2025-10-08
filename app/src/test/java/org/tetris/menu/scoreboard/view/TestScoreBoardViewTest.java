package org.tetris.menu.scoreboard.view;

import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.base.NodeMatchers.*;
import static org.testfx.matcher.control.TextInputControlMatchers.hasText;

import org.junit.Test;

import org.testfx.framework.junit.ApplicationTest;
import org.tetris.scoreboard.view.ScoreBoardView;

import javafx.stage.Stage;

public class TestScoreBoardViewTest extends ApplicationTest {

    @Override
    public void start(Stage stage) {
        ScoreBoardView scoreBoardView = new ScoreBoardView();
        stage.setScene(scoreBoardView.getScene());
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