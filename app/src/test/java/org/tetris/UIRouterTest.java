package org.tetris;

import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.tetris.menu.start.controller.StartMenuController;
import org.tetris.menu.start.model.StartMenuModel;
import org.tetris.menu.start.view.StartMenuView;

import static org.mockito.Mockito.*;

@DisplayName("UIRouter 테스트")
@ExtendWith(MockitoExtension.class)
class UIRouterTest {

    @Mock
    private Stage mockStage;
    @Mock
    private StartMenuView mockView;
    @Mock
    private StartMenuModel mockModel;
    @Mock
    private StartMenuController mockController;
    @Mock
    private Parent mockRoot;

    // --- NullPointerException 해결을 위한 추가 Mock 객체 ---
    @Mock
    private ObservableList<String> mockStyleClass;

    private UIRouter uiRouter;

    @BeforeEach
    void setUp() {
        // --- 핵심 수정 사항 ---
        // 1. Scene 생성자가 mockRoot.getStyleClass()를 호출할 때 null을 반환하지 않도록 설정합니다.
        when(mockRoot.getStyleClass()).thenReturn(mockStyleClass);

        // 2. Scene 생성자가 mockView.getRoot()를 호출할 때 mockRoot를 반환하도록 설정합니다.
        when(mockView.getRoot()).thenReturn(mockRoot);

        // 테스트 친화적인 생성자를 통해 UIRouter를 생성합니다.
        uiRouter = new UIRouter(mockStage, mockView, mockModel, mockController);
    }

    @Test
    @DisplayName("생성 시 컨트롤러를 올바른 파라미터로 초기화해야 한다")
    void constructorShouldInitializeController() {
        verify(mockController).init(uiRouter, mockModel);
    }

    @Test
    @DisplayName("showStartMenu() 호출 시 Scene을 설정하고 Stage를 보여줘야 한다")
    void showStartMenuShouldSetSceneAndShowStage() {
        uiRouter.showStartMenu();
        verify(mockStage).setScene(any(Scene.class));
        verify(mockStage).show();
        verify(mockController).bindInput();
    }

    @Test
    @DisplayName("exitGame() 호출 시 stage.close()를 호출해야 한다")
    void exitGameShouldCloseStage() {
        uiRouter.exitGame();
        verify(mockStage).close();
    }
}