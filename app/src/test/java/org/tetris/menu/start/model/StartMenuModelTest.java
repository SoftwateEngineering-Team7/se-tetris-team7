package org.tetris.menu.start.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("StartMenuModel 테스트")
class StartMenuModelTest {

    private StartMenuModel model;
    private final int BUTTON_COUNT = 3;

    @BeforeEach
    void setUp() {
        // 각 테스트가 실행되기 전에 모델을 3개의 버튼으로 초기화합니다.
        model = new StartMenuModel(BUTTON_COUNT);
    }

    @Test
    @DisplayName("모델 생성 시 selectedIndex는 0으로 초기화되어야 한다")
    void modelShouldBeInitializedWithIndexZero() {
        assertNotNull(model, "모델은 null이 아니어야 합니다.");
        assertEquals(0, model.getSelectedIndex(), "초기 선택 인덱스는 0이어야 합니다.");
    }

    @Test
    @DisplayName("move(1) 호출 시 인덱스가 1씩 증가해야 한다")
    void moveDownShouldIncrementIndex() {
        // Act & Assert
        model.move(1);
        assertEquals(1, model.getSelectedIndex(), "아래로 1칸 이동 시 인덱스는 1이어야 합니다.");

        model.move(1);
        assertEquals(2, model.getSelectedIndex(), "아래로 1칸 더 이동 시 인덱스는 2여야 합니다.");
    }

    @Test
    @DisplayName("마지막 인덱스에서 아래로 이동하면 첫 인덱스(0)로 순환해야 한다")
    void moveDownShouldWrapAroundFromEndToStart() {
        // Arrange: 마지막 인덱스(2)로 이동합니다.
        model.move(1);
        model.move(1);
        assertEquals(2, model.getSelectedIndex(), "테스트 준비: 마지막 인덱스로 이동해야 합니다.");

        // Act: 마지막 인덱스에서 아래로 이동합니다.
        model.move(1);

        // Assert: 인덱스가 0으로 순환했는지 확인합니다.
        assertEquals(0, model.getSelectedIndex(), "마지막 인덱스에서 아래로 이동하면 0으로 순환해야 합니다.");
    }

    @Test
    @DisplayName("move(-1) 호출 시 인덱스가 1씩 감소해야 한다")
    void moveUpShouldDecrementIndex() {
        // Arrange: 인덱스를 2로 설정합니다.
        model.move(1);
        model.move(1);
        assertEquals(2, model.getSelectedIndex(), "테스트 준비: 인덱스를 2로 설정해야 합니다.");

        // Act & Assert
        model.move(-1);
        assertEquals(1, model.getSelectedIndex(), "위로 1칸 이동 시 인덱스는 1이어야 합니다.");

        model.move(-1);
        assertEquals(0, model.getSelectedIndex(), "위로 1칸 더 이동 시 인덱스는 0이어야 합니다.");
    }

    @Test
    @DisplayName("첫 인덱스에서 위로 이동하면 마지막 인덱스(2)로 순환해야 한다")
    void moveUpShouldWrapAroundFromStartToEnd() {
        model.move(-1);
        assertEquals(2, model.getSelectedIndex(), "첫 인덱스에서 위로 이동하면 마지막 인덱스(2)로 순환해야 합니다.");
    }

    @Test
    @DisplayName("setSelectedIndex() 호출 시 인덱스가 올바르게 설정되어야 한다")
    void setSelectedIndexShouldUpdateIndex() {
        model.setSelectedIndex(1);
        assertEquals(1, model.getSelectedIndex(), "인덱스를 1로 설정해야 합니다.");

        model.setSelectedIndex(2);
        assertEquals(2, model.getSelectedIndex(), "인덱스를 2로 설정해야 합니다.");

        model.setSelectedIndex(0);
        assertEquals(0, model.getSelectedIndex(), "인덱스를 0으로 설정해야 합니다.");
    }
}