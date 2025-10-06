package org.example;

import javafx.application.Platform;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.tetris.UIRouter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
 * App.start(Stage)를 단위 테스트합니다.
 * - JavaFX Toolkit 초기화
 * - UIRouter 생성 가로채기(mockConstruction)
 * - router.showStartMenu()와 stage.show() 호출 검증
 */
class AppTest {

    @BeforeAll
    static void initFx() {
        // JavaFX Application Thread 초기화(이미 초기화된 경우 예외 무시)
        try {
            Platform.startup(() -> {
            });
        } catch (IllegalStateException ignore) {
        }
        Platform.setImplicitExit(false);
    }

    @Test
    @DisplayName("start()는 UIRouter를 생성하고 showStartMenu() 호출 후 Stage.show()를 호출한다")
    void start_shouldCreateRouter_showMenu_and_showStage() throws Exception {
        Stage stage = Mockito.mock(Stage.class);

        // new UIRouter(stage) 생성 가로채기
        try (MockedConstruction<UIRouter> mocked = Mockito.mockConstruction(UIRouter.class)) {
            App app = new App();
            app.start(stage);

            // UIRouter가 1회 생성되었는지 확인
            assertEquals(1, mocked.constructed().size());
            UIRouter routerMock = mocked.constructed().get(0);

            // 기대 호출 검증
            verify(routerMock, times(1)).showStartMenu();
            verify(stage, times(1)).show();
            verifyNoMoreInteractions(routerMock);
        }
    }
}