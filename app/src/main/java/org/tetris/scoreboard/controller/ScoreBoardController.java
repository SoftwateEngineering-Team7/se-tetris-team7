package org.tetris.scoreboard.controller;

import org.tetris.scoreboard.model.ScoreBoard;
import org.tetris.scoreboard.model.ScoreInfo;

public class ScoreBoardController {
    private final ScoreBoard scoreBoard;
    private int finishScore;

    /**
     * ScoreBoardController 객체를 생성합니다.
     * ScoreBoard 모델을 초기화합니다.
     */
    public ScoreBoardController(int finishScore)
    {
        scoreBoard = new ScoreBoard();
        this.finishScore = finishScore;
    }

    public int getFinishScore(){
        return finishScore;
    }

    public ScoreBoard getScoreBoard(){
        return scoreBoard;
    }

    /**
     * 제출 버튼 클릭 시 호출되는 메서드입니다.
     * @param name 플레이어 이름
     */
    public void OnSubmitClick(String name)
    {
        submitCurrentScore(finishScore, name);
    }

    private void submitCurrentScore(int score, String name)
    {
        scoreBoard.insert(new ScoreInfo(score, name));
        scoreBoard.writeHighScoreList();
    }
}