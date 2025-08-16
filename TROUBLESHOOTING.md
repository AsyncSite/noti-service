# Noti Service Troubleshooting Guide

## 🚨 핵심 이슈: Spring Boot 3.2+ Nested JAR 문제

### Spring Boot 3.2+ JAR 구조 변경으로 인한 Thymeleaf 템플릿 로딩 실패

#### 증상
- **로컬 환경(IDE)**: 이메일 발송 정상 작동 ✅
- **로컬 Docker(bootJar)**: `FileNotFoundException: class path resource [templates/email.html] cannot be opened` ❌
- **서버 배포**: 동일한 템플릿 로딩 오류 발생 ❌
- 오류 메시지: `Error resolving template [email], template might not exist`

#### 근본 원인
Spring Boot 3.2부터 executable JAR 포맷이 변경됨:
- **이전**: `jar:file:/app.jar!/BOOT-INF/classes/templates/email.html`
- **3.2+**: `jar:nested:/app.jar/!BOOT-INF/classes/!/templates/email.html`

`SpringResourceTemplateResolver`가 nested JAR 프로토콜을 처리하지 못함!

#### ✅ 해결책: ClassLoaderTemplateResolver 사용

`ThymeleafConfig.java`:
```java
@Configuration
public class ThymeleafConfig {
    
    @Bean
    public ClassLoaderTemplateResolver templateResolver() {
        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        // 중요: "classpath:" 없이 상대 경로 사용!
        templateResolver.setPrefix("templates/");  
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateResolver.setCharacterEncoding("UTF-8");
        return templateResolver;
    }
    
    @Bean
    public SpringTemplateEngine templateEngine(ClassLoaderTemplateResolver templateResolver) {
        SpringTemplateEngine templateEngine = new SpringTemplateEngine();
        templateEngine.setTemplateResolver(templateResolver);
        return templateEngine;
    }
}
```

#### ❌ 실패한 시도들 (시간 낭비 주의)
1. **SpringResourceTemplateResolver**: nested JAR 지원 안 함
2. **checkExistence(false)**: 문제 해결 안 됨
3. **Spring Boot Loader CLASSIC**: 불필요한 복잡성 추가

#### 예방 방법
1. **개발 초기부터 Docker 환경에서 테스트**
2. **bootJar로 빌드한 JAR 파일을 직접 실행해서 검증**
3. **ClassLoaderTemplateResolver를 기본으로 사용**

---

## 문제 해결 가이드

### 1. MySQL Character Set 문제 (한글 깨짐)

#### 증상
- 로컬 환경에서는 이메일 발송이 정상 작동하지만 서버에서는 실패
- Thymeleaf 템플릿 에러: `Error resolving template [email], template might not exist`
- 데이터베이스 조회 시 한글이 `???`로 표시됨
- 로그: `Exception processing template "email": Error resolving template [email]`

#### 원인
서버 MySQL의 character set이 `latin1`로 설정되어 있어 한글 데이터가 손상됨:
```sql
-- 문제가 되는 설정
character_set_client    | latin1
character_set_connection| latin1  
character_set_results   | latin1
```

한글 텍스트가 `???`로 저장되면 Thymeleaf 템플릿 파싱이 실패하게 됨.

#### 해결 방법

##### 1. JDBC URL에 Character Encoding 파라미터 추가
`application-docker.yml`:
```yaml
spring:
  datasource:
    url: jdbc:mysql://asyncsite-mysql:3306/notidb?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&characterEncoding=utf8&useUnicode=true&createDatabaseIfNotExist=true
```

##### 2. CI/CD 파이프라인 설정 확인
`.github/workflows/ci-cd.yml`:
```yaml
- SPRING_DATASOURCE_URL=jdbc:mysql://asyncsite-mysql:3306/notidb?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&characterEncoding=utf8&useUnicode=true&createDatabaseIfNotExist=true
```

##### 3. JVM 인코딩 옵션 추가
`Dockerfile`:
```dockerfile
ENV JAVA_OPTS="-Xmx512m -Xms256m -Dfile.encoding=UTF-8 -Dsun.jnu.encoding=UTF-8"
```

##### 4. 자동 복구 로직 구현
`SystemTemplateInitializer`에 charset 손상 감지 및 자동 복구 기능 추가:
```java
private boolean isCharsetCorrupted(NotificationTemplate template) {
    // 연속된 '?' 문자 감지 (한글이 깨진 경우)
    String corruptedPattern = "\\?{2,}";
    boolean titleCorrupted = title != null && title.matches(".*" + corruptedPattern + ".*");
    boolean contentCorrupted = content != null && content.matches(".*" + corruptedPattern + ".*");
    
    // 단일 '?'가 비정상적으로 많은 경우도 감지
    boolean titleSuspicious = title != null && countCharOccurrences(title, '?') > 3;
    boolean contentSuspicious = content != null && countCharOccurrences(content, '?') > 5;
    
    return titleCorrupted || contentCorrupted || titleSuspicious || contentSuspicious;
}
```

##### 5. MySQL 서버 설정 (선택사항)
서버에 직접 접근 가능한 경우:
```sql
-- MySQL 설정 확인
SHOW VARIABLES LIKE 'character_set%';

-- 세션 레벨에서 임시 변경
SET NAMES 'utf8mb4';

-- 영구 설정 (my.cnf 또는 custom.cnf)
[mysqld]
character-set-server=utf8mb4
collation-server=utf8mb4_unicode_ci

[client]
default-character-set=utf8mb4
```

