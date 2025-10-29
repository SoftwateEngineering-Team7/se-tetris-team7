package org.util;

public class ScreenPreset {
    public static final String SMALL_STRING = "SMALL";
    public static final String MIDDLE_STRING = "MIDDLE";
    public static final String LARGE_STRING = "LARGE";

    public static final String SMALL_SIZE = "800x800";
    public static final String MIDDLE_SIZE = "1000x810";
    public static final String LARGE_SIZE = "1200x820";

    public static final int SMALL_WIDTH = 800;
    public static final int SMALL_HEIGHT = 800;
    public static final int MIDDLE_WIDTH = 1000;
    public static final int MIDDLE_HEIGHT = 810;
    public static final int LARGE_WIDTH = 1200;
    public static final int LARGE_HEIGHT = 820;


    private static String currentPreset = SMALL_STRING;
    
    private final String name;
    private final int width;
    private final int height;

    private ScreenPreset(String name, int width, int height) {
        this.name = name;
        this.width = width;
        this.height = height;
    }

    /**
     * 현재 설정된 화면 크기 반환
     */
    public static int getWidth() {
        switch (currentPreset) {
            case MIDDLE_STRING: return MIDDLE.width;
            case LARGE_STRING: return LARGE.width;
            default: return SMALL.width;
        }
    }

    public static int getHeight() {
        switch (currentPreset) {
            case MIDDLE_STRING: return MIDDLE.height;
            case LARGE_STRING: return LARGE.height;
            default: return SMALL.height;
        }
    }

    /**
     * 화면 크기 설정
     * @param preset 화면 크기 이름 ("SMALL", "MIDDLE", "LARGE")
     */
    public static void setCurrentPreset(String preset) {
        if (preset != null && (preset.equals(SMALL_STRING) || preset.equals(MIDDLE_STRING) || preset.equals(LARGE_STRING))) {
            currentPreset = preset;
        }
    }

    /**
     * 현재 프리셋 이름 반환
     */
    public static String getCurrentPreset() {
        return currentPreset;
    }

    public String getName() {
        return name;
    }

    // region 화면 크기 정의

    public static final ScreenPreset SMALL = new ScreenPreset(ScreenPreset.SMALL_STRING, ScreenPreset.SMALL_WIDTH, ScreenPreset.SMALL_HEIGHT);
    public static final ScreenPreset MIDDLE = new ScreenPreset(ScreenPreset.MIDDLE_STRING, ScreenPreset.MIDDLE_WIDTH, ScreenPreset.MIDDLE_HEIGHT);
    public static final ScreenPreset LARGE = new ScreenPreset(ScreenPreset.LARGE_STRING, ScreenPreset.LARGE_WIDTH, ScreenPreset.LARGE_HEIGHT);

    // endregion
}
