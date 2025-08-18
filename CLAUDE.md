
# Noti Service - 개발 가이드라인 (Java Ver.)

## 🚨 필독 - 로컬 빌드 전 반드시 확인!

### ⚠️ Docker 빌드 표준 준수 필수
**이 서비스를 빌드하기 전에 반드시 [Docker 빌드 표준 문서](../core-platform/docs/development/DOCKER_BUILD_STANDARDS.md)를 읽고 따르세요.**

### 🔨 Noti Service 빌드 방법
```bash
# Noti Service 빌드 (테스트 포함 - 필수)
cd ~/asyncsite/noti-service
./gradlew dockerRebuildAndRunNotiOnly
```

**절대 금지사항:**
- ❌ `./gradlew build -x test` (테스트 스킵 금지)
- ❌ `docker build/run` 수동 실행 금지
- ❌ 테스트 실패 무시하고 진행 금지

테스트가 실패하면 **반드시 테스트를 통과시킨 후** 빌드하세요.

---

## 1\. 프로젝트 개요

본 프로젝트는 Spring Cloud 기반의 MSA(Microservices Architecture) 코어 플랫폼입니다. MSA 구현에 필요한 기반 환경을 제공하며, 각 서비스는 독립적으로 개발 및 배포될 수 있습니다.

- **제공 기능**:
  - API Gateway (Spring Cloud Gateway)
  - Service Discovery (Eureka Server)
  - 통합 보안 인프라 (Spring Security + OAuth2)
  - 공통 라이브러리 및 유틸리티
- **구현 서비스 예시**:
  - `noti-service`: 알림 발송(메일 발송, SNS발송, 문자 발송), 조회, 관리

## 2\. 기술 스택

- **언어**: Java 21
- **프레임워크**: Spring Boot 3.x, Spring Cloud
- **빌드 도구**: Gradle (Groovy DSL)
- **Service Discovery**: Netflix Eureka
- **API Gateway**: Spring Cloud Gateway
- **보안**: Spring Security, OAuth2
- **컨테이너**: Docker, Docker Compose
- **테스트**: JUnit 5, Mockito

## 3\. 코딩 컨벤션

### 3.1. Java 스타일

- 모던 Java 관용구와 컨벤션을 따릅니다.
- \*\*불변성(Immutability)\*\*을 적극적으로 활용합니다. (`final` 키워드 사용)
- `Optional<T>`을 사용하여 Null을 안전하게 다룹니다.
- Google Java Style Guide를 따르는 것을 권장합니다.

### 3.2. 패키지 구조

`com.asyncsite.coreplatform.{module}.{layer}`

- `module`: `gateway`, `noti-service` 등
- `layer`: `adapter.in.web` (컨트롤러), `application.service` (서비스), `adapter.out.persistence` (리포지토리), `config`, `domain` 등 (Clean Architecture 기준)

### 3.3. 네이밍 컨벤션

- **클래스**: `PascalCase` (e.g., `NotiService`, `SecurityConfig`)
- **메서드**: `camelCase` (e.g., `getNotiById()`)
- **상수**: `UPPER_SNAKE_CASE` (e.g., `DEFAULT_TIMEOUT`)
- **패키지**: `lowercase` (e.g., `com.asyncsite.coreplatform`)

### 3.4. Spring Boot 컨벤션

- 생성자 주입(Constructor Injection)을 사용합니다. (`@Autowired` 필드 주입 지양)
- 설정 클래스는 `config` 패키지에 위치시킵니다.
- 외부 설정은 `@ConfigurationProperties`를 사용합니다.
- 프로필: `local`, `dev`, `staging`, `prod`

### 3.5. API 설계

- RESTful 엔드포인트를 지향합니다.
- 적절한 HTTP 상태 코드를 사용합니다.
- 일관된 에러 응답 형식을 유지합니다.
- 필요시 API 버전을 관리합니다. (`/api/v1/`)

### 3.6. 보안 가이드라인

- **비밀 정보(Secrets)를 절대 커밋하지 않습니다.**
- 민감한 데이터는 환경 변수를 사용합니다.
- 적절한 인증/인가를 구현합니다.
- OAuth2 모범 사례를 따릅니다.

