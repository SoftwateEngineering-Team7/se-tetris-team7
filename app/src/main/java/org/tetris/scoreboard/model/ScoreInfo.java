package org.tetris.scoreboard.model;

public record ScoreInfo(int score, String name, String difficulty)
{
    public ScoreInfo setScore(int score)
    {
        return new ScoreInfo(score, name(), difficulty());
    }

    public ScoreInfo setName(String name)
    {
        return new ScoreInfo(score(), name, difficulty());
    }

    public ScoreInfo setDifficulty(String difficulty)
    {
        return new ScoreInfo(score(), name(), difficulty);
    }
}