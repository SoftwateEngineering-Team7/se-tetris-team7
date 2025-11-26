package org.tetris.game.comand;

import org.tetris.game.engine.GameEngine;

/**
 * 서버가 클라이언트에게 게임 상태 업데이트를 브로드캐스트하기 위한 커맨드 객체.
 * 게임 보드의 현재 상태와 같은 정보를 포함합니다.
 */
public class UpdateStateCommand implements GameCommand {
    private static final long serialVersionUID = 1L;
    private final String state;

    public UpdateStateCommand(String state) {
        this.state = state;
    }

    @Override
    public void execute(GameEngine<?, ?> game) {
        game.updateState(state);
    }

    public String getState() {
        return state;
    }
}
