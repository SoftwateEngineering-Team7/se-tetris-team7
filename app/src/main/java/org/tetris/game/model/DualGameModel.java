package org.tetris.game.model;

import org.tetris.shared.BaseModel;

/**
 * 로컬 멀티플레이(1 vs 1) 테트리스 게임을 위한 모델 클래스입니다.
 * 두 명의 플레이어 각각의 독립적인 {@link GameModel} 인스턴스를 관리합니다.
 * 각 플레이어의 게임 상태를 별도로 추적하며, 멀티플레이 로직 구현에 활용됩니다.
 */
public class DualGameModel extends BaseModel {
    private GameModel player1GameModel;
    private GameModel player2GameModel;

    public DualGameModel() {
        this.player1GameModel = new GameModel();
        this.player2GameModel = new GameModel();
    }

    public GameModel getPlayer1GameModel() {
        return player1GameModel;
    }

    public GameModel getPlayer2GameModel() {
        return player2GameModel;
    }
}
