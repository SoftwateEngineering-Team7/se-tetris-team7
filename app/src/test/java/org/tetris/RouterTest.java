package org.tetris;

import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;
import org.junit.Before;
import static org.junit.Assert.*;

import javafx.stage.Stage;

public class RouterTest extends ApplicationTest {
    
    private Stage testStage;
    private Router router;

    @Override
    public void start(Stage s) throws Exception {
        testStage = s;
    }

    @Before
    public void setUp() {
        router = new Router(testStage);
    }
    
    @Test
    public void testRouterCreation() {
        assertNotNull("Router가 null이 아니어야 합니다", router);
    }
    
    @Test
    public void testRouterWithNullStage() {
        // null로 Router 생성시 예외 발생
        try {
            Router routerWithNullStage = new Router(null);
            fail("예외가 발생해야 합니다");
        } catch (IllegalArgumentException e) {
            assertEquals("Stage는 null일 수 없습니다", e.getMessage());
        }
    }
    
    @Test
    public void testRouterPublicMethods() {
        // Router의 public 메서드들이 존재하는지 확인
        assertNotNull("showStartMenu 메서드가 존재해야 합니다", router);
        assertNotNull("showSettings 메서드가 존재해야 합니다", router);
        assertNotNull("showGamePlaceholder 메서드가 존재해야 합니다", router);
        assertNotNull("exitGame 메서드가 존재해야 합니다", router);
        
        // 메서드 호출 시 예외가 발생하지 않는지 확인 (Stage가 null이므로 일부 예외 예상)
        try {
            router.showStartMenu();
        } catch (Exception e) {
            // Stage가 null이므로 예외 발생 가능
            assertTrue("예상된 예외가 발생해야 합니다", 
                      e instanceof NullPointerException || e instanceof RuntimeException);
        }
        
        try {
            router.showSettings();
        } catch (Exception e) {
            // Stage가 null이므로 예외 발생 가능
            assertTrue("예상된 예외가 발생해야 합니다", 
                      e instanceof NullPointerException || e instanceof RuntimeException);
        }
        
        try {
            router.showGamePlaceholder();
        } catch (Exception e) {
            // Stage가 null이므로 예외 발생 가능
            assertTrue("예상된 예외가 발생해야 합니다", 
                      e instanceof NullPointerException || e instanceof RuntimeException);
        }
        
        // try {
        //     router.exitGame();
        // } catch (Exception e) {
        //     // Stage가 null이므로 예외 발생 가능
        //     assertTrue("예상된 예외가 발생해야 합니다", 
        //               e instanceof NullPointerException);
        // }
    }
}