# LoL Tierpeek — League of Legends Friends Tracker

친구 6명의 랭크 점수(LP) 그래프와 전적을 **한 화면**에서 보는 League of Legends 전적 트래커 백엔드.

## 빠른 시작

### 필수 조건
- Java 21+
- Docker & Docker Compose
- Riot API Key (Personal)

### 로컬 개발 실행

```bash
# 1. 환경변수 설정
cp .env.example .env
# .env 파일을 편집해 RIOT_API_KEY 입력

# 2. 인프라 기동 (PostgreSQL + Redis)
docker-compose up -d postgres redis

# 3. 애플리케이션 실행
./gradlew bootRun
```

서버는 `http://localhost:8080`에서 실행됩니다.

### 주요 엔드포인트

- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **대시보드**: `GET /api/dashboard`
- **친구 등록**: `POST /api/friends`
- **헬스체크**: `GET /actuator/health`

## 개발 가이드

자세한 개발 가이드는 `CLAUDE.md`를 참고하세요.

- 아키텍처 및 패키지 구조
- Riot API 연동 규칙
- 데이터베이스 스키마
- 코딩 컨벤션
- 테스트 전략

## 빌드 & 테스트

```bash
# 전체 빌드
./gradlew clean build

# 테스트만 실행
./gradlew test

# 특정 테스트 실행
./gradlew test --tests ClassName.methodName
```

## 기술 스택

- **Framework**: Spring Boot 4.0.6
- **Language**: Java 21
- **Database**: PostgreSQL
- **Cache**: Redis
- **Build**: Gradle
- **Test**: JUnit 5 + Mockito
- **Docs**: Springdoc OpenAPI (Swagger)

## 프로젝트 구조

```
src/main/java/com/loltracker/
├── config/           # Spring 설정
├── controller/       # HTTP 엔드포인트
├── service/          # 비즈니스 로직
├── client/           # 외부 API 클라이언트
├── scheduler/        # 스케줄된 작업
├── repository/       # 데이터 접근
├── entity/           # JPA 엔티티
├── dto/              # 데이터 전송 객체
├── util/             # 유틸리티
└── exception/        # 커스텀 예외
```

## 라이선스

MIT