package org.tetris.menu.start.model;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class StartMenuModelTest {
    private static final int BUTTON_COUNT = 5;
    private StartMenuModel model;

    @Before
    public void setUp() {
        model = new StartMenuModel();
    }

    @Test
    public void testInitialState() {
        // 초기 선택 인덱스는 0이어야 함
        assertEquals("초기 선택 인덱스는 0이어야 합니다.", 0, model.getSelectedIndex());
    }

    @Test
    public void testMoveForward() {
        // 초기 인덱스: 0
        assertEquals(0, model.getSelectedIndex());
        
        // 한 칸 앞으로 이동
        model.move(1);
        assertEquals("한 칸 앞으로 이동하면 인덱스는 1이어야 합니다.", 1, model.getSelectedIndex());
        
        // 한 칸 더 앞으로 이동
        model.move(1);
        assertEquals("다시 한 칸 앞으로 이동하면 인덱스는 2여야 합니다.", 2, model.getSelectedIndex());
    }

    @Test
    public void testMoveBackward() {
        // 초기 인덱스: 0
        assertEquals(0, model.getSelectedIndex());
        
        // 한 칸 뒤로 이동 (wrapping 발생)
        model.move(-1);
        assertEquals("0에서 -1 이동하면 마지막 인덱스(5)로 래핑되어야 합니다.", 
                     5, model.getSelectedIndex());
        
        // 한 칸 더 뒤로 이동
        model.move(-1);
        assertEquals("5에서 -1 이동하면 인덱스는 4여야 합니다.", 4, model.getSelectedIndex());
    }

    @Test
    public void testForwardWrapping() {
        // 마지막 인덱스로 이동
        model.setSelectedIndex(5);
        assertEquals(5, model.getSelectedIndex());

        // 한 칸 앞으로 이동 (wrapping 발생)
        model.move(1);
        assertEquals("마지막 인덱스(5)에서 +1 이동하면 첫 번째 인덱스(0)로 래핑되어야 합니다.",
                     0, model.getSelectedIndex());
    }

    @Test
    public void testBackwardWrapping() {
        // 초기 인덱스: 0
        assertEquals(0, model.getSelectedIndex());
        
        // 한 칸 뒤로 이동 (wrapping 발생)
        model.move(-1);
        assertEquals("0에서 -1 이동하면 마지막 인덱스(5)로 래핑되어야 합니다.", 
                     5, model.getSelectedIndex());
    }

    @Test
    public void testSetSelectedIndexDirect() {
        // 직접 인덱스 설정
        model.setSelectedIndex(2);
        assertEquals("setSelectedIndex(2)로 설정하면 인덱스는 2여야 합니다.", 
                     2, model.getSelectedIndex());
        
        model.setSelectedIndex(3);
        assertEquals("setSelectedIndex(3)로 설정하면 인덱스는 3이어야 합니다.", 
                     3, model.getSelectedIndex());
    }
}
