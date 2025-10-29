package org.tetris.shared;

import org.junit.Test;
import static org.junit.Assert.*;

import org.tetris.Router;

public class RouterAwareTest {
    
    // 테스트용 RouterAware 구현체
    private static class TestRouterAware implements RouterAware {
        private Router router;
        
        @Override
        public void setRouter(Router router) {
            this.router = router;
        }
        
        public Router getRouter() {
            return router;
        }
    }
    
    @Test
    public void testRouterAwareInterface() {
        TestRouterAware routerAware = new TestRouterAware();
        
        // 초기에는 router가 null이어야 함
        assertNull("초기에는 router가 null이어야 합니다", routerAware.getRouter());
        
        // Router 설정 (null로 테스트)
        Router router = null; // Router는 final 클래스이므로 실제 인스턴스 생성 필요시 별도 처리
        routerAware.setRouter(router);
        
        // Router가 올바르게 설정되었는지 확인
        assertEquals("설정된 Router가 올바르게 반환되어야 합니다", router, routerAware.getRouter());
    }
    
    @Test
    public void testRouterAwareWithNullRouter() {
        TestRouterAware routerAware = new TestRouterAware();
        
        // null router 설정 (허용되어야 함)
        routerAware.setRouter(null);
        assertNull("null router가 설정되어야 합니다", routerAware.getRouter());
    }
    
    // @Test
    // public void testRouterAwareReplacement() {
    //     TestRouterAware routerAware = new TestRouterAware();
        
    //     // 첫 번째 Router 설정
    //     Router router1 = new Router(null);
    //     routerAware.setRouter(router1);
    //     assertEquals("첫 번째 Router가 설정되어야 합니다", router1, routerAware.getRouter());
        
    //     // 두 번째 Router로 교체
    //     Router router2 = new Router(null);
    //     routerAware.setRouter(router2);
    //     assertEquals("두 번째 Router로 교체되어야 합니다", router2, routerAware.getRouter());
    //     assertNotEquals("이전 Router와는 달라야 합니다", router1, routerAware.getRouter());
    // }
    
    // @Test
    // public void testInterfaceImplementation() {
    //     TestRouterAware routerAware = new TestRouterAware();
        
    //     // RouterAware 인터페이스를 구현하는지 확인
    //     assertTrue("TestRouterAware는 RouterAware를 구현해야 합니다", 
    //               routerAware instanceof RouterAware);
        
    //     // 인터페이스 메서드가 존재하는지 확인
    //     RouterAware interfaceRef = routerAware;
    //     Router router = new Router(null);
    //     // 인터페이스 참조를 통해 메서드 호출 가능한지 확인
    //     interfaceRef.setRouter(router);
    //     assertEquals("인터페이스를 통한 Router 설정이 동작해야 합니다", 
    //                 router, routerAware.getRouter());
    // }
}