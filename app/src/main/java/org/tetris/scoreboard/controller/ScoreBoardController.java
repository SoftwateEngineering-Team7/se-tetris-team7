package org.tetris.scoreboard.controller;

import org.tetris.scoreboard.model.ScoreBoard;
import org.tetris.scoreboard.model.ScoreInfo;

public class ScoreBoardController {
    private final ScoreBoard scoreBoard;
    public final int finishScore;

    public ScoreBoardController()
    {
        scoreBoard = new ScoreBoard();
        finishScore = 0;
    }

    public ScoreBoard getScoreBoard()
    {
        return scoreBoard;
    }

    public void OnSubmitClick(String name)
    {
        submitCurrentScore(finishScore, name);
    }

    private void submitCurrentScore(int score, String name)
    {
        scoreBoard.insert(new ScoreInfo(score, name));
        scoreBoard.writeHighScore();
    }


}