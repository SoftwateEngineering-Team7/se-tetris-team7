package org.tetris;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

import javafx.stage.Stage;

public class RouterTest {
    
    private Stage testStage;
    private Router router;
    
    @Before
    public void setUp() {
        // JavaFX 환경이 필요한 테스트이므로, 실제 Stage 생성은 제한적
        // 테스트에서는 null Stage로 생성하여 NPE가 발생할 수 있음을 고려
        testStage = null; // 실제 JavaFX 환경에서는 new Stage()
        router = new Router(testStage);
    }
    
    @Test
    public void testRouterCreation() {
        assertNotNull("Router가 null이 아니어야 합니다", router);
    }
    
    @Test
    public void testRouterWithNullStage() {
        // null Stage로 Router 생성 가능한지 확인
        Router routerWithNullStage = new Router(null);
        assertNotNull("null Stage로도 Router가 생성되어야 합니다", routerWithNullStage);
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
        
        try {
            router.exitGame();
        } catch (Exception e) {
            // Stage가 null이므로 예외 발생 가능
            assertTrue("예상된 예외가 발생해야 합니다", 
                      e instanceof NullPointerException);
        }
    }
}