## 4\. 고급 컨벤션 및 모범 사례

### 4.1. 일반 원칙

- **컴파일러가 아닌 사람을 위한 코드 작성**: 코드는 작성되는 것보다 훨씬 많이 읽힙니다. 영리함보다 명확성과 가독성을 우선시하세요.
- **불변성(Immutability) 우선**:
  - 부작용을 줄이고 상태를 추론하기 쉽게 만들기 위해 가능한 `final`을 사용합니다.
  - 메서드의 파라미터와 반환 타입으로 불변 컬렉션(`List.of()`, `Collections.unmodifiableList()`)을 사용하고, 꼭 필요한 경우에만 가변 컬렉션을 노출합니다.
- **원시 타입 집착(Primitive Obsession) 피하기**:
  - `String`, `int` 같은 원시 타입을 도메인 개념을 표현하는 데 남용하지 마세요.
  - `record`나 클래스로 원시 타입을 래핑하세요. (e.g., `String userEmail` 대신 `record Email(String value)`) 이는 타입 안전성을 높이고 도메인을 명확하게 만듭니다.
- **일급 컬렉션(First-Class Collections)**:
  - 컬렉션만을 포함하는 클래스는 그 자체로 클래스로 만드세요. (e.g., `List<Car> cars` 대신 `class Cars { private final List<Car> values; }`)
  - 이를 통해 컬렉션과 관련된 비즈니스 로직(e.g., `findFastestCar()`)을 해당 클래스에 추가할 수 있습니다.

### 4.2. 클래스 및 메서드 설계

- **작고 집중된 단위 (단일 책임 원칙)**:
  - **메서드**: 하나의 기능만 잘 수행해야 합니다. 15줄 미만을 목표로 하고, 길어지면 리팩토링을 고려하세요.
  - **클래스**: 단일 책임을 가져야 합니다. 너무 많은 인스턴스 변수나 메서드는 클래스 분리의 신호입니다.
- **들여쓰기(Indentation) 깊이 제한**:
  - 메서드의 들여쓰기 깊이를 최대 2단계로 유지하도록 노력하세요.
  - 깊은 중첩(`if`/`for`)은 코드 추적을 어렵게 만듭니다. **가드 절(Guard Clause)** 등을 사용해 리팩토링하세요.
  - **예시 (Guard Clause)**:
    ```java
    // 지양
    void process(Noti noti) {
        if (noti != null) {
            if (noti.isActive()) {
                // ... 핵심 로직
            }
        }
    }

    // 권장
    void process(Noti noti) {
        if (noti == null || !noti.isActive()) {
            return;
        }
        // ... 핵심 로직
    }
    ```
- **유틸리티 클래스의 정적 메서드(Static Methods)**: Java에는 최상위 함수가 없으므로, 순수하고 상태 없는 유틸리티 기능은 `final` 클래스의 `private` 생성자와 `static` 메서드로 제공하여 `StringUtils`, `DateUtils` 같은 객체를 불필요하게 생성하는 것을 방지합니다.

### 4.3. 에러 핸들링 및 Null 안전성

- **명시적인 Null 처리**: `Optional<T>`을 의도적으로 사용하여 값이 없을 수 있음을 명시합니다. `Optional.get()`을 `isPresent()` 확인 없이 호출하는 것을 절대적으로 피해야 합니다.
- **비즈니스 에러는 예외 대신 `Result` 패턴 사용**: 예측 가능한 비즈니스 실패(e.g., "사용자 없음", "잔액 부족")에 대해 예외를 던지지 마세요. 대신 `Result` 같은 커스텀 타입을 반환하여 호출자가 성공과 실패 케이스를 명시적으로 처리하도록 강제합니다.
  ```java
  // Java 17+ Sealed Interface
  public sealed interface Result<T, E> {
      record Success<T, E>(T data) implements Result<T, E> {}
      record Failure<T, E>(E error) implements Result<T, E> {}
  }

  Result<User, AppError> findNoti(Long id) { ... }
  ```

### 4.4. 테스트 컨벤션

