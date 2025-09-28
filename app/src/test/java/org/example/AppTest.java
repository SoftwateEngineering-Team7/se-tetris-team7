package org.example;

import org.junit.Test;
import org.junit.BeforeClass;
import static org.junit.Assert.*;

/**
 * JavaFX App 클래스에 대한 단위 테스트
 * JavaFX 애플리케이션의 경우 UI 테스트는 별도의 통합 테스트로 분리하는 것이 일반적
 */
public class AppTest {

    @BeforeClass
    public static void setUpClass() {
        // JavaFX 툴킷을 헤드리스 모드로 초기화 (GUI 없이 테스트)
        System.setProperty("testfx.robot", "glass");
        System.setProperty("testfx.headless", "true");
        System.setProperty("prism.order", "sw");
        System.setProperty("prism.text", "t2k");
        System.setProperty("java.awt.headless", "true");
    }

    @Test
    public void testAppInstantiation() {
        // App 인스턴스가 정상적으로 생성되는지 테스트
        App app = new App();
        assertNotNull("App 인스턴스가 null이 아니어야 합니다", app);
        assertTrue("App은 JavaFX Application을 상속해야 합니다",
                app instanceof javafx.application.Application);
    }

    @Test
    public void testAppHasMainMethod() {
        // main 메서드가 존재하는지 테스트
        try {
            App.class.getDeclaredMethod("main", String[].class);
            assertTrue("main 메서드가 존재합니다", true);
        } catch (NoSuchMethodException e) {
            fail("main 메서드가 존재하지 않습니다");
        }
    }

    @Test
    public void testAppHasStartMethod() {
        // start 메서드가 존재하는지 테스트 (JavaFX Application에서 필수)
        try {
            App.class.getDeclaredMethod("start", javafx.stage.Stage.class);
            assertTrue("start 메서드가 존재합니다", true);
        } catch (NoSuchMethodException e) {
            fail("start 메서드가 존재하지 않습니다");
        }
    }

    @Test
    public void testAppPackage() {
        // App 클래스가 올바른 패키지에 있는지 테스트
        String expectedPackage = "org.example";
        String actualPackage = App.class.getPackage().getName();
        assertEquals("패키지명이 일치해야 합니다", expectedPackage, actualPackage);
    }

    @Test
    public void testAppClassStructure() {
        // App 클래스의 기본 구조 테스트
        assertTrue("클래스명에 App이 포함되어야 합니다", 
                  App.class.getName().contains("App"));
        
        // JavaFX Application을 상속하는지 확인
        Class<?> superClass = App.class.getSuperclass();
        assertEquals("App은 JavaFX Application을 상속해야 합니다",
                    "javafx.application.Application", superClass.getName());
    }

    @Test
    public void testAppMethodsExist() {
        // 필수 메서드들이 존재하는지 확인
        boolean hasMain = false;
        boolean hasStart = false;
        
        try {
            App.class.getDeclaredMethod("main", String[].class);
            hasMain = true;
        } catch (NoSuchMethodException e) {
            // main 메서드 없음
        }
        
        try {
            App.class.getDeclaredMethod("start", javafx.stage.Stage.class);
            hasStart = true;
        } catch (NoSuchMethodException e) {
            // start 메서드 없음
        }
        
        assertTrue("main 메서드가 존재해야 합니다", hasMain);
        assertTrue("start 메서드가 존재해야 합니다", hasStart);
    }
}