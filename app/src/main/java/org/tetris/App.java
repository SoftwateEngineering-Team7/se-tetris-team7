package org.tetris;

import org.tetris.network.GameClient;
import org.tetris.network.GameServer;

import javafx.application.Application;
import javafx.stage.Stage;

public class App extends Application {
    @Override
    public void start(Stage stage) {
        Router nav = new Router(stage);
        nav.showStartMenu(); // 시작 화면 표시
        stage.show();
    }

    /**
     * 앱 종료 시 네트워크 스레드 정리
     */
    @Override
    public void stop() {
        System.out.println("[APP] Application stopping, cleaning up resources...");
        
        // 클라이언트 연결 종료
        try {
            GameClient client = GameClient.getInstance();
            if (client.isConnected()) {
                client.disconnect();
            }
        } catch (Exception e) {
            System.err.println("[APP] Error disconnecting client: " + e.getMessage());
        }
        
        // 서버 종료
        try {
            GameServer.getInstance().stop();
        } catch (Exception e) {
            System.err.println("[APP] Error stopping server: " + e.getMessage());
        }
        
        System.out.println("[APP] Cleanup complete.");
    }

    public static void main(String[] args) {
        launch();
    }
}