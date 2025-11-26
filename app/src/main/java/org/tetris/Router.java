package org.tetris;

import org.tetris.menu.setting.model.Setting;
import org.tetris.menu.setting.SettingMenuFactory;
import org.tetris.menu.start.StartMenuFactory;
import org.tetris.scoreboard.ScoreBoardFactory;
import org.tetris.network.menu.NetworkMenuFactory;
import org.tetris.game.DualGameFactory;
import org.tetris.game.GameFactory;
import org.tetris.game.controller.DualGameController;
import org.tetris.game.controller.GameController;
import org.tetris.game.model.GameMode;
import org.tetris.scoreboard.controller.ScoreBoardController;
import org.tetris.scoreboard.model.ScoreBoard;
import org.tetris.shared.*;
import org.util.ScreenPreset;

import javafx.stage.Modality;
import javafx.stage.Stage;

public final class Router {
    private final Stage stage;

    // 화면별 팩토리들을 주입
    private final StartMenuFactory startMenuFactory;
    private final Setting setting; // 전역 Setting 객체
    private final MvcFactory<?, ?> settingsFactory;
    private final MvcFactory<?, ?> gameFactory;
    private final MvcFactory<?, ?> dualGameFactory;
    private final MvcFactory<ScoreBoard, ScoreBoardController> scoreBoardFactory;
    private final MvcFactory<?, ?> networkMenuFactory;

    private MvcBundle<?, ViewWrap, ?> current; // 현재 화면

    public Router(Stage stage) {
        this.stage = stage;
        this.setting = new Setting();

        this.startMenuFactory = new StartMenuFactory();
        this.settingsFactory = new SettingMenuFactory(setting);
        this.gameFactory = new GameFactory();
        this.scoreBoardFactory = new ScoreBoardFactory();
        this.networkMenuFactory = new NetworkMenuFactory();
        this.dualGameFactory = new DualGameFactory();

        setStageSize();
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

    public void showGamePlaceholder(boolean itemMode) {
        var controller = show(gameFactory);
        if (controller instanceof GameController gameController) {
            gameController.setUpGameMode(itemMode);
            gameController.initialize();
        }
    }

    public void showDualGamePlaceholder(GameMode mode) {
        var controller = show(dualGameFactory);
        if (controller instanceof DualGameController gameController) {
            gameController.setUpGameMode(mode);
            gameController.initialize();
        }
    }

    public void showScoreBoard(boolean fromGame, boolean itemMode, int score) {
        var bundle = scoreBoardFactory.create();
        var controller = bundle.controller();

        if (controller instanceof ScoreBoardController sbc) {
            sbc.setFromGame(fromGame, score);
            sbc.setItemMode(itemMode);
            sbc.setDifficulty(setting.getDifficulty());
        }

        showPopup(scoreBoardFactory, "Score Board");
    }

    public void exitGame() {
        stage.close();
    }

    // Setting getter (필요한 경우)
    public Setting getSetting() {
        return setting;
    }

    public ScoreBoardController getScoreBoardController() {
        return scoreBoardFactory.create().controller();
    }

    public void setStageSize() {
        stage.setWidth(ScreenPreset.getWidth());
        stage.setHeight(ScreenPreset.getHeight());
        stage.centerOnScreen();
    }

    /* --------- 화면 전환 로직 ---------- */

    private <M extends BaseModel, C extends BaseController<M>> C show(MvcFactory<M, C> factory) {
        // 이전 화면 정리
        if (current != null) {
            current.controller().cleanup();
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
        return bundle.controller();
    }

    private <M extends BaseModel, C extends BaseController<M>> C showPopup(MvcFactory<M, C> factory, String title) {
        // 새 화면 생성 + 연결
        MvcBundle<M, ViewWrap, C> bundle = factory.create();

        // 컨트롤러가 Router의 참조를 원하면 주입
        if (bundle.controller() instanceof RouterAware aware) {
            aware.setRouter(this);
        }

        // 팝업 Stage 생성
        Stage popupStage = new Stage();
        popupStage.setTitle(title);
        popupStage.setScene(bundle.view().getScene());

        // 팝업 설정
        popupStage.initModality(Modality.APPLICATION_MODAL); // 모달 팝업
        popupStage.initOwner(stage); // 부모 창 설정
        popupStage.setResizable(false);

        // 팝업 크기 설정 (필요에 따라 조정)
        popupStage.setWidth(600);
        popupStage.setHeight(440);
        popupStage.centerOnScreen();

        // 팝업 표시
        popupStage.showAndWait(); // 팝업이 닫힐 때까지 대기

        return bundle.controller();
    }
}