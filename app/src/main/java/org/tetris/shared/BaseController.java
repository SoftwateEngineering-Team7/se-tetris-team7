package org.tetris.shared;

import javafx.fxml.FXML;
/**
* 모든 컨트롤러가 공통으로 가지는 베이스. 라이프사이클 훅을 제공합니다.
* @param <M> Model 타입
* @param <V> View 타입
*/
public abstract class BaseController<M extends BaseModel, V extends BaseView>  {
    protected M model;
    protected V view;


    public void bind(M model, V view) {
    this.model = model;
    this.view = view;
    }


    /** 최초 바인딩 직후(뷰 로드 완료 후) 호출 */
    @FXML
    protected void initialize() {}

    public M getModel() { return model; }
    public V getView() { return view; }
}