package org.tetris.game;

import org.tetris.game.controller.DualGameController;
import org.tetris.game.model.DualGameModel;
import org.tetris.shared.MvcFactory;

/**
 * 로컬 멀티플레이 게임 화면을 생성하는 팩토리 클래스입니다.
 * DualGameModel과 DualGameController를 연결하여 MVC 구조의 화면을 생성합니다.
 */
public class DualGameFactory extends MvcFactory<DualGameModel, DualGameController> {
    public DualGameFactory() {
        super(() -> new DualGameModel(), model -> new DualGameController(model), "view/dual_game.fxml");
    }

}
