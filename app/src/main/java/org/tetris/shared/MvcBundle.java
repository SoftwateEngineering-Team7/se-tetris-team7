package org.tetris.shared;

/** 묶어서 돌려주는 DTO */
public record MvcBundle<M extends BaseModel,
C extends BaseController<M, V>,
V extends BaseView>(M model, C controller, V view) {}