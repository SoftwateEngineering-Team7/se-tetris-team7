# Team7 Tetris 🎮

JavaFX 기반의 모던한 테트리스 게임입니다. MVC 아키텍처 패턴을 적용하여 구현하였으며, 다양한 게임 모드와 아이템 시스템을 제공합니다.

## 📋 목차

- [팀원](#-팀원)
- [주요 기능](#-주요-기능)
- [기술 스택](#-기술-스택)
- [시스템 요구사항](#-시스템-요구사항)
- [설치 및 실행](#-설치-및-실행)
- [프로젝트 구조](#-프로젝트-구조)
- [테스트](#-테스트)
- [빌드 및 배포](#-빌드-및-배포)
- [라이선스](#-라이선스)

## 👥 팀원

| 이름   | GitHub                               |
| ------ | ------------------------------------ |
| 강형석 | [@강형석](https://github.com/강형석) |
| 김대규 | [@김대규](https://github.com/김대규) |
| 김민석 | [@김민석](https://github.com/김민석) |
| 박재홍 | [@박재홍](https://github.com/박재홍) |

## ✨ 주요 기능

### 게임 모드

- **일반 모드**: 클래식 테트리스 게임 플레이
- **아이템 모드**: 특수 아이템이 추가된 테트리스

### 아이템 시스템

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
- 다양한 난이도 설정
- 키 레이아웃 설정
- 화면 크기 프리셋

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
│   │   │   │       │   ├── Router.java           # 화면 라우팅
│   │   │   │       │   ├── game/                 # 게임 관련 코드
│   │   │   │       │   │   ├── controller/       # 게임 컨트롤러
│   │   │   │       │   │   ├── model/            # 게임 모델 (블록, 보드, 아이템)
│   │   │   │       │   │   └── view/             # 게임 뷰 (FXML)
│   │   │   │       │   ├── menu/                 # 메뉴 시스템
│   │   │   │       │   │   ├── setting/          # 설정 메뉴
│   │   │   │       │   │   └── start/            # 시작 메뉴
│   │   │   │       │   ├── scoreboard/           # 점수판 시스템
│   │   │   │       │   └── shared/               # 공유 컴포넌트 (MVC 베이스 클래스)
│   │   │   │       └── util/                     # 유틸리티 클래스
│   │   │   └── resources/
│   │   │       ├── css/                           # 스타일시트
│   │   │       ├── fxml/                          # FXML 레이아웃 파일
│   │   │       └── tetris.icns                    # 앱 아이콘
│   │   └── test/
│   │       └── java/                              # 단위 테스트
│   └── build.gradle                               # 빌드 설정
├── gradle/                                        # Gradle 래퍼
├── gradlew                                        # Unix용 Gradle 실행 스크립트
├── gradlew.bat                                    # Windows용 Gradle 실행 스크립트
├── settings.gradle                                # Gradle 설정
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

### 커버리지 검증 (73%)

```bash
./gradlew jacocoTestCoverageVerification
```

## 📦 빌드 및 배포

### JAR 파일 생성

```bash
./gradlew jar
```

생성된 JAR 파일은 `app/build/libs/` 디렉토리에서 확인할 수 있습니다.

### 네이티브 패키지 생성 (macOS)

```bash
./gradlew packageApp
```

생성된 DMG 파일은 `app/build/dist/` 디렉토리에서 확인할 수 있습니다.

> **Note**: 다른 운영체제용 패키지를 생성하려면 `build.gradle` 파일의 `packageApp` 태스크에서 `--type` 옵션을 변경하세요.
>
> - macOS: `dmg` 또는 `pkg`
> - Windows: `exe` 또는 `msi`
> - Linux: `deb` 또는 `rpm`

## 🎮 게임 조작법

### 기본 키 설정

- **←/→**: 좌우 이동
- **↓**: 소프트 드롭
- **Space**: 하드 드롭
- **↑**: 시계방향 회전
- **C**: 블록 홀드(3차 때 구현 예정)
- **P**: 일시정지/메뉴로 돌아가기

키 설정은 설정 메뉴에서 변경 가능합니다.

## 🏗 아키텍처

프로젝트는 **MVC (Model-View-Controller)** 패턴을 따릅니다:

- **Model**: 게임 로직, 데이터 관리
- **View**: FXML 기반 UI 레이아웃, CSS 스타일링
- **Controller**: 사용자 입력 처리, Model과 View 연결

주요 디자인 패턴:

- **Factory Pattern**: MvcFactory를 통한 MVC 컴포넌트 생성
- **Router Pattern**: 화면 전환 관리
- **Observer Pattern**: Model-View 간 데이터 바인딩

## 📄 라이선스

이 프로젝트는 MIT 라이선스 하에 배포됩니다. 자세한 내용은 [LICENSE](LICENSE) 파일을 참조하세요.

---

## 🙏 감사의 말

이 프로젝트는 소프트웨어공학 수업의 일환으로 제작되었습니다.

**Developed with ❤️ by Team 7**
