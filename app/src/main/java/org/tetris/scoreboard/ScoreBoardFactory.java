package org.tetris.scoreboard;

import org.tetris.scoreboard.controller.ScoreBoardController;
import org.tetris.scoreboard.model.ScoreBoard;
import org.tetris.shared.MvcFactory;;

public class ScoreBoardFactory extends MvcFactory<ScoreBoard, ScoreBoardController> {
    public ScoreBoardFactory() {
        super( () -> new ScoreBoard(), model -> new ScoreBoardController(model), "view/scoreboard.fxml");
    }
}
