package org.tetris.network.game;

import org.tetris.game.model.PlayerSlot;
import org.tetris.network.ClientThread;
import org.tetris.network.game.controller.P2PGameController;
import org.tetris.network.game.model.P2PGameModel;

/**
 * P2P 게임 엔진
 * LocalMultiGameEngine을 확장하여 2인 플레이 구조를 재사용합니다.
 * - player (Player 1): Local Player
 * - player2 (Player 2): Remote Player
 */
public class P2PGameEngine extends LocalMultiGameEngine {

    private ClientThread clientThread;

    // We need to adapt P2PGameModel to DualGameModel or use it directly if it
    // extends it.
    // P2PGameModel extends BaseModel, has local and remote GameModels.
    // LocalMultiGameEngine expects DualGameModel.
    // We can create a DualGameModel from P2PGameModel's components or make
    // P2PGameModel extend DualGameModel.
    // Let's assume we pass a DualGameModel constructed from P2PGameModel's parts,
    // or just use P2PGameModel if we change the generic type?
    // LocalMultiGameEngine is GameEngine<GameViewCallback, DualGameModel>.
    // P2PGameEngine needs to be compatible.

    // For now, let's assume we can pass a DualGameModel that wraps the two models.

    public P2PGameEngine(PlayerSlot localPlayer, PlayerSlot remotePlayer, P2PGameModel gameModel,
            P2PGameController controller) {
        super(localPlayer, remotePlayer, gameModel, controller);
    }

    public void setClientThread(ClientThread clientThread) {
        this.clientThread = clientThread;
    }

    @Override
    public void gameOver(int score) {
        super.gameOver(score);
        if (clientThread != null) {
            clientThread.sendCommand(new org.tetris.network.comand.GameOverCommand(score));
        }
    }

    // ===== Remote Actions (Override Command Interface) =====
    // GameEngine methods (moveLeft, etc.) are called by Command objects received
    // from network.
    // In P2P, received commands control the REMOTE player (player2).

    @Override
    public void moveLeft() {
        moveLeftP2(); // Control Remote
    }

    @Override
    public void moveRight() {
        moveRightP2();
    }

    @Override
    public void rotate() {
        rotateP2();
    }

    @Override
    public void softDrop() {
        softDropP2();
    }

    @Override
    public void hardDrop() {
        hardDropP2();
    }

    // ===== Local Actions (Called by Controller) =====
    // Control Local Player (player) and send commands.

    public void doLocalMoveLeft() {
        // Call super.moveLeft() which calls doMoveLeft(player) -> Local Player
        super.moveLeft();
        if (clientThread != null)
            clientThread.sendCommand(new org.tetris.network.comand.MoveLeftCommand());
    }

    public void doLocalMoveRight() {
        super.moveRight();
        if (clientThread != null)
            clientThread.sendCommand(new org.tetris.network.comand.MoveRightCommand());
    }

    public void doLocalRotate() {
        super.rotate();
        if (clientThread != null)
            clientThread.sendCommand(new org.tetris.network.comand.RotateCommand());
    }

    public void doLocalSoftDrop() {
        super.softDrop();
        if (clientThread != null)
            clientThread.sendCommand(new org.tetris.network.comand.SoftDropCommand());
    }

    public void doLocalHardDrop() {
        super.hardDrop();
        if (clientThread != null)
            clientThread.sendCommand(new org.tetris.network.comand.HardDropCommand());
    }

    @Override
    public void togglePause() {
        super.togglePause();
        // P2P pause logic if needed
    }
}
