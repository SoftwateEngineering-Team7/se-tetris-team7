package org.tetris.shared;

import org.junit.Test;
import static org.junit.Assert.*;

public class BaseModelTest {
    
    // 테스트용 구체 클래스
    private static class TestModel extends BaseModel {
        private String testValue;
        
        public TestModel(String testValue) {
            this.testValue = testValue;
        }
        
        public String getTestValue() {
            return testValue;
        }
        
        public void setTestValue(String testValue) {
            this.testValue = testValue;
        }
    }
    
    @Test
    public void testModelCreation() {
        TestModel model = new TestModel("test");
        assertNotNull("모델이 null이 아니어야 합니다", model);
        assertEquals("테스트 값이 올바르게 설정되어야 합니다", "test", model.getTestValue());
    }
    
    @Test
    public void testModelInheritance() {
        TestModel model = new TestModel("test");
        assertTrue("TestModel은 BaseModel을 상속해야 합니다", model instanceof BaseModel);
    }
    
    @Test
    public void testModelValueModification() {
        TestModel model = new TestModel("initial");
        assertEquals("초기값이 올바르게 설정되어야 합니다", "initial", model.getTestValue());
        
        model.setTestValue("modified");
        assertEquals("값이 올바르게 수정되어야 합니다", "modified", model.getTestValue());
    }
}