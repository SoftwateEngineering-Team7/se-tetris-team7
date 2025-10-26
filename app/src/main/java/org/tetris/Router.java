package org.tetris;

import org.tetris.menu.start.StartMenuFactory;
import org.tetris.scoreboard.ScoreBoardFactory;
import org.tetris.shared.*;
import javafx.stage.Stage;

public final class Router {
    private final Stage stage;

    // 화면별 팩토리들을 주입 (원하는 것만)
    private final StartMenuFactory startMenuFactory;
    private final MvcFactory<?, ?> settingsFactory;
    private final MvcFactory<?, ?> gameFactory;

    private MvcBundle<?, ViewWrap, ?> current; // 현재 화면

    public Router(Stage stage) {
        if(stage == null) {
            throw new IllegalArgumentException("Stage는 null일 수 없습니다");
        }
        this.stage = stage;

        this.startMenuFactory = new StartMenuFactory();
        this.settingsFactory = new ScoreBoardFactory();
        this.gameFactory = new StartMenuFactory();

        stage.setTitle("Tetris");
        stage.setResizable(false);
    }

    /* --------- 공개 API ---------- */

    public void showStartMenu() {
        show(startMenuFactory);
    }

    public void showSettings() {
        show(settingsFactory);
    }

    public void showGamePlaceholder() {
        show(gameFactory);
    }

    public void exitGame() {
        stage.close();
    }

    /* --------- 화면 전환 로직 ---------- */

    private <M extends BaseModel, C extends BaseController<M>>
    void show(MvcFactory<M, C> factory) {
        // 이전 화면 정리
        if (current != null) {
            // 만약 필요하면 여기서 컨트롤러 종료 처리 코드 추가
        }

        // 새 화면 생성 + 연결
        MvcBundle<M, ViewWrap, C> bundle = factory.create();

        // 컨트롤러가 Router의 참조를 원하면 주입
        if (bundle.controller() instanceof RouterAware aware) {
            aware.setRouter(this);
        }

        // Stage 적용 및 라이프사이클
        stage.setScene(bundle.view().getScene());
        stage.show();

        current = bundle;
    }
}