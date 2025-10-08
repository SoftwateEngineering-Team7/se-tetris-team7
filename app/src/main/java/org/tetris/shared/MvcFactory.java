package org.tetris.shared;

public interface MvcFactory<M extends BaseModel,
C extends BaseController<M, V>,
V extends BaseView> {
    M createModel();
    C createController(M model);
    V createView(C controller);


    /** 기본 구현: 생성 후 컨트롤러에 바인딩까지 마친다. */
    default MvcBundle<M, C, V> create() {
    M model = createModel();
    C controller = createController(model);
    V view = createView(controller);
    controller.bind(model, view);
    return new MvcBundle<>(model, controller, view);
}
}