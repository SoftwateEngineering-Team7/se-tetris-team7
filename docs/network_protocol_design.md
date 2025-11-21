# 1:1 네트워크 테트리스 프로토콜 설계 가이드 (Java Serialization 기반)

이 문서는 현재 프로젝트에 구현된 **Java Serialization** 및 **Command Pattern**을 기반으로 한 1:1 테트리스 네트워크 프로토콜 설계를 다룹니다.

## 1. 기본 구조 및 연결 흐름

별도의 인증 절차 없이, 소켓 연결이 성립되면 즉시 게임 세션에 참여하는 것으로 간주합니다.

1.  **Server (`GameServer`)**: 12345 포트에서 리스닝.
2.  **Client 1**: 접속 -> 대기 상태.
3.  **Client 2**: 접속 -> 게임 시작 준비.
4.  **Game Loop**: `GameCommand` 객체를 직렬화하여 교환.

---

## 2. 패킷 구조 (Packet Structure)

직접 바이트를 조작하는 대신, Java의 `ObjectOutputStream` / `ObjectInputStream`을 사용하여 객체 단위로 통신합니다.

*   **전송 단위**: `GameCommand` 인터페이스를 구현한 객체
*   **직렬화**: `java.io.Serializable` 인터페이스 사용

### 장점
*   구현이 매우 간단함 (객체를 그대로 전송).
*   커맨드 패턴과 결합하여, 수신 측에서 `command.execute(engine)` 형태로 즉시 실행 가능.

### 단점
*   바이너리 크기가 큼 (헤더 오버헤드).
*   Java 이외의 언어와 호환되지 않음.

---

## 3. 커맨드 리스트 (Command List)

모든 커맨드는 `org.tetris.network.comand.GameCommand` 인터페이스를 구현해야 합니다.

### 3.1. 게임 라이프사이클 (Lifecycle)
| 클래스명 | 방향 | 필드 (Payload) | 설명 |
| :--- | :--- | :--- | :--- |
| `GameStartCommand` | S -> C | `long seed` | 두 클라이언트 접속 시 서버가 전송. **공유 시드**로 블록 순서 동기화. |
| `GameOverCommand` | C -> S | `int score` | 플레이어 게임 오버 알림. |
| `GameResultCommand` | S -> C | `boolean isWinner`, `int score` | 승패 결과 통보. |

### 3.2. 게임 플레이 (Gameplay)
실시간 입력을 `GameCommand` 객체로 캡슐화하여 전송합니다.

| 클래스명 | 방향 | 필드 (Payload) | 설명 |
| :--- | :--- | :--- | :--- |
| `MoveLeftCommand` | C <-> S | - | 왼쪽 이동 명령. |
| `MoveRightCommand` | C <-> S | - | 오른쪽 이동 명령. |
| `RotateCommand` | C <-> S | - | 회전 명령. |
| `SoftDropCommand` | C <-> S | - | 소프트 드롭 명령. |
| `HardDropCommand` | C <-> S | - | 하드 드롭 명령. |
| `AttackCommand` | C -> S -> C | `int lines` | **공격**. 상대방에게 보낼 쓰레기 줄 개수. |
| `UpdateStateCommand` | C -> S -> C | `String state` | (디버그/동기화용) 현재 상태 메시지 전송. |

---

## 4. 구현 상세 (Codebase Reference)

### 1) GameCommand 인터페이스
```java
public interface GameCommand extends Serializable {
    void execute(GameEngine game);
}
```
*   모든 커맨드는 이 인터페이스를 구현합니다.
*   `execute(GameEngine game)` 메서드를 통해 수신 측에서 로직을 수행합니다.

### 2) 패킷 전송 (ServerHandler / ClientHandler)
`ObjectOutputStream`을 사용하여 객체를 전송합니다.

```java
// 송신 측
public void sendCommand(GameCommand command) {
    try {
        oos.writeObject(command);
        oos.flush();
    } catch (IOException e) {
        // 에러 처리
    }
}
```

### 3) 패킷 수신 및 실행
`ObjectInputStream`으로 객체를 읽은 후, `execute()`를 호출하여 처리합니다.

```java
// 수신 측 (별도 스레드)
GameCommand command = (GameCommand) ois.readObject();
command.execute(gameEngine);
```

### 4) 서버의 릴레이(Relay) 역할
1:1 대전에서 서버는 한 클라이언트의 커맨드를 다른 클라이언트에게 전달하는 역할을 수행합니다.

```java
// ServerHandler.java (예시)
if (command instanceof MoveLeftCommand) {
    // 상대방 클라이언트에게 전송
    GameServer.getInstance().sendToOtherClient(this, command);
}
```

---

## 5. 주의사항 및 개선 포인트

1.  **동기화**: `ObjectOutputStream`은 같은 객체 참조를 재사용하려는 성질이 있어, 상태가 변한 동일 객체를 다시 보낼 때 `reset()`을 호출해야 할 수 있습니다. (현재는 매번 `new Command()` 하므로 문제없음)
2.  **보안**: `ObjectInputStream`은 역직렬화 취약점이 존재할 수 있으므로, 신뢰할 수 없는 네트워크 환경에서는 주의해야 합니다. (현재 프로젝트 범위에서는 무방)
3.  **성능**: 객체 직렬화 오버헤드가 크므로, 너무 빈번한 전송(예: 마우스 이동 등)에는 부적합하지만, 테트리스 키 입력 정도는 충분히 처리 가능합니다.
