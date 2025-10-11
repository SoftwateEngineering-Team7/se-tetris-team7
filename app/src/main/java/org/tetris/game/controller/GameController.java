package org.tetris.game.controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import org.tetris.game.model.Board;

public class GameController {

    private final Board board;
    private Timeline fallTimeline; // 1칸 떨어지는 타임라인
    private Timeline lockTimeline; // 블록이 바닥에 닿았을 때 바로 고정되지 않고 잠시 대기하는 시간
    private boolean isLocking = false;
    private boolean fallRunning = false; // 한 틱이 더 돌거나 다시 예약되는 현상을 방지하기 위한 명시적 가드

    private final double defaultGravitySeconds = 1.0;
    private final double defaultLockDelaySeconds = 0.5;

    public GameController(Board board) {
        this.board = board;
    }

    // 떨어지는 속도(시간) 설정
    private double getTime() {
        double time = defaultGravitySeconds;
        // 시간 계산 로직
        return time;
    }

    // 락 딜레이 시간 설정
    private double getLockTime() {
        double time = defaultLockDelaySeconds;
        // 시간 계산 로직
        return time;
    }

    public void startGameLoop() {
        stopGameLoop(); // 중복 방지
        fallRunning = true;
        scheduleNextFall();
    }

    public void stopGameLoop() {
        fallRunning = false; // 재스케줄 차단을 위해 먼저 false
        if (fallTimeline != null)
            fallTimeline.stop();
    }

    private void scheduleNextFall() {
        if (!fallRunning)
            return; // 실행 의사가 없으면 탈출

        if (fallTimeline != null)
            fallTimeline.stop();

        fallTimeline = new Timeline(
                new KeyFrame(Duration.seconds(getTime()), e -> onBlockDownTick()));

        fallTimeline.setCycleCount(1);

        fallTimeline.setOnFinished(ev -> {
            if (fallRunning)
                scheduleNextFall(); // 실행 의사 확인 후 재예약
        });

        fallTimeline.play();
    }

    // 한칸 떨어뜨리기 틱
    private void onBlockDownTick() {
        if (!fallRunning)
            return;

        if (board.canMoveDown()) {
            cancelLockDelay();
            board.moveToDown();
            // 보드 그리기
        } else {
            startLockDelay();
        }
    }

    /*
     * 락 딜레이, 우선 블록이 아래로 움직일 수 없을 때 lock 딜레이를 하고 그 사이에
     * 회전이나 좌우 이동이 일어나서 canMoveDown()이 true가 되면 딜레이를 취소
     */
    private void startLockDelay() {
        if (isLocking)
            return;

        stopGameLoop();
        isLocking = true;

        if (lockTimeline != null)
            lockTimeline.stop();

        lockTimeline = new Timeline(
                new KeyFrame(Duration.seconds(getLockTime()), e -> {
                    isLocking = false;
                    if (!board.canMoveDown()) {
                        afterPlaceBlock();
                    } else {
                        // 구제된 경우
                        startGameLoop();
                    }
                }));
        lockTimeline.setCycleCount(1);
        lockTimeline.play();
    }

    private void afterPlaceBlock() {
        var rows = board.findFullRows(); // 자리만
        if (rows.isEmpty()) {
            updateGameProgress();
        } else {
            board.clearRowsAndCollapse(rows);
            // animation for clearing rows
            // ui.render();
            updateGameProgress();
        }
    }

    private void gameOver() {
        // game over

        stopGameLoop();
        cancelLockDelay();
    }

    private void updateGameProgress() {
        if (!board.spawnNextBlock()) {
            gameOver();
            return;
        } else {
            // ui.render();
            startGameLoop();
        }
    }

    private void cancelLockDelay() {
        isLocking = false;
        if (lockTimeline != null)
            lockTimeline.stop();
    }

    // 입력 핸들러(간단형)
    public void onMoveLeft() {
        if (board.canMoveLeft()) {
            board.moveToLeft();
            if (board.canMoveDown())
                if (isLocking) {
                    cancelLockDelay();
                    startGameLoop();
                }
            // ui.render();
        }
    }

    public void onMoveRight() {
        if (board.canMoveRight()) {
            board.moveToRight();
            if (board.canMoveDown())
                if (isLocking) {
                    cancelLockDelay();
                    startGameLoop();
                }
            // ui.render();
        }
    }

    public void onRotate(boolean clockwise) {
        if (clockwise) {
            if (board.canRotate(true)) {
                board.rotateToClockwise(true);
                if (board.canMoveDown())
                    if (isLocking) {
                        cancelLockDelay();
                        startGameLoop();
                    }
            }
            // ui.render();
        } else {
            if (board.canRotate(false)) {
                board.rotateToClockwise(false);
                if (board.canMoveDown())
                    if (isLocking) {
                        cancelLockDelay();
                        startGameLoop();
                    }
            }
            // ui.render();
        }
    }

    public void onHardDrop() {
        cancelLockDelay();
        stopGameLoop();
        // 보드 로직 함수
        afterPlaceBlock();
    }
}
