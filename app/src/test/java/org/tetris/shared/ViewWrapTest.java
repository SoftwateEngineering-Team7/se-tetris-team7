package org.tetris.shared;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

public class ViewWrapTest {
    
    private Parent testRoot;
    private ViewWrap viewWrap;
    
    @Before
    public void setUp() {
        // JavaFX 환경이 필요하지만, 단순한 Parent 객체로도 테스트 가능
        testRoot = new VBox();
        viewWrap = new ViewWrap(testRoot);
    }
    
    @Test
    public void testViewWrapCreation() {
        assertNotNull("ViewWrap이 null이 아니어야 합니다", viewWrap);
        assertNotNull("ViewWrap의 root가 null이 아니어야 합니다", viewWrap.getRoot());
        assertNotNull("ViewWrap의 scene이 null이 아니어야 합니다", viewWrap.getScene());
    }
    
    @Test
    public void testRootAccess() {
        assertEquals("ViewWrap이 올바른 root를 반환해야 합니다", testRoot, viewWrap.getRoot());
        assertTrue("Root는 VBox 타입이어야 합니다", viewWrap.getRoot() instanceof VBox);
    }
    
    @Test
    public void testSceneCreation() {
        Scene scene = viewWrap.getScene();
        assertNotNull("Scene이 null이 아니어야 합니다", scene);
        assertEquals("Scene의 root가 올바르게 설정되어야 합니다", 
                    testRoot, scene.getRoot());
    }
    
    @Test
    public void testSceneConsistency() {
        Scene scene1 = viewWrap.getScene();
        Scene scene2 = viewWrap.getScene();
        assertSame("같은 Scene 인스턴스를 반환해야 합니다", scene1, scene2);
    }
    
    @Test(expected = NullPointerException.class)
    public void testNullRootCreation() {
        new ViewWrap(null);
    }
}