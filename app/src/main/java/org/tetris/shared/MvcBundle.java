package org.tetris.shared;

/**
 * MVC 컴포넌트 묶음
 * @return <M> Model 타입, ViewWrap 타입, <C> Controller 타입
 */
public record MvcBundle<
    M extends BaseModel, 
    V extends ViewWrap,
    C extends BaseController<M>
>(M model, V view, C controller) {}