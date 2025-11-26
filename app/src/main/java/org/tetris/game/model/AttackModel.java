package org.tetris.game.model;

import java.util.ArrayList;

public class AttackModel {
    private static final int MAX_ATTACKS = 10;

    private ArrayList<int[]> attacks;

    public AttackModel() {
        attacks = new ArrayList<>();
    }

    public ArrayList<int[]> getAttacks() {
        return attacks;
    }

    /**
     * 큐에 공격을 추가합니다. 만약 큐의 크기가 최대치를 초과하면 추가하지 않습니다.
     * @param attack 공격을 나타내는 정수 배열
     */
    public void push(int[] attack) {
        if (attacks.size() >= MAX_ATTACKS) {
            return;
        }

        attacks.add(attack);
    }

    /**
     * 큐에서 가장 오래된 공격을 제거하고 반환합니다.
     * @return 공격을 나타내는 정수 배열, 큐가 비어있으면 null을 반환합니다.
     */
    public int[] pop() {
        if (attacks.size() == 0) {
            return null;
        }
        return attacks.remove(0);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int[] attack : attacks) {
            for (int i = 0; i < attack.length; i++) {
                sb.append(attack[i]);
                if (i < attack.length - 1) {
                    sb.append(" ");
                }
            }
            sb.append(" \n");
        }
        return sb.toString();
    }

    public void reset(){
        attacks.clear();
    }
}
