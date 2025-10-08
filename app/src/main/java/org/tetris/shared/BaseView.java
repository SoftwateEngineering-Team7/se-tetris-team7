package org.tetris.shared;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

import java.net.URL;
import java.util.Objects;

public abstract class BaseView {
    protected final Parent root;
    protected final Scene scene;
    
    protected BaseView() {
        try {
            // FXML 경로를 서브클래스가 제공
            URL url = Objects.requireNonNull(
                getClass().getResource(fxmlPath()),
                "FXML not found: " + fxmlPath()
            );
            FXMLLoader loader = new FXMLLoader(url);

            // 컨트롤러 제공 방식: 기본은 this(서브클래스가 컨트롤러인 패턴)
            Object controller = provideController();
            if (controller != null) {
                // FXML에 fx:controller 없어야 함
                loader.setController(controller);
            }

            // 로드
            Parent loaded = (Parent) loader.load();
            this.root = loaded;
            this.scene = new Scene(root);

            // 공통 로그: 서브클래스 이름이 찍힙니다.
            // (생성자 안에서 getClass()는 실제 서브클래스를 반환하므로 의도대로 동작)
            System.out.println(getClass().getSimpleName() + " constructor called");

            // 로드 이후 훅
            afterLoad(controller != null ? controller : loader.getController());

        } catch (Exception e) {
            throw new RuntimeException("Failed to load " + fxmlPath(), e);
        }
    }

    /** 서브클래스가 FXML의 클래스패스 경로를 반환
     * (예: "/org/tetris/score/ScoreBoard.fxml") 
     * @return FXML 파일 경로
     * */
    protected abstract String fxmlPath();

    /** 필요 시 별도 컨트롤러를 제공 (기본: this 사용, 아니면 null 리턴하고 fx:controller 사용) */
    protected abstract Object provideController();

    /** 로드 이후 초기화 훅 (옵션) */
    protected void afterLoad(Object controller) {}

    public Scene getScene() { return scene; }
    public Parent getRoot() { return root; }
}
