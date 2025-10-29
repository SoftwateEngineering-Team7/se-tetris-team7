package org.tetris;

import org.junit.Test;
import static org.junit.Assert.*;

public class RouterTest {
    
    @Test
    public void testRouterCreationWithNullStage() {
        // null Stage로 Router 생성 시 NullPointerException 발생 예상
        try {
            Router router = new Router(null);
            // 예외가 발생하지 않으면 테스트 실패
            fail("null Stage로 Router 생성 시 NullPointerException이 발생해야 합니다");
        } catch (NullPointerException e) {
            // 예상된 예외
            assertTrue("NullPointerException이 발생해야 합니다", true);
        }
    }
    
    @Test
    public void testRouterPublicMethodsExist() {
        // Router 클래스에 public 메서드들이 존재하는지 확인
        try {
            // 메서드 존재 여부만 확인 (reflection 사용)
            Router.class.getMethod("showStartMenu");
            Router.class.getMethod("showSettings");
            Router.class.getMethod("showGamePlaceholder", boolean.class);
            Router.class.getMethod("exitGame");
            Router.class.getMethod("showScoreBoard", boolean.class, boolean.class, int.class);
            Router.class.getMethod("getScoreBoardController");
            Router.class.getMethod("getSetting");
            
            assertTrue("모든 public 메서드가 존재해야 합니다", true);
        } catch (NoSuchMethodException e) {
            fail("필요한 public 메서드가 존재하지 않습니다: " + e.getMessage());
        }
    }
}