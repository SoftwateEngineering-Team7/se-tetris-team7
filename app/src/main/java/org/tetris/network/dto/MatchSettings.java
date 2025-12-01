package org.tetris.network.dto;

import java.io.Serializable;

import org.tetris.game.model.GameMode;

public class MatchSettings implements Serializable {
    private static final long serialVersionUID = 3L;

    private final int playerNumber; // 1 or 2 - 이 클라이언트가 Player 1인지 Player 2인지
    private final long mySeed;
    private final long otherSeed;
    private final GameMode gameMode;
    private final String difficulty;

    public MatchSettings(int playerNumber, long mySeed, long otherSeed, GameMode gameMode, String difficulty) {
        this.playerNumber = playerNumber;
        this.mySeed = mySeed;
        this.otherSeed = otherSeed;
        this.gameMode = gameMode;
        this.difficulty = difficulty;
    }

    public int getPlayerNumber() {
        return playerNumber;
    }

    public long getMySeed() {
        return mySeed;
    }

    public long getOtherSeed() {
        return otherSeed;
    }

    public GameMode getGameMode() {
        return gameMode;
    }

    public String getDifficulty() {
        return difficulty;
    }
}
