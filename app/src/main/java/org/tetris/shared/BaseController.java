package org.tetris.shared;

import javafx.fxml.FXML;

/**
* 모든 컨트롤러가 공통으로 가지는 베이스. 라이프사이클 훅을 제공합니다.
* @param <M> Model 타입
*/
public abstract class BaseController<M extends BaseModel>  {
    protected M model;

    protected BaseController(M model) {
        this.model = model;
    }
    
    /**FXML 로딩, model bind 직후 호출 */
    @FXML
    protected void initialize() {}
    
    /**화면 전환 시 리소스 정리를 위해 호출 */
    public void cleanup() {}
}