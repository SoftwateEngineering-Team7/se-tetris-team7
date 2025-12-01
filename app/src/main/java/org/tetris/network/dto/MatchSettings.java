package org.tetris.network.dto;

import java.io.Serializable;

public class MatchSettings implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int playerNumber; // 1 or 2 - 이 클라이언트가 Player 1인지 Player 2인지
    private final long mySeed;
    private final long otherSeed;
    // 추후 게임 모드 등 추가 가능

    public MatchSettings(int playerNumber, long mySeed, long otherSeed) {
        this.playerNumber = playerNumber;
        this.mySeed = mySeed;
        this.otherSeed = otherSeed;
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
}
