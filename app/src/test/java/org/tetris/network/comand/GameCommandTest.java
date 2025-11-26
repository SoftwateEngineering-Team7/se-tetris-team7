package org.tetris.network.comand;

import org.junit.Before;
import org.junit.Test;
import org.tetris.game.view.GameViewCallback;
import org.tetris.network.game.GameEngine;
import org.tetris.shared.BaseModel;

import static org.junit.Assert.*;

/**
 * GameCommand의 각 구현체가 GameEngine의 올바른 메서드를 호출하는지 테스트합니다.
 */
public class GameCommandTest {

    private TestGameEngine mockEngine;

    @Before
    public void setUp() {
        mockEngine = new TestGameEngine();
    }

    @Test
    public void testMoveLeftCommand() {
        GameCommand command = new MoveLeftCommand();
        command.execute(mockEngine);
        assertTrue(mockEngine.moveLeftCalled);
    }

    @Test
    public void testMoveRightCommand() {
        GameCommand command = new MoveRightCommand();
        command.execute(mockEngine);
        assertTrue(mockEngine.moveRightCalled);
    }

    @Test
    public void testRotateCommand() {
        GameCommand command = new RotateCommand();
        command.execute(mockEngine);
        assertTrue(mockEngine.rotateCalled);
    }

    @Test
    public void testSoftDropCommand() {
        GameCommand command = new SoftDropCommand();
        command.execute(mockEngine);
        assertTrue(mockEngine.softDropCalled);
    }

    @Test
    public void testHardDropCommand() {
        GameCommand command = new HardDropCommand();
        command.execute(mockEngine);
        assertTrue(mockEngine.hardDropCalled);
    }

    @Test
    public void testGameOverCommand() {
        int score = 1000;
        GameCommand command = new GameOverCommand(score);
        command.execute(mockEngine);
        assertTrue(mockEngine.onGameResultCalled);
        assertEquals(score, mockEngine.lastScore);
        assertTrue(mockEngine.lastIsWinner);
    }

    // Manual Stub for GameEngine
    private static class TestGameEngine extends GameEngine<GameViewCallback, BaseModel> {
        boolean moveLeftCalled = false;
        boolean moveRightCalled = false;
        boolean rotateCalled = false;
        boolean softDropCalled = false;
        boolean hardDropCalled = false;
        boolean onGameResultCalled = false;
        int lastScore = -1;
        boolean lastIsWinner = false;

        public TestGameEngine() {
            super(null, null, null);
        }

        @Override
        public void moveLeft() {
            moveLeftCalled = true;
        }

        @Override
        public void moveRight() {
            moveRightCalled = true;
        }

        @Override
        public void rotate() {
            rotateCalled = true;
        }

        @Override
        public void softDrop() {
            softDropCalled = true;
        }

        @Override
        public void hardDrop() {
            hardDropCalled = true;
        }

        @Override
        public void onGameResult(boolean isWinner, int score) {
            onGameResultCalled = true;
            lastIsWinner = isWinner;
            lastScore = score;
        }

        @Override
        public void startGame(long seed) {
        }

        @Override
        public void restartGame() {
        }

        @Override
        public void stopGame() {
        }

        @Override
        public void gameOver(int score) {
        }
    }
}
