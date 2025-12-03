package org.tetris.game.model;

import org.tetris.shared.BaseModel;

public class DualGameModel extends BaseModel {
    private GameModel player1GameModel;
    private GameModel player2GameModel;
    private AttackModel player1AttackModel; // Player 1이 받을 공격
    private AttackModel player2AttackModel; // Player 2가 받을 공격
    private TimeAttack timeAttack;

    public DualGameModel() {
        this.player1GameModel = new GameModel();
        this.player2GameModel = new GameModel();
        this.player1AttackModel = new AttackModel();
        this.player2AttackModel = new AttackModel();
        this.timeAttack = new TimeAttack();
    }

    public GameModel getPlayer1GameModel() {
        return player1GameModel;
    }

    public GameModel getPlayer2GameModel() {
        return player2GameModel;
    }

    public AttackModel getPlayer1AttackModel() {
        return player1AttackModel;
    }

    public AttackModel getPlayer2AttackModel() {
        return player2AttackModel;
    }

    public TimeAttack getTimeAttack() {
        return timeAttack;
    }
}
