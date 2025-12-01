package org.tetris.network.comand;

import org.tetris.network.dto.MatchSettings;

/**
 * 게임 메뉴에서 발생할 수 있는 모든 액션을 정의하는 인터페이스.
 * 네트워크 커맨드는 이 인터페이스를 통해 실제 게임 로직(Controller 등)에 명령을 전달합니다.
 */
public interface GameMenuCommandExecutor {
    void onReady(boolean isReady);

    void gameStart(MatchSettings settings);

    void updatePing(long ping);
}
