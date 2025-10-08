package org.tetris.scoreboard.model;

public record ScoreInfo(int score, String name)
{
    public ScoreInfo setScore(int score)
    {
        return new ScoreInfo(score, name());
    }

    public ScoreInfo setName(String name)
    {
        return new ScoreInfo(score(), name);
    }
}