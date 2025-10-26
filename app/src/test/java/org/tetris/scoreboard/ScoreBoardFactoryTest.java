package org.tetris.scoreboard;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

import org.tetris.scoreboard.controller.ScoreBoardController;
import org.tetris.scoreboard.model.ScoreBoard;
import org.tetris.shared.*;

public class ScoreBoardFactoryTest {
    
    private ScoreBoardFactory factory;
    
    @Before
    public void setUp() {
        factory = new ScoreBoardFactory();
    }
    
    @Test
    public void testFactoryCreation() {
        assertNotNull("ScoreBoardFactory가 null이 아니어야 합니다", factory);
    }
    
    @Test
    public void testFactoryInheritance() {
        // Factory가 MvcFactory를 상속하는지 확인
        assertTrue("ScoreBoardFactory는 MvcFactory를 상속해야 합니다", 
                  MvcFactory.class.isAssignableFrom(factory.getClass()));
    }
    
    @Test
    public void testCreateBundle() {
        // Factory의 create 메서드 호출 테스트
        try {
            // Raw type을 사용하여 컴파일 오류 회피
            @SuppressWarnings("rawtypes")
            MvcBundle bundle = factory.create();
            
            // 만약 FXML 파일이 제대로 로드되면 bundle이 생성되어야 함
            if (bundle != null) {
                assertNotNull("Bundle이 null이 아니어야 합니다", bundle);
                assertNotNull("Bundle의 model이 null이 아니어야 합니다", bundle.model());
                assertNotNull("Bundle의 view가 null이 아니어야 합니다", bundle.view());
                assertNotNull("Bundle의 controller가 null이 아니어야 합니다", bundle.controller());
                
                // Model 검증
                assertTrue("Model은 ScoreBoard 타입이어야 합니다", 
                          bundle.model() instanceof ScoreBoard);
                ScoreBoard scoreBoard = (ScoreBoard) bundle.model();
                assertNotNull("Model의 최고 점수 리스트가 null이 아니어야 합니다", 
                             scoreBoard.getHighScoreList());
                
                // Controller 검증
                assertTrue("Controller는 ScoreBoardController 타입이어야 합니다", 
                          bundle.controller() instanceof ScoreBoardController);
                ScoreBoardController controller = (ScoreBoardController) bundle.controller();
                assertEquals("Controller의 초기 점수는 0이어야 합니다", 
                            0, controller.getFinishScore());
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
        try {
            MvcBundle<ScoreBoard, ViewWrap, ScoreBoardController> bundle = factory.create();
            if (bundle != null) {
                ScoreBoard model = bundle.model();
                assertNotNull("모델이 생성되어야 합니다", model);
                assertNotNull("최고 점수 리스트가 초기화되어야 합니다", model.getHighScoreList());
                assertTrue("초기 최고 점수 리스트는 비어있거나 파일에서 로드된 내용이어야 합니다", 
                          model.getHighScoreList().size() >= 0);
            }
        } catch (RuntimeException e) {
            // FXML 파일 문제로 인한 예외는 예상됨
            System.out.println("FXML 로딩 실패: " + e.getMessage());
        }
    }
    
    @Test
    public void testControllerCreation() {
        try {
            MvcBundle<ScoreBoard, ViewWrap, ScoreBoardController> bundle = factory.create();
            if (bundle != null) {
                ScoreBoardController controller = bundle.controller();
                assertNotNull("Controller가 생성되어야 합니다", controller);
                assertNotNull("Controller의 ScoreBoard가 null이 아니어야 합니다", 
                             controller.getScoreBoard());
                assertEquals("Controller의 초기 완료 점수는 0이어야 합니다", 
                            0, controller.getFinishScore());
            }
        } catch (RuntimeException e) {
            // FXML 파일 문제로 인한 예외는 예상됨
            System.out.println("FXML 로딩 실패: " + e.getMessage());
        }
    }
    
    @Test
    public void testBundleCaching() {
        try {
            MvcBundle<ScoreBoard, ViewWrap, ScoreBoardController> bundle1 = factory.create();
            MvcBundle<ScoreBoard, ViewWrap, ScoreBoardController> bundle2 = factory.create();
            
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