- **Public 메서드 테스트**: 클래스의 공개적인 동작에 단위 테스트를 집중하세요. Private 메서드는 Public 메서드를 통해 간접적으로 테스트됩니다.
- **BDD 스타일 이름 사용**: Behavior-Driven Development (BDD) 스타일로 테스트의 의도를 명확히 하세요.
  - **예시**: `givenUserIsAdmin_whenDeletingResource_thenResourceIsDeleted()`
- **한 번에 하나만 테스트**: 각 테스트 케이스는 하나의 논리적 결과나 동작만 검증해야 합니다.

## 5\. Git 워크플로우 및 커밋 가이드라인

(이 섹션은 언어에 구애받지 않으므로 기존 가이드라인과 동일하게 적용됩니다.)

### 5.1. 브랜치 네이밍 컨벤션

**`{type}/{ticket-info}/{short-description}`**

- **`{type}`**: `feature`, `fix`, `release`, `hotfix`
- **`{ticket-info}`**: Notion 등 티켓 정보 (e.g., `issue-43`). 티켓이 없으면 `common`.
- **`{short-description}`**: 하이픈(-)으로 연결된 영어 소문자 설명.

**✅ 브랜치 이름 예시:**

- `feature/issue-43/new-login-api`
- `fix/work-12/main-page-css-error`
- `release/issue-43/v1.0.0`

### 5.2. 브랜치 전략

- **`main`**: 최종 프로덕션 코드. `release` 또는 `hotfix` 브랜치만 병합 가능.
- **`deploy-qa`**: 통합 테스트 브랜치. `feature`, `fix` 브랜치를 병합하여 QA 환경에 배포.

### 5.3. 커밋 메시지 컨벤션

**Conventional Commits** 명세를 따릅니다.

- **기본 구조**:
  ```
  <type>: <subject>

  (optional) <body>
  ```
- **주요 타입**: `feat`, `fix`, `docs`, `style`, `refactor`, `test`, `chore`

**✅ 커밋 메시지 예시:**

- `feat: Add social login functionality`
- `fix: Correct null return value when fetching user info`
- `refactor: Extract duplicate authentication logic into a utility function`


## 6. 헥사고날 아키텍처 (Hexagonal Architecture) 가이드라인

우리 프로젝트는 헥사고날 아키텍처(Ports & Adapters)를 따릅니다. 이 아키텍처의 핵심 목표는 **비즈니스 로직(Domain)을 외부 기술(Adapter)로부터 분리**하는 것입니다.
**가장 중요한 원칙**: **모든 의존성은 안쪽으로 향해야 합니다.** (`Adapter` → `Application` → `Domain`)

### 6.1. 패키지 구조 및 역할

다음은 `noti-service`를 예시로 한 표준 패키지 구조입니다.

```
com
└── asyncsite
    └── notiservice
        ├── domain                // 헥사곤의 내부: 순수한 비즈니스 로직
        │   ├── model             // 도메인 모델 (e.g., Noti, NotiSetting)
        │   ├── service           // 여러 도메인 모델에 걸친 복잡한 로직을 처리하는 도메인 서비스
        │   └── port              // 애플리케이션 경계의 인터페이스 (Port)
        │       ├── in            // Input Port: Use Case 인터페이스 (e.g., ProposeNotiUseCase)
        │       └── out           // Output Port: 외부 시스템 연동 인터페이스 (e.g., NotiRepositoryPort)
        │
        ├── application           // 애플리케이션 계층: 유스케이스의 실제 구현
        │   └── service           // Input Port(Use Case) 구현체 (e.g., ProposeNotiService)
        │
        └── adapter               // 헥사곤의 외부: 외부 세계와의 연결
            ├── in                // Driving Adapters (애플리케이션을 호출)
            │   ├── web           // Web Adapter (e.g., NotiController)
            │   └── mapper        // DTO와 도메인 모델 간의 변환 (e.g., NotiDtoMapper)
            └── out               // Driven Adapters (애플리케이션에 의해 호출됨)
                └── persistence   // Persistence Adapter (DB 연동)
                    ├── entity    // JPA Entity (e.g., NotiJpaEntity)
                    ├── repository// Spring Data JPA Repository
                    ├── mapper    // Persistence Entity와 도메인 모델 간의 변환
                    └── NotiPersistenceAdapter.java // Output Port 구현체
```

### 6.2. 계층별 상세 설명

- **`domain` (내부)**:
  - 프레임워크에 대한 의존성이 전혀 없는 순수한 Java 코드 영역입니다.
  - `model`: 시스템의 핵심 비즈니스 규칙과 데이터를 담는 객체입니다.
  - `port`: 애플리케이션의 기능을 정의하는 **'창구'** 역할을 합니다. `in`은 외부에서 내부로 들어오는 요청의 규격(Use Case)을, `out`은 내부에서 외부로 나가는 요청의 규격(Repository, Event Publisher 등)을 정의합니다.

- **`application` (내부)**:
  - `domain` 계층의 `port.in` (Use Case) 인터페이스를 구현합니다.
  - 비즈니스 흐름을 제어하고, `domain` 모델과 `port.out`을 사용하여 실제 작업을 수행합니다. 트랜잭션 경계가 설정되는 곳입니다.

- **`adapter` (외부)**:
  - 외부 기술과 내부 로직을 연결하는 **'접착제'** 역할을 합니다.
  - `in`: 외부의 요청(e.g., HTTP Request)을 받아 내부의 `application` 계층(Use Case)을 호출합니다.
  - `out`: `domain` 계층에 정의된 `port.out` 인터페이스를 구현하여, 특정 기술(e.g., JPA, Kafka)을 통해 데이터를 저장하거나 메시지를 발행합니다.

## 7\. AI 어시스턴트 중요 참고사항

### 🚨 Spring Boot 3.2+ Nested JAR 이슈 - 필독!

#### 문제 설명
Spring Boot 3.2부터 JAR 파일 포맷이 변경되어 Thymeleaf 템플릿 로딩이 실패할 수 있습니다:
- **증상**: 로컬에서는 작동하지만 Docker/서버에서 `FileNotFoundException: templates/email.html`
- **원인**: `jar:nested:` 프로토콜을 SpringResourceTemplateResolver가 처리 못함

#### 해결책
**반드시 ClassLoaderTemplateResolver 사용**:
```java
@Bean
public ClassLoaderTemplateResolver templateResolver() {
    ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
    resolver.setPrefix("templates/");  // "classpath:" 없이!
    resolver.setSuffix(".html");
    return resolver;
}
```

#### 검증 방법
```bash
# 반드시 Docker 환경에서 테스트
./gradlew clean bootJar
docker build -t test-service .
docker run test-service
```

### CI/CD 환경변수 주의사항

#### 문제
빈 GitHub Secrets가 환경변수로 설정되면 yml 기본값을 덮어씀:
```yaml
# 잘못된 예시 - 빈 secret이 ""로 설정됨
- MAIL_USERNAME=${{ secrets.MAIL_USERNAME }}  # 빈 값이면 yml 기본값 무시됨
```

#### 해결책
필수 환경변수만 CI/CD에 설정하고, 선택적 값은 yml에서 관리:
```yaml
# application-docker.yml
spring.mail.username: ${MAIL_USERNAME:default@example.com}  # 기본값 제공
```

### 일반 가이드라인

1.  **기존 코드 패턴 확인**: 새 기능 구현 전, 기존 코드의 패턴과 일관성을 유지하세요.
2.  **클린 아키텍처 준수**: 모든 기능은 Ports and Adapters 패턴에 따라 설계 및 구현해야 합니다. **Domain/Application 계층은 절대로 Adapter 계층에 의존해서는 안 됩니다.**
3.  **전문적인 익명 커밋 로그 유지**: 커밋 메시지에 "Claude", "AI", "assistant" 등 AI 자신을 나타내는 단어를 절대 포함하지 마세요. 모든 커밋은 팀의 인간 개발자가 작성한 것처럼 전문적이어야 합니다.
4.  **독립 실행성 보장**: 모든 모듈은 독립적으로 실행 가능해야 합니다.
5.  **의존성 관리**: 의존성 추가 시 호환성 매트릭스를 확인하고 신중하게 추가하세요.
6.  **로컬과 서버 환경 차이 주의**: 항상 Docker 환경에서 최종 테스트 수행