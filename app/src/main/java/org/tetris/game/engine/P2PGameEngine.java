package org.tetris.game.engine;

import org.tetris.game.comand.GameCommand;
import org.tetris.game.comand.GameStartCommand;
import org.tetris.game.controller.P2PGameController;
import org.tetris.game.model.GameModel;
import org.tetris.game.model.P2PGameModel;
import org.tetris.game.model.PlayerSlot;
import org.tetris.network.ClientThread;
import org.tetris.network.GameServer;
import org.tetris.game.model.DualGameModel;
import org.tetris.game.view.GameViewCallback;
import org.tetris.network.menu.model.NetworkMenu;

/**
 * P2P 게임 엔진
 * LocalMultiGameEngine을 확장하여 2인 플레이 구조를 재사용합니다.
 * - player (Player 1): Local Player
 * - player2 (Player 2): Remote Player
 */
public class P2PGameEngine extends LocalMultiGameEngine {

    private ClientThread clientThread;
    private NetworkMenu networkMenu;
    private long lastDropTime1;
    private long lastDropTime2;
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

    protected P2PGameEngine(PlayerSlot localPlayer, PlayerSlot remotePlayer, P2PGameModel gameModel,
            P2PGameController controller) {
        super(localPlayer, remotePlayer, gameModel, controller);
        if (gameModel != null && gameModel.getPlayer2GameModel() != null) {
            gameModel.getPlayer2GameModel().spawnNewBlock();
        }
        startGameLoop();
    }

    public static Builder create() {
        return new Builder();
    }

    public static class Builder extends GameEngine.Builder<Builder, GameViewCallback, DualGameModel> {
        protected PlayerSlot player2;

        public Builder player2(PlayerSlot player2) {
            this.player2 = player2;
            return this;
        }

        @Override
        protected Builder self() {
            return this;
        }

        @Override
        public P2PGameEngine build() {
            return new P2PGameEngine(player, player2, (P2PGameModel) gameModel, (P2PGameController) controller);
        }
    }

    public synchronized NetworkMenu getNetworkMenu() {
        return networkMenu;
    }

    public synchronized void setNetworkMenu(NetworkMenu networkMenu) {
        this.networkMenu = networkMenu;
    }

    public void onReadyCommand(boolean others) {
        if (networkMenu == null) {
            throw new IllegalStateException("NetworkMenu is not set in GameEngine.");
        }

        networkMenu.setOtherIsReady(others);
        if (others && networkMenu.isReady()) {
            System.out.println("[CLIENT-ENGINE] Both players are ready. Starting game...");

            long seed = System.currentTimeMillis();
            GameCommand startCommand = new GameStartCommand(seed);
            GameServer.getInstance().broadcast(startCommand);
        } else {
            System.out.println("[CLIENT-ENGINE] Other Player is not ready.");
        }
    }

    public void setClientThread(ClientThread clientThread) {
        this.clientThread = clientThread;
    }

    @Override
    public void gameOver(int score) {
        super.gameOver(score);
        if (clientThread != null) {
            clientThread.sendCommand(new org.tetris.game.comand.GameOverCommand(score));
        }
    }

    public synchronized void updatePing(long ping) {
        System.out.println("[CLIENT-ENGINE] Ping: " + ping + "ms");
        if (networkMenu != null) {
            networkMenu.setPing(ping);
        }
    }

    @Override
    protected void updateGameLoop(long now) {
        if (gameModel.isPaused())
            return;
        if (gameModel.getPlayer1GameModel().isGameOver() && gameModel.getPlayer2GameModel().isGameOver())
            return;

        updatePlayer(player, gameModel.getPlayer1GameModel(), now, 1);
        updatePlayer(player2, gameModel.getPlayer2GameModel(), now, 2);

        controller.updateScoreDisplay();
        controller.updateLevelDisplay();
        controller.updateLinesDisplay();
        controller.updateNextBlockPreview();
    }

    @Override
    protected void updatePlayer(PlayerSlot p, GameModel model, long now, int playerIdx) {
        if (p == null || model.isGameOver())
            return;

        if (p.isFlashing) {
            tickFlash(p, model, now);
            controller.updateGameBoard();
            return;
        }

        long lastDropTime = (playerIdx == 1) ? lastDropTime1 : lastDropTime2;
        int dropIntervalFrames = model.getDropInterval();
        long dropIntervalNanos = dropIntervalFrames * FRAME_TIME;

        if (now - lastDropTime >= dropIntervalNanos) {
            boolean moved = p.boardModel.autoDown();
            if (moved) {
                p.scoreModel.blockDropped();
            } else {
                lockBlock(p, model);
            }
            if (playerIdx == 1)
                lastDropTime1 = now;
            else
                lastDropTime2 = now;
        }

        // We should call updateGameBoard periodically?
        // SingleGameEngine calls it every frame.
        controller.updateGameBoard();
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
        doHardDrop(player2, gameModel.getPlayer2GameModel());
    }

    // Player 2 Controls
    public void moveLeftP2() {
        doMoveLeft(player2);
    }

    public void moveRightP2() {
        doMoveRight(player2);
    }

    public void rotateP2() {
        doRotate(player2);
    }

    public void softDropP2() {
        doSoftDrop(player2);
    }

    public void hardDropP2() {
        doHardDrop(player2, gameModel.getPlayer2GameModel());
    }
    // ===== Local Actions (Called by Controller) =====
    // Control Local Player (player) and send commands.

    public void doLocalMoveLeft() {
        // Call super.moveLeft() which calls doMoveLeft(player) -> Local Player
        super.moveLeft();
        if (clientThread != null)
            clientThread.sendCommand(new org.tetris.game.comand.MoveLeftCommand());
    }

    public void doLocalMoveRight() {
        super.moveRight();
        if (clientThread != null)
            clientThread.sendCommand(new org.tetris.game.comand.MoveRightCommand());
    }

    public void doLocalRotate() {
        super.rotate();
        if (clientThread != null)
            clientThread.sendCommand(new org.tetris.game.comand.RotateCommand());
    }

    public void doLocalSoftDrop() {
        super.softDrop();
        if (clientThread != null)
            clientThread.sendCommand(new org.tetris.game.comand.SoftDropCommand());
    }

    public void doLocalHardDrop() {
        super.hardDrop();
        if (clientThread != null)
            clientThread.sendCommand(new org.tetris.game.comand.HardDropCommand());
    }

    @Override
    public void togglePause() {
        super.togglePause();
        // P2P pause logic if needed
    }
}
