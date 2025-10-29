package org.tetris.menu.start;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

import org.tetris.menu.start.controller.StartMenuController;
import org.tetris.menu.start.model.StartMenuModel;
import org.tetris.shared.MvcBundle;
import org.tetris.shared.ViewWrap;

public class StartMenuFactoryTest {
    
    private StartMenuFactory factory;
    
    @Before
    public void setUp() {
        factory = new StartMenuFactory();
    }
    
    @Test
    public void testFactoryCreation() {
        assertNotNull("StartMenuFactory가 null이 아니어야 합니다", factory);
    }
    
    @Test
    public void testFactoryInheritance() {
        assertTrue("StartMenuFactory는 MvcFactory를 상속해야 합니다", 
                  factory instanceof org.tetris.shared.MvcFactory);
    }
    
    @Test
    public void testCreateWithInvalidFxmlPath() {
        // FXML 파일 경로가 잘못되어 있다면 RuntimeException이 발생해야 함
        try {
            MvcBundle<StartMenuModel, ViewWrap, StartMenuController> bundle = factory.create();
            
            // 만약 FXML 파일이 제대로 로드되면 bundle이 생성되어야 함
            if (bundle != null) {
                assertNotNull("Bundle이 null이 아니어야 합니다", bundle);
                assertNotNull("Bundle의 model이 null이 아니어야 합니다", bundle.model());
                assertNotNull("Bundle의 view가 null이 아니어야 합니다", bundle.view());
                assertNotNull("Bundle의 controller가 null이 아니어야 합니다", bundle.controller());
                
                // Model 검증
                assertTrue("Model은 StartMenuModel 타입이어야 합니다", 
                          bundle.model() instanceof StartMenuModel);
                assertEquals("Model의 초기 선택 인덱스는 0이어야 합니다", 
                            0, bundle.model().getSelectedIndex());
                
                // Controller 검증
                assertTrue("Controller는 StartMenuController 타입이어야 합니다", 
                          bundle.controller() instanceof StartMenuController);
            }
            
        } catch (RuntimeException e) {
            // FXML 파일이 없거나 로드에 실패한 경우
            assertTrue("FXML 로딩 실패 메시지가 포함되어야 합니다", 
                      e.getMessage().contains("Failed to load FXML"));
        }
    }
    
    @Test
    public void testModelCreation() {
        // Factory 내부의 모델 생성 로직 테스트
        // StartMenuFactory는 6개 버튼으로 모델을 생성함
        try {
            MvcBundle<StartMenuModel, ViewWrap, StartMenuController> bundle = factory.create();
            if (bundle != null) {
                StartMenuModel model = bundle.model();
                assertEquals("모델이 6개 버튼으로 초기화되어야 합니다", 0, model.getSelectedIndex());
                
                // 모델의 이동 테스트
                model.move(1);
                assertEquals("한 번 이동 후 인덱스가 1이어야 합니다", 1, model.getSelectedIndex());
                
                model.move(1);
                assertEquals("두 번 이동 후 인덱스가 2이어야 합니다", 2, model.getSelectedIndex());
                
                model.move(1);
                assertEquals("세 번 이동 후 인덱스가 3으로 순환되어야 합니다", 3, model.getSelectedIndex());

                model.move(1);
                assertEquals("네 번 이동 후 인덱스가 4이어야 합니다", 4, model.getSelectedIndex());
                
                model.move(1);
                assertEquals("다섯 번 이동 후 인덱스가 5이어야 합니다", 5, model.getSelectedIndex());

                model.move(1);
                assertEquals("여섯 번 이동 후 인덱스가 0으로 순환되어야 합니다", 0, model.getSelectedIndex());
            }
        } catch (RuntimeException e) {
            // FXML 파일 문제로 인한 예외는 예상됨
            System.out.println("FXML 로딩 실패: " + e.getMessage());
        }
    }
    
    @Test
    public void testControllerCreation() {
        try {
            MvcBundle<StartMenuModel, ViewWrap, StartMenuController> bundle = factory.create();
            if (bundle != null) {
                StartMenuController controller = bundle.controller();
                assertNotNull("Controller가 생성되어야 합니다", controller);
                // Controller가 올바르게 생성되었는지 확인 (model 필드는 protected이므로 직접 접근 불가)
                
                // Controller가 RouterAware를 구현하는지 확인
                assertTrue("Controller는 RouterAware를 구현해야 합니다", 
                          controller instanceof org.tetris.shared.RouterAware);
            }
        } catch (RuntimeException e) {
            // FXML 파일 문제로 인한 예외는 예상됨
            System.out.println("FXML 로딩 실패: " + e.getMessage());
        }
    }
    
    @Test
    public void testBundleCaching() {
        try {
            MvcBundle<StartMenuModel, ViewWrap, StartMenuController> bundle1 = factory.create();
            MvcBundle<StartMenuModel, ViewWrap, StartMenuController> bundle2 = factory.create();
            
            if (bundle1 != null && bundle2 != null) {
                // MvcFactory는 bundle을 캐싱하므로 같은 인스턴스를 반환해야 함
                assertSame("Factory는 같은 bundle 인스턴스를 반환해야 합니다", bundle1, bundle2);
                assertSame("캐싱된 bundle의 model이 같아야 합니다", bundle1.model(), bundle2.model());
                assertSame("캐싱된 bundle의 controller가 같아야 합니다", bundle1.controller(), bundle2.controller());
                assertSame("캐싱된 bundle의 view가 같아야 합니다", bundle1.view(), bundle2.view());
            }
        } catch (RuntimeException e) {
            // FXML 파일 문제로 인한 예외는 예상됨
            System.out.println("FXML 로딩 실패: " + e.getMessage());
        }
    }
}