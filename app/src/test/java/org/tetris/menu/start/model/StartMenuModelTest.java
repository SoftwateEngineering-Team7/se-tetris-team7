package org.tetris.menu.start.model;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

public class StartMenuModelTest {
    
    private StartMenuModel model;
    
    @Before
    public void setUp() {
        model = new StartMenuModel(3); // 3개 버튼 (게임 시작, 설정, 종료)
    }
    
    @Test
    public void testModelCreation() {
        assertNotNull("모델이 null이 아니어야 합니다", model);
        assertEquals("초기 선택 인덱스는 0이어야 합니다", 0, model.getSelectedIndex());
    }
    
    @Test
    public void testMoveDown() {
        assertEquals("초기 인덱스는 0이어야 합니다", 0, model.getSelectedIndex());
        
        model.move(1);
        assertEquals("인덱스가 1로 이어야 합니다", 1, model.getSelectedIndex());
        
        model.move(1);
        assertEquals("인덱스가 2로 이어야 합니다", 2, model.getSelectedIndex());
    }
    
    @Test
    public void testMoveWrapAround() {
        // 마지막에서 아래로 이동하면 처음으로 순환
        model.move(1); // 0 -> 1
        model.move(1); // 1 -> 2
        model.move(1); // 2 -> 0 (순환)
        assertEquals("인덱스가 2에서 순환하여 0이어야 합니다(순환)", 0, model.getSelectedIndex());
        
        // 첫번째에서 위로 이동하면 마지막으로 순환
        model.move(-1); // 0 -> 2 (역순환)
        assertEquals("인덱스가 1로 이어야 합니다", 2, model.getSelectedIndex());
        
        model.move(-1); // 2 -> 1
        assertEquals("인덱스가 0으로 이어야 합니다", 1, model.getSelectedIndex());
    }
    
    @Test
    public void testMoveUp() {
        // 첫번째에서 위로 이동하면 마지막으로 순환
        assertEquals("초기 상태에서 다음으로 이동하면 0으로 이어야 합니다", 0, model.getSelectedIndex());
        
        model.move(-1);
        assertEquals("첫번째에서 위 이전으로 이동하면 2로 이어야 합니다", 2, model.getSelectedIndex());
    }
    
    @Test
    public void testDifferentButtonCounts() {
        // 2개 버튼 모델 테스트
        StartMenuModel model2 = new StartMenuModel(2);
        assertEquals("2개 버튼 모델의 초기 인덱스는 0이어야 합니다", 0, model2.getSelectedIndex());
        
        model2.move(1);
        assertEquals("인덱스가 1로 이어야 합니다", 1, model2.getSelectedIndex());
        
        model2.move(1);
        assertEquals("2개 버튼에서 순환하면 0으로 이어야 합니다", 0, model2.getSelectedIndex());
        
        // 5개 버튼 모델 테스트
        StartMenuModel model5 = new StartMenuModel(5);
        for (int i = 0; i < 5; i++) {
            assertEquals("5개 버튼 모델에서 인덱스가 " + i + "이어야 합니다", i, model5.getSelectedIndex());
            model5.move(1);
        }
        
        assertEquals("5개 버튼에서 5번 이동하면 0으로 순환해야 합니다", 0, model5.getSelectedIndex());
    }
}