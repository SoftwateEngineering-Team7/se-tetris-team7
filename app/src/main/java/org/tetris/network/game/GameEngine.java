package org.tetris.network.game;

/**
 * 가상의 게임 엔진 (커맨드 실행 대상)
 * 실제 게임의 로직을 처리하는 역할을 가정합니다.
 * 
 * TODO: 실제 테트리스 GameModel과 통합
 * TODO: 옵저버 패턴으로 UI 자동 업데이트
 * TODO: P2P 대전 - 상대방 게임 상태 수신 및 표시
 */
public class GameEngine {
    private String currentState = "Initial State";

    // TODO: 옵저버 리스트 구현 (UI 업데이트 리스너 등록)
    // private List<GameStateObserver> observers = new ArrayList<>();
    
    public void startGame(long seed) {
        System.out.println("[CLIENT-ENGINE] Game Started with seed: " + seed);
        // TODO: 게임 모델 초기화 및 시드 설정
        // - Random 객체에 시드 적용하여 블록 생성 순서 동기화
        // - 게임 루프 시작
    }

    public void gameOver(int score) {
        System.out.println("[CLIENT-ENGINE] Game Over. Score: " + score);
        // TODO: 게임 오버 처리
        // - 게임 루프 중단
        // - 서버에 GameOverCommand 전송 (ClientHandler 등을 통해)
    }

    public void onGameResult(boolean isWinner, int score) {
        System.out.println("[CLIENT-ENGINE] Game Result: " + (isWinner ? "Win" : "Lose") + ", Score: " + score);
        // TODO: 결과 화면 표시
        // - 승리/패배 메시지 출력
        // - 재시작 버튼 활성화 등
    }

    public void moveLeft() {
        System.out.println("[CLIENT-ENGINE] Move Left");
        // TODO: 실제 게임 모델의 moveLeft 호출
    }

    public void moveRight() {
        System.out.println("[CLIENT-ENGINE] Move Right");
        // TODO: 실제 게임 모델의 moveRight 호출
    }

    public void rotate() {
        System.out.println("[CLIENT-ENGINE] Rotate");
        // TODO: 실제 게임 모델의 rotate 호출
    }

    public void softDrop() {
        System.out.println("[CLIENT-ENGINE] Soft Drop");
        // TODO: 실제 게임 모델의 softDrop 호출
    }

    public void hardDrop() {
        System.out.println("[CLIENT-ENGINE] Hard Drop");
        // TODO: 실제 게임 모델의 hardDrop 호출
    }

    public void attack(int lines) {
        System.out.println("[CLIENT-ENGINE] Attacked! Adding " + lines + " garbage lines.");
        // TODO: 공격 받음 처리
        // - 내 보드 하단에 쓰레기 줄 추가
    }

    public void updateState(String state) {
        this.currentState = state;
        System.out.println("[CLIENT-ENGINE] State updated to: " + state);
        // TODO: 상대방 게임 상태를 UI에 표시
        // - 상대방 보드 상태 업데이트
        // - 상대방 점수, 레벨 등 표시
        // TODO: 실제 게임 상태 객체로 변환 (String -> GameState)
    }

    public String getCurrentState() {
        return currentState;
    }
}