#### 예방 방법
1. **개발 초기부터 JDBC URL에 charset 파라미터 포함**
2. **모든 환경(local, docker, prod)에서 동일한 charset 설정 유지**
3. **SystemTemplateInitializer에 charset 검증 로직 포함**
4. **배포 후 첫 실행 시 로그 확인하여 템플릿 초기화 정상 여부 검증**

#### 디버깅 명령어
```bash
# 로컬 Docker 로그 확인
docker logs asyncsite-noti-service --tail 100

# 서버 로그 확인 (SSH 접속 후)
ssh -p 2222 user@server
docker logs asyncsite-noti-service --tail 100 | grep -i "template\|charset"

# 데이터베이스 직접 확인
docker exec -it asyncsite-mysql mysql -uroot -p
USE notidb;
SELECT template_id, LEFT(content_template, 50) FROM notification_template;
-- 한글이 ???로 표시되면 charset 문제

# charset 설정 확인
SHOW VARIABLES LIKE 'character_set%';
SHOW VARIABLES LIKE 'collation%';
```

---

### 2. Thymeleaf 템플릿 로딩 실패

#### 증상
- `Error resolving template [email]` 에러 발생
- 템플릿 파일은 존재하지만 찾지 못함

#### 원인
1. 템플릿 파일이 JAR에 포함되지 않음
2. 클래스패스 설정 오류
3. 템플릿 이름과 파일명 불일치

#### 해결 방법
1. **JAR 파일 내용 확인**:
```bash
jar -tf build/libs/noti-service-*.jar | grep templates
```

2. **명시적 Thymeleaf 설정 추가** (`ThymeleafConfig.java`):
```java
@Bean
public SpringResourceTemplateResolver templateResolver(ApplicationContext applicationContext) {
    SpringResourceTemplateResolver templateResolver = new SpringResourceTemplateResolver();
    templateResolver.setPrefix("classpath:/templates/");
    templateResolver.setSuffix(".html");
    templateResolver.setTemplateMode(TemplateMode.HTML);
    templateResolver.setCharacterEncoding("UTF-8");
    templateResolver.setCheckExistence(true);
    return templateResolver;
}
```

---

### 3. CI/CD 환경변수 오버라이드 문제

#### 증상
- 이메일 발송 시 `jakarta.mail.internet.AddressException: From address is empty` 오류
- application-docker.yml에 기본값이 설정되어 있음에도 빈 값으로 처리됨

#### 원인
CI/CD 파이프라인에서 빈 GitHub Secrets가 환경변수로 설정되어 yml 기본값을 덮어씀:
```yaml
# 문제가 되는 설정
- SPRING_MAIL_USERNAME=${{ secrets.MAIL_USERNAME }}  # 빈 값이면 ""로 오버라이드
- APPLICATION_NOTIFICATION_EMAIL_FROM_ADDRESS=${{ secrets.MAIL_FROM }}  # 빈 값
```

#### 해결책
빈 환경변수를 CI/CD에서 제거하여 application.yml의 기본값이 사용되도록 함:
```yaml
# .github/workflows/ci-cd.yml
environment:
  - SPRING_PROFILES_ACTIVE=docker
  - EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE=...
  # 메일 관련 환경변수 제거! yml 기본값 사용
```

#### 교훈
- **빈 환경변수는 null이 아닌 빈 문자열("")로 설정됨**
- **Spring Boot는 빈 문자열도 유효한 값으로 처리하여 yml 기본값을 무시**
- **CI/CD 환경변수는 필수 값만 설정하고, 선택적 값은 yml에서 관리**

---

### 4. 이메일 발송 실패

#### 증상
- SMTP 연결 실패
- 인증 오류
- 타임아웃

#### 해결 방법
1. **환경 변수 확인**:
```bash
docker exec asyncsite-noti-service env | grep MAIL
```

2. **SMTP 설정 검증**:
```yaml
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
          connection-timeout: 5000
          timeout: 5000
```

3. **Gmail 앱 비밀번호 사용** (2단계 인증 활성화 필수)

---

### 4. 로그 분석 팁

#### 중요 로그 패턴
```bash
# 템플릿 초기화 확인
grep "시스템 템플릿 초기화" /app/logs/noti-service.log

# charset 문제 감지
grep "템플릿 문자셋 손상 감지" /app/logs/noti-service.log

# 이메일 발송 추적
grep "알림 발송 요청" /app/logs/noti-service.log
```

#### 디버그 로깅 활성화
`application.yml`:
```yaml
logging:
  level:
    com.asyncsite.notiservice: DEBUG
    org.thymeleaf: DEBUG
    org.springframework.mail: DEBUG
```

---

## 문제 발생 시 체크리스트

### 배포 후 검증
- [ ] 템플릿 초기화 로그 확인 (`시스템 템플릿 초기화 완료`)
- [ ] 데이터베이스 한글 데이터 정상 표시 확인
- [ ] 테스트 이메일 발송 성공 확인
- [ ] Health check 엔드포인트 정상 응답

### 문제 발생 시
1. [ ] Docker 컨테이너 재시작
2. [ ] 데이터베이스 charset 설정 확인
3. [ ] 환경 변수 정상 설정 확인
4. [ ] 네트워크 연결 상태 확인
5. [ ] 로그 레벨을 DEBUG로 변경하여 상세 로그 확인

---

## 연락처
문제가 지속되는 경우 다음 채널로 문의:
- Discord: AsyncSite 개발 채널
- GitHub Issues: https://github.com/AsyncSite/noti-service/issues