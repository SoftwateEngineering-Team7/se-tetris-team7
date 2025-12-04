# Team7 Tetris 🎮

JavaFX 기반의 모던한 테트리스 게임입니다. MVC 아키텍처 패턴을 적용하여 구현하였으며, 다양한 게임 모드와 아이템 시스템, P2P 네트워크 대전 기능을 제공합니다.

## 📋 목차

- [팀원](#-팀원)
- [주요 기능](#-주요-기능)
- [기술 스택](#-기술-스택)
- [시스템 요구사항](#-시스템-요구사항)
- [설치 및 실행](#-설치-및-실행)
- [프로젝트 구조](#-프로젝트-구조)
- [테스트](#-테스트)
- [빌드 및 배포](#-빌드-및-배포)
- [버전 히스토리](#-버전-히스토리)
- [라이선스](#-라이선스)

## 👥 팀원

| 이름   | GitHub                               |
| ------ | ------------------------------------ |
| 강형석 | [@강형석](https://github.com/강형석) |
| 김대규 | [@김대규](https://github.com/김대규) |
| 김민석 | [@김민석](https://github.com/김민석) |
| 박재홍 | [@박재홍](https://github.com/박재홍) |

태스크 관리 기록
[노션 태스크 관리](https://lively-cuticle-191.notion.site/272bc165c69381c69fb2cbf9aa005360?v=272bc165c6938118be03000cd7fdcb81&source=copy_link)

## ✨ 주요 기능

### 게임 모드

- **일반 모드**: 클래식 테트리스 게임 플레이
- **아이템 모드**: 특수 아이템이 추가된 테트리스
- **타임어택 모드**: 제한 시간 내 최고 점수 도전
- **로컬 멀티플레이어**: 한 화면에서 두 명이 대전
- **P2P 네트워크 대전**: 네트워크를 통한 1:1 실시간 대전

### P2P 네트워크 기능

- **호스트/클라이언트 방식**: 호스트가 서버를 생성하고 클라이언트가 접속
- **실시간 동기화**: 양쪽 플레이어의 보드 상태 실시간 동기화
- **Ping 모니터링**: 네트워크 지연 시간 실시간 표시
- **Ready 시스템**: 양쪽 플레이어 준비 완료 후 게임 시작
- **공격 시스템**: 라인 클리어 시 상대방에게 방해 블록 전송
- **일시정지/재시작**: 호스트에 의한 게임 일시정지 및 재시작

### 아이템 시스템

상세 링크: https://www.notion.so/2bebc165c69380619762fae91c9815e2

- **B (Bomb) 아이템**: 3x3 영역 폭파
- **C (Cross) 아이템**: 특정 행/열 제거
- **V (Vertical) 아이템**: 열 제거
- **L (Line) 아이템**: 행 제거
- **W (Weight) 아이템**: 블록 즉시 낙하

### 블록 타입

7가지 테트로미노 블록 제공:

- I 블록 (막대)
- O 블록 (정사각형)
- T 블록 (T자형)
- S 블록 (S자형)
- Z 블록 (Z자형)
- J 블록 (J자형)
- L 블록 (L자형)

### 기타 기능

- 게임 설정 커스터마이징
- 하이스코어 기록 및 관리
- 다양한 난이도 설정 (Easy, Normal, Hard)
- 키 레이아웃 설정 (플레이어별 독립 설정)
- 화면 크기 프리셋
- 하드 드롭, 라인 클리어 이펙트 애니메이션
- 스코어보드 난이도별 기록 및 초기화

## 🛠 기술 스택

- **Language**: Java 21
- **Framework**: JavaFX 21
- **Build Tool**: Gradle 9.0.0
- **Testing**:
  - JUnit
  - TestFX (JavaFX GUI 테스트)
  - JaCoCo (코드 커버리지)
- **Pattern**: MVC (Model-View-Controller)

## 💻 시스템 요구사항

- **Java Development Kit (JDK)**: 21 이상
- **운영체제**: macOS, Windows, Linux
- **메모리**: 최소 512MB RAM 권장

## 🚀 설치 및 실행

### 1. 프로젝트 클론

```bash
git clone https://github.com/your-username/se-tetris-team7.git
cd se-tetris-team7
```

### 2. 실행 방법

#### Unix/macOS

```bash
./gradlew run
```

#### Windows

```bash
gradlew.bat run
```

### 3. 빌드

```bash
./gradlew build
```

## 📁 프로젝트 구조

```
se-tetris-team7/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── org/
│   │   │   │       ├── tetris/
│   │   │   │       │   ├── App.java              # 메인 애플리케이션
│   │   │   │       │   ├── Launcher.java         # 앱 런처 (패키징용)
│   │   │   │       │   ├── Router.java           # 화면 라우팅
│   │   │   │       │   ├── game/                 # 게임 관련 코드
│   │   │   │       │   │   ├── controller/       # 게임 컨트롤러 (Single, Dual, P2P, Item)
│   │   │   │       │   │   ├── model/            # 게임 모델 (블록, 보드, 아이템, 공격)
│   │   │   │       │   │   ├── engine/           # 게임 엔진 (로컬/P2P)
│   │   │   │       │   │   └── view/             # 게임 뷰 (FXML)
│   │   │   │       │   ├── menu/                 # 메뉴 시스템
│   │   │   │       │   │   ├── setting/          # 설정 메뉴
│   │   │   │       │   │   └── start/            # 시작 메뉴
│   │   │   │       │   ├── network/              # 네트워크 시스템
│   │   │   │       │   │   ├── comand/           # 커맨드 패턴 (Input, Attack, Sync 등)
│   │   │   │       │   │   ├── dto/              # 데이터 전송 객체
│   │   │   │       │   │   └── menu/             # 네트워크 메뉴
│   │   │   │       │   ├── scoreboard/           # 점수판 시스템
│   │   │   │       │   └── shared/               # 공유 컴포넌트 (MVC 베이스 클래스)
│   │   │   │       └── util/                     # 유틸리티 클래스
│   │   │   └── resources/
│   │   │       ├── css/                          # 스타일시트
│   │   │       └── org/tetris/                   # FXML 레이아웃 파일
│   │   └── test/
│   │       └── java/                             # 단위 테스트 (70%+ 커버리지)
│   └── build.gradle                              # 빌드 설정
├── docs/                                         # 문서
│   ├── network_architecture.md                   # 네트워크 아키텍처 설계
│   └── network_protocol_design.md                # 네트워크 프로토콜 설계
├── gradle/                                       # Gradle 래퍼
├── gradlew                                       # Unix용 Gradle 실행 스크립트
├── gradlew.bat                                   # Windows용 Gradle 실행 스크립트
├── settings.gradle                               # Gradle 설정
└── README.md
```

## 🧪 테스트

### 테스트 실행

```bash
./gradlew test
```

### 테스트 커버리지 확인

```bash
./gradlew jacocoTestReport
```

커버리지 리포트는 `app/build/reports/jacoco/test/html/index.html` 에서 확인할 수 있습니다.

### 커버리지 검증 (70%+)

```bash
./gradlew jacocoTestCoverageVerification
```

## 📦 빌드 및 배포

### JAR 파일 생성

```bash
./gradlew jar
```

생성된 JAR 파일은 `app/build/libs/` 디렉토리에서 확인할 수 있습니다.

### 네이티브 패키지 생성

```bash
./gradlew packageApp
```

각 운영체제에 맞는 패키지가 `app/build/dist/` 디렉토리에 생성됩니다.

> **Note**: 빌드 시스템이 자동으로 운영체제를 감지하여 적절한 패키지 타입을 생성합니다.
>
> - **macOS**: DMG 파일 (`tetris-1.0.0.dmg`)
> - **Windows**: EXE 파일 (`tetris-1.0.0.exe`)
> - **Linux**: DEB 파일 (`tetris-1.0.0.deb`)

## 🎮 게임 조작법

### 기본 키 설정 (Player 1)

- **←/→**: 좌우 이동
- **↓**: 소프트 드롭
- **Space**: 하드 드롭
- **↑**: 시계방향 회전
- **P**: 일시정지/메뉴로 돌아가기

### 멀티플레이어 키 설정 (Player 2)

- **A/D**: 좌우 이동
- **S**: 소프트 드롭
- **Shift**: 하드 드롭
- **W**: 시계방향 회전

키 설정은 설정 메뉴에서 각 플레이어별로 변경 가능합니다.

## 🏗 아키텍처

프로젝트는 **MVC (Model-View-Controller)** 패턴을 따릅니다:

- **Model**: 게임 로직, 데이터 관리
- **View**: FXML 기반 UI 레이아웃, CSS 스타일링
- **Controller**: 사용자 입력 처리, Model과 View 연결

주요 디자인 패턴:

- **Factory Pattern**: MvcFactory를 통한 MVC 컴포넌트 생성
- **Router Pattern**: 화면 전환 관리
- **Observer Pattern**: Model-View 간 데이터 바인딩
- **Command Pattern**: 네트워크 통신을 위한 커맨드 객체 패턴
- **Strategy Pattern**: 게임 엔진 전략 (로컬/P2P)

## 📜 버전 히스토리

### v2.0.0 (2024-12-04) - 현재

**주요 기능 추가:**
- ✨ P2P 네트워크 대전 모드 구현
- ✨ 타임어택 모드 추가
- ✨ 로컬 멀티플레이어 (듀얼) 모드 추가
- ✨ 공격 시스템 (라인 클리어 시 상대방에게 방해 블록 전송)
- ✨ Ping 모니터링 및 네트워크 상태 표시

**개선 사항:**
- 🎨 하드 드롭, 라인 클리어 이펙트 애니메이션 추가
- 🎨 게임 UI/UX 개선 (NextBlock, Pause, Canvas 정렬)
- ⚡ 게임 루프 최적화
- 🔧 크로스 플랫폼 패키징 지원 (macOS, Windows, Linux)
- 🔧 플레이어별 독립 키 설정 지원

**버그 수정:**
- 🐛 보드 크기가 창 크기에 대응되게 수정
- 🐛 무게추 아이템 버그 수정
- 🐛 P2P 게임 동기화 관련 버그 수정

**기술적 변경:**
- 📝 네트워크 커맨드 패턴 도입
- 📝 게임 엔진 리팩토링 (GameEngine, P2PGameEngine)
- 📝 듀얼 게임 컨트롤러 구조 개선
- 📝 테스트 커버리지 70% 이상 유지

### v1.0.0 (2024-05-XX)

**초기 릴리즈:**
- 기본 테트리스 게임 구현
- 아이템 모드 (5종 아이템)
- 스코어보드 시스템
- 설정 메뉴 (난이도, 키 설정, 화면 크기)
- MVC 아키텍처 적용

## 📄 라이선스

이 프로젝트는 MIT 라이선스 하에 배포됩니다. 자세한 내용은 [LICENSE](LICENSE) 파일을 참조하세요.

---

## 🙏 감사의 말

이 프로젝트는 소프트웨어공학 수업의 일환으로 제작되었습니다.

**Developed with ❤️ by Team 7**
