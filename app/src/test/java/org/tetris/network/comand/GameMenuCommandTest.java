package org.tetris.network.comand;

import org.junit.Test;
import org.tetris.network.mocks.TestGameMenuCommandExecutor;

import static org.junit.Assert.*;

public class GameMenuCommandTest {

    @Test
    public void testReadyCommand() {
        TestGameMenuCommandExecutor executor = new TestGameMenuCommandExecutor();
        boolean isReady = true;

        // 익명 클래스로 ReadyCommand 흉내내기 (실제 ReadyCommand가 있다면 그것을 사용)
        // 하지만 여기서는 GameMenuCommand 인터페이스를 구현하는 방식으로 테스트
        GameMenuCommand command = new GameMenuCommand() {
            @Override
            public void execute(GameMenuCommandExecutor exec) {
                exec.onReady(isReady);
            }
        };

        command.execute(executor);

        assertTrue(executor.executedCommands.contains("onReady"));
        assertTrue(executor.lastIsReady);
    }

    @Test
    public void testGameStartCommand() {
        TestGameMenuCommandExecutor executor = new TestGameMenuCommandExecutor();

        GameMenuCommand command = new GameMenuCommand() {
            @Override
            public void execute(GameMenuCommandExecutor exec) {
                exec.gameStart();
            }
        };

        command.execute(executor);

        assertTrue(executor.executedCommands.contains("gameStart"));
    }
}
