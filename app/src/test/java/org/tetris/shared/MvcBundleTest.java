package org.tetris.shared;

import javafx.scene.layout.VBox;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

public class MvcBundleTest {
    
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
    
    private TestModel testModel;
    private ViewWrap testView;
    private TestController testController;
    private MvcBundle<TestModel, ViewWrap, TestController> bundle;
    
    @Before
    public void setUp() {
        testModel = new TestModel("test");
        testView = new ViewWrap(new VBox());
        testController = new TestController(testModel);
        bundle = new MvcBundle<>(testModel, testView, testController);
    }
    
    @Test
    public void testBundleCreation() {
        assertNotNull("Bundle이 null이 아니어야 합니다", bundle);
        assertNotNull("Bundle의 model이 null이 아니어야 합니다", bundle.model());
        assertNotNull("Bundle의 view가 null이 아니어야 합니다", bundle.view());
        assertNotNull("Bundle의 controller가 null이 아니어야 합니다", bundle.controller());
    }
    
    @Test
    public void testBundleComponents() {
        assertEquals("Bundle이 올바른 model을 가져야 합니다", testModel, bundle.model());
        assertEquals("Bundle이 올바른 view를 가져야 합니다", testView, bundle.view());
        assertEquals("Bundle이 올바른 controller를 가져야 합니다", testController, bundle.controller());
    }
    
    @Test
    public void testModelValueAccess() {
        assertEquals("Bundle을 통해 model의 값에 접근할 수 있어야 합니다", 
                    "test", bundle.model().getValue());
    }
    
    @Test
    public void testControllerModelConsistency() {
        assertEquals("Controller의 model과 Bundle의 model이 같아야 합니다", 
                    bundle.model(), bundle.controller().model);
    }
    
    @Test
    public void testRecordEquality() {
        MvcBundle<TestModel, ViewWrap, TestController> bundle2 = 
            new MvcBundle<>(testModel, testView, testController);
        
        assertEquals("같은 구성요소를 가진 Bundle들은 동일해야 합니다", bundle, bundle2);
        assertEquals("같은 구성요소를 가진 Bundle들의 hashCode는 같아야 합니다", 
                    bundle.hashCode(), bundle2.hashCode());
    }
    
    @Test
    public void testRecordToString() {
        String bundleString = bundle.toString();
        assertNotNull("toString()이 null을 반환하지 않아야 합니다", bundleString);
        assertTrue("toString()에 클래스 이름이 포함되어야 합니다", 
                  bundleString.contains("MvcBundle"));
    }
}