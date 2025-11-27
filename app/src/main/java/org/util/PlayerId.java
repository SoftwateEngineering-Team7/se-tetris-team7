package org.util;

public enum PlayerId {
    PLAYER1(1),
    PLAYER2(2);

    private final int value;

    PlayerId(int value) { // enum의 private 생성자
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}