package org.tetris.network;

import org.junit.After;
import org.junit.Before;
import org.tetris.game.engine.GameEngine;
import org.tetris.game.engine.LocalMultiGameEngine;
import org.tetris.game.engine.P2PGameEngine;
import org.tetris.game.model.DualGameModel;
import org.tetris.game.model.P2PGameModel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 네트워크 테스트를 위한 기본 클래스입니다.
 * 서버 시작/종료 및 클라이언트/엔진 생성 헬퍼 메서드를 제공하여 코드 중복을 줄입니다.
 */
public abstract class BaseNetworkTest {
    protected static final int TIMEOUT_SECONDS = 10;
    protected List<ClientThread> clients;
    protected List<GameEngine<?, ?>> gameEngines;

    @Before
    public void setUp() throws IOException {
        clients = new ArrayList<>();
        gameEngines = new ArrayList<>();
        GameServer.getInstance().reset();
    }

    @After
    public void tearDown() {
        for (ClientThread client : clients) {
            if (client != null) {
                client.disconnect();
            }
        }
        clients.clear();
        gameEngines.clear();
        stopServer();
    }

    protected void startServer() throws IOException {
        GameServer.getInstance().start();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    protected void stopServer() {
        GameServer.getInstance().stop();
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    protected ClientThread createClientWithLocalMultiEngine() {
        LocalMultiGameEngine engine = LocalMultiGameEngine.builder()
                .gameModel(new DualGameModel())
                .build();
        ClientThread client = new ClientThread(engine);
        clients.add(client);
        gameEngines.add(engine);
        return client;
    }

    protected ClientThread createClientWithP2PEngine() {
        // P2PGameEngine requires PlayerSlots, but for network testing we might mock
        // them or pass null if allowed
        // P2PGameEngine(PlayerSlot localPlayer, PlayerSlot remotePlayer, P2PGameModel
        // gameModel, P2PGameController controller)
        P2PGameEngine engine = P2PGameEngine.create()
                .gameModel(new P2PGameModel())
                .build();
        engine.setClientThread(null); // Will be set when client is created? No, circular dependency.

        ClientThread client = new ClientThread(engine);
        engine.setClientThread(client);

        clients.add(client);
        gameEngines.add(engine);
        return client;
    }
}
