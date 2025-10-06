package org.tetris.menu.start.view;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.tetris.menu.start.controller.StartMenuController;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@DisplayName("StartMenuView 테스트")
@ExtendWith(MockitoExtension.class)
class StartMenuViewTest {

    @Mock
    private Parent mockRoot;

    @Mock
    private StartMenuController mockController;

    /**
     * JavaFX 컴포넌트를 테스트하기 전에 JavaFX 툴킷을 초기화합니다.
     * 테스트가 여러 번 실행될 때 이미 툴킷이 실행 중일 수 있으므로 IllegalStateException을 처리합니다.
     */
    @BeforeAll
    static void initJavaFX() {
        try {
            Platform.startup(() -> {
            });
        } catch (IllegalStateException e) {
            // JavaFX Platform already started, no-op.
        }
    }

    @Test
    @DisplayName("FXML 로딩 성공 시, root와 controller를 정상적으로 초기화한다")
    void shouldInitializeRootAndControllerOnSuccess() {
        // 이 컨텍스트 내에서 `new FXMLLoader()`가 호출되면, Mockito가 가짜 객체를 대신 주입합니다.
        try (MockedConstruction<FXMLLoader> mockedLoader = Mockito.mockConstruction(FXMLLoader.class,
                (mock, context) -> {
                    when(mock.load()).thenReturn(mockRoot);
                    when(mock.getController()).thenReturn(mockController);
                })) {

            // WHEN: 테스트 대상 객체를 생성합니다.
            StartMenuView view = new StartMenuView();

            // THEN: 생성된 View가 Mock 객체들로 올바르게 초기화되었는지 확인합니다.
            assertEquals(mockRoot, view.getRoot(), "Root 객체가 정상적으로 주입되어야 합니다.");
            assertEquals(mockController, view.getController(), "Controller 객체가 정상적으로 주입되어야 합니다.");
        }
    }

    @Test
    @DisplayName("FXML load() 실패 시, RuntimeException으로 전환하여 던진다")
    void shouldThrowRuntimeExceptionWhenFxmlLoadFails() {
        // 이 컨텍스트 내에서 `new FXMLLoader()`가 호출되면, Mockito가 가짜 객체를 대신 주입합니다.
        try (MockedConstruction<FXMLLoader> mockedLoader = Mockito.mockConstruction(FXMLLoader.class,
                (mock, context) -> {
                    // FXMLLoader의 load() 메소드가 호출될 때 강제로 IOException을 발생시킵니다.
                    when(mock.load()).thenThrow(new IOException("Test FXML load failure"));
                })) {

            // WHEN & THEN: StartMenuView 생성 시 RuntimeException이 발생하는지 검증합니다.
            RuntimeException exception = assertThrows(RuntimeException.class,
                    StartMenuView::new,
                    "FXML 로딩 실패 시 RuntimeException이 발생해야 합니다.");

            // 추가 검증: 발생한 예외의 메시지와 원인(cause)이 예상과 일치하는지 확인합니다.
            assertEquals("Failed to load start_menu.fxml", exception.getMessage());
            assertTrue(exception.getCause() instanceof IOException, "원인 예외는 IOException이어야 합니다.");
        }
    }
}