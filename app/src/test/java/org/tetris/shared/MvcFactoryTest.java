package org.tetris.shared;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

import java.util.function.Function;
import java.util.function.Supplier;

public class MvcFactoryTest {
    
    // 테스트용 구체 클래스들
    private static class TestModel extends BaseModel {
        private final String value;
        
        public TestModel(String value) {
            this.value = value;
        }
        
        public String getValue() {
            return value;
        }
    }
    
    private static class TestController extends BaseController<TestModel> {
        public TestController(TestModel model) {
            super(model);
        }
    }
    
    // 테스트용 MvcFactory 구현체
    private static class TestMvcFactory extends MvcFactory<TestModel, TestController> {
        public TestMvcFactory(Supplier<TestModel> makeModel,
                             Function<TestModel, TestController> makeController,
                             String fxmlPath) {
            super(makeModel, makeController, fxmlPath);
        }
    }
    
    // 테스트용 Supplier 구현체
    private static class TestModelSupplier implements Supplier<TestModel> {
        private int callCount = 0;
        
        @Override
        public TestModel get() {
            callCount++;
            return new TestModel("test-" + callCount);
        }
        
        public int getCallCount() {
            return callCount;
        }
    }
    
    // 테스트용 Function 구현체
    private static class TestControllerFunction implements Function<TestModel, TestController> {
        private int callCount = 0;
        
        @Override
        public TestController apply(TestModel model) {
            callCount++;
            return new TestController(model);
        }
        
        public int getCallCount() {
            return callCount;
        }
    }
    
    private TestModelSupplier modelSupplier;
    private TestControllerFunction controllerFunction;
    
    @Before
    public void setUp() {
        modelSupplier = new TestModelSupplier();
        controllerFunction = new TestControllerFunction();
    }
    
    @Test
    public void testFactoryCreation() {
        TestMvcFactory factory = new TestMvcFactory(
            modelSupplier, 
            controllerFunction, 
            "test.fxml"
        );
        
        assertNotNull("Factory가 null이 아니어야 합니다", factory);
    }
    
    @Test(expected = NullPointerException.class)
    public void testNullModelSupplier() {
        new TestMvcFactory(null, controllerFunction, "test.fxml");
    }
    
    @Test(expected = NullPointerException.class)
    public void testNullControllerFunction() {
        new TestMvcFactory(modelSupplier, null, "test.fxml");
    }
    
    @Test(expected = NullPointerException.class)
    public void testNullFxmlPath() {
        new TestMvcFactory(modelSupplier, controllerFunction, null);
    }
    
    @Test
    public void testCreateWithInvalidFxmlPath() {
        TestMvcFactory factory = new TestMvcFactory(
            modelSupplier, 
            controllerFunction, 
            "nonexistent.fxml"
        );
        
        // FXML 파일이 존재하지 않으므로 RuntimeException이 발생해야 함
        try {
            factory.create();
            fail("존재하지 않는 FXML 파일에 대해 예외가 발생해야 합니다");
        } catch (RuntimeException e) {
            assertTrue("RuntimeException이 발생해야 합니다", 
                      e.getMessage().contains("Failed to load FXML"));
        }
    }
    
    @Test
    public void testSupplierAndFunctionCalls() {
        TestMvcFactory factory = new TestMvcFactory(
            modelSupplier, 
            controllerFunction, 
            "nonexistent.fxml"  // 실제로는 예외가 발생하지만, 호출 확인용
        );
        
        assertEquals("초기에는 Supplier가 호출되지 않았어야 합니다", 0, modelSupplier.getCallCount());
        assertEquals("초기에는 Function이 호출되지 않았어야 합니다", 0, controllerFunction.getCallCount());
        
        try {
            factory.create();
        } catch (RuntimeException e) {
            // FXML 로딩 실패는 예상됨
        }
        
        // Supplier와 Function이 호출되었는지 확인
        assertEquals("Supplier가 한 번 호출되어야 합니다", 1, modelSupplier.getCallCount());
        assertEquals("Function이 한 번 호출되어야 합니다", 1, controllerFunction.getCallCount());
    }
    
    @Test
    public void testFactoryParameterValidation() {
        // 모든 파라미터가 필수인지 확인
        assertNotNull("modelSupplier가 설정되어야 합니다", modelSupplier);
        assertNotNull("controllerFunction이 설정되어야 합니다", controllerFunction);
        
        // Factory 생성 시 파라미터 검증
        TestMvcFactory factory = new TestMvcFactory(
            modelSupplier, 
            controllerFunction, 
            "test.fxml"
        );
        
        assertNotNull("Factory 인스턴스가 생성되어야 합니다", factory);
    }
}