package org.util;

public class Difficulty {
    public static final String EASY_STRING = "EASY";
    public static final String NORMAL_STRING = "NORMAL";
    public static final String HARD_STRING = "HARD";


    private static String currentDifficulty = EASY_STRING; // 기본값
    
    private final String name;
    private final float speedMultiplier;

    private Difficulty(String name, float speedMultiplier) {
        this.name = name;
        this.speedMultiplier = speedMultiplier;
    }

    /**
     * 현재 설정된 난이도의 속도 배율 반환
     */
    public static float getSpeedMultiplier() {
        switch (currentDifficulty) {
            case NORMAL_STRING: return NORMAL.speedMultiplier;
            case HARD_STRING: return HARD.speedMultiplier;
            default: return EASY.speedMultiplier;
        }
    }

    /**
     * 난이도 설정
     * @param difficulty 난이도 이름 ("EASY", "NORMAL", "HARD")
     */
    public static void setCurrentDifficulty(String difficulty) {
        if (difficulty != null && (difficulty.equals(EASY_STRING) || difficulty.equals(NORMAL_STRING) || difficulty.equals(HARD_STRING))) {
            currentDifficulty = difficulty;
        }
    }

    /**
     * 현재 난이도 이름 반환
     */
    public static String getCurrentDifficulty() {
        return currentDifficulty;
    }

    public String getName() {
        return name;
    }

    // region 난이도 정의

    public static final Difficulty EASY = new Difficulty(EASY_STRING, 1.0f);
    public static final Difficulty NORMAL = new Difficulty(NORMAL_STRING, 1.15f);
    public static final Difficulty HARD = new Difficulty(HARD_STRING, 1.3f);

    // endregion
}
