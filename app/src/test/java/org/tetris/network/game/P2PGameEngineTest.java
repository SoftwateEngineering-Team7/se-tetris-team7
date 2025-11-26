package org.tetris.network.game;

import org.junit.Before;
import org.junit.Test;
import org.tetris.network.BaseNetworkTest;
import org.tetris.network.ClientThread;
import org.tetris.network.comand.*;
import org.tetris.network.game.controller.P2PGameController;
import org.tetris.network.game.model.P2PGameModel;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * P2PGameEngine의 로직을 테스트합니다.
 * 로컬 동작 시 커맨드 전송 여부와, 원격 커맨드 수신 시 동작을 검증합니다.
 */
public class P2PGameEngineTest extends BaseNetworkTest {

    private P2PGameEngine engine;
    private TestClientThread mockClientThread;
    private TestP2PGameController mockController;

    @Before
    public void setUp() throws java.io.IOException {
        super.setUp();
        // BaseNetworkTest의 setUp은 서버/클라이언트 리스트 초기화만 수행
        // 여기서는 Mock ClientThread를 주입하여 네트워크 전송을 검증

        P2PGameModel model = new P2PGameModel();
        mockController = new TestP2PGameController(model);
        engine = new P2PGameEngine(null, null, model, mockController);

        mockClientThread = new TestClientThread(engine);
        engine.setClientThread(mockClientThread);
    }

    @Test
    public void testDoLocalMoveLeftSendsCommand() {
        engine.doLocalMoveLeft();
        assertTrue(mockClientThread.lastCommand instanceof MoveLeftCommand);
    }

    @Test
    public void testDoLocalMoveRightSendsCommand() {
        engine.doLocalMoveRight();
        assertTrue(mockClientThread.lastCommand instanceof MoveRightCommand);
    }

    @Test
    public void testDoLocalRotateSendsCommand() {
        engine.doLocalRotate();
        assertTrue(mockClientThread.lastCommand instanceof RotateCommand);
    }

    @Test
    public void testDoLocalSoftDropSendsCommand() {
        engine.doLocalSoftDrop();
        assertTrue(mockClientThread.lastCommand instanceof SoftDropCommand);
    }

    @Test
    public void testDoLocalHardDropSendsCommand() {
        engine.doLocalHardDrop();
        assertTrue(mockClientThread.lastCommand instanceof HardDropCommand);
    }

    @Test
    public void testGameOverSendsCommand() {
        engine.gameOver(100);
        assertTrue(mockClientThread.lastCommand instanceof GameOverCommand);
    }

    // 원격 동작 테스트 (실제 로직 실행 여부는 통합 테스트나 별도 로직 테스트에서 검증)
    // 여기서는 메서드 호출 시 예외가 발생하지 않는지 확인
    @Test
    public void testRemoteActions() {
        engine.moveLeft();
        engine.moveRight();
        engine.rotate();
        engine.softDrop();
        engine.hardDrop();
        // No exceptions expected
    }

    // Manual Stub for ClientThread
    private static class TestClientThread extends ClientThread {
        GameCommand lastCommand;

        public TestClientThread(GameEngine gameEngine) {
            super(gameEngine);
        }

        @Override
        public void sendCommand(GameCommand command) {
            this.lastCommand = command;
        }

        @Override
        public void connect(String host, int port) {
            // Do nothing
        }

        @Override
        public void disconnect() {
            // Do nothing
        }
    }

    // Manual Stub for P2PGameController
    private static class TestP2PGameController extends P2PGameController {
        public TestP2PGameController(P2PGameModel model) {
            super(model);
        }

        @Override
        public void updateScoreDisplay() {
        }

        @Override
        public void updateLevelDisplay() {
        }

        @Override
        public void updateLinesDisplay() {
        }

        @Override
        public void updateNextBlockPreview() {
        }

        @Override
        public void updateGameBoard() {
        }

        @Override
        public void updatePauseUI(boolean isPaused) {
        }

        @Override
        public void showGameOver() {
        }

        @Override
        public void initialize() {
            // Do nothing to avoid FXML/UI setup
        }
    }
}
