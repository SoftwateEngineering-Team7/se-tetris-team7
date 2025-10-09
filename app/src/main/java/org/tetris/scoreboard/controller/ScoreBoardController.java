package org.tetris.scoreboard.controller;

import org.tetris.scoreboard.model.ScoreBoard;
import org.tetris.scoreboard.model.ScoreInfo;
public class ScoreBoardController extends BaseController<ScoreBoard> {

    private int finishScore;

    public ScoreBoardController(ScoreBoard scoreBoard)
    {
        super( scoreBoard );
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