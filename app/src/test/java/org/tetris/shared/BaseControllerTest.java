package org.tetris.shared;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

public class BaseControllerTest {
    
    // 테스트용 구체 클래스들
    private static class TestModel extends BaseModel {
        private String value;
        
        public TestModel(String value) {
            this.value = value;
        }
        
        public String getValue() {
            return value;
        }
        
        public void setValue(String value) {
            this.value = value;
        }
    }
    
    private static class TestController extends BaseController<TestModel> {
        private boolean initializeCalled = false;
        
        public TestController(TestModel model) {
            super(model);
        }
        
        @Override
        protected void initialize() {
            super.initialize();
            initializeCalled = true;
        }
        
        public boolean isInitializeCalled() {
            return initializeCalled;
        }
    }
    
    private TestModel testModel;
    private TestController testController;
    
    @Before
    public void setUp() {
        testModel = new TestModel("test");
        testController = new TestController(testModel);
    }
    
    @Test
    public void testControllerCreation() {
        assertNotNull("컨트롤러가 null이 아니어야 합니다", testController);
        assertNotNull("컨트롤러의 모델이 null이 아니어야 합니다", testController.model);
        assertEquals("컨트롤러가 올바른 모델을 가져야 합니다", testModel, testController.model);
    }
    
    @Test
    public void testControllerInheritance() {
        assertTrue("TestController는 BaseController를 상속해야 합니다", 
                  testController instanceof BaseController);
    }
    
    @Test
    public void testModelAccess() {
        assertEquals("컨트롤러를 통해 모델에 접근할 수 있어야 합니다", 
                    "test", testController.model.getValue());
        
        testController.model.setValue("modified");
        assertEquals("컨트롤러를 통해 모델을 수정할 수 있어야 합니다", 
                    "modified", testController.model.getValue());
    }
    
    @Test
    public void testInitializeMethod() {
        // initialize 메서드는 일반적으로 FXML 로딩 시 자동 호출되지만, 
        // 테스트에서는 수동으로 호출하여 확인
        assertFalse("초기에는 initialize가 호출되지 않아야 합니다", 
                   testController.isInitializeCalled());
        
        testController.initialize();
        assertTrue("initialize 호출 후에는 플래그가 true여야 합니다", 
                  testController.isInitializeCalled());
    }
    
    @Test(expected = NullPointerException.class)
    public void testNullModelCreation() {
        new TestController(null);
    }
}