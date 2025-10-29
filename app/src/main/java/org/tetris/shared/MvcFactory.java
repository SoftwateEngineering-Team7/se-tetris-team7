package org.tetris.shared;


import java.net.URL;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

/**
* MVC를 생성하고 연결하는 추상 팩토리 클래스.
*/
public abstract class MvcFactory<M extends BaseModel,
                                     C extends BaseController<M>> {

    private final Supplier<M> makeModel;
    private final Function<M, C> makeController;
    private final String fxmlPath;
    private MvcBundle<M, ViewWrap, C> bundle;

    protected MvcFactory(Supplier<M> makeModel,
                            Function<M, C> makeController,
                            String fxmlPath) {
        this.makeModel = Objects.requireNonNull(makeModel, "makeModel must not be null");
        this.makeController = Objects.requireNonNull(makeController, "makeController must not be null");
        this.fxmlPath = Objects.requireNonNull(fxmlPath, "fxmlPath must not be null");
    }

    public final MvcBundle<M, ViewWrap, C> create() {
        try {
            if(bundle != null) {
                return bundle; // 이미 생성된 경우 재사용
            }
            M model = makeModel.get();
            C controller = makeController.apply(model);

            URL url = Objects.requireNonNull(getClass().getResource(fxmlPath), "FXML not found");
            FXMLLoader loader = new FXMLLoader(url);
            loader.setController(controller); // 또는 setControllerFactory / fx:controller
            Parent root = (Parent) loader.load();
            ViewWrap view = new ViewWrap(root);

            // 디버그 로그: 서브클래스 이름이 찍힙니다.
            System.out.println(getClass().getSimpleName() + " factory called");

            bundle = new MvcBundle<>(model, view, controller);
            
            return bundle;
         } catch (Exception e) {
            throw new RuntimeException("Failed to load FXML", e);
        }
    }

}