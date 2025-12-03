package org.tetris;

/**
 * JavaFX 애플리케이션을 Fat JAR에서 실행하기 위한 런처 클래스.
 * JavaFX 11+에서 모듈 시스템 체크를 우회합니다.
 */
public class Launcher {
    public static void main(String[] args) {
        App.main(args);
    }
}
