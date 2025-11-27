package org.tetris.network.dto;

import java.io.Serializable;

public class MatchSettings implements Serializable {
    private static final long serialVersionUID = 1L;

    private final long mySeed;
    private final long otherSeed;
    // 추후 게임 모드 등 추가 가능

    public MatchSettings(long mySeed, long otherSeed) {
        this.mySeed = mySeed;
        this.otherSeed = otherSeed;
    }

    public long getMySeed() {
        return mySeed;
    }

    public long getOtherSeed() {
        return otherSeed;
    }
}
