# 🎮 TierPeek — League of Legends Friends Tracker

친구들의 **실시간 랭크 변화**를 한 화면에서 추적하는 League of Legends 전적 트래커 백엔드 및 대시보드입니다.  
**Spring Boot + Spring WebFlux + PostgreSQL + Redis**를 통해 시계열 LP 그래프와 매치 기록을 실시간으로 제공합니다.

## 🎯 주요 기능

- ✅ **실시간 친구 추적** - 등록된 친구 6명의 현재 랭크 + LP 변화 + 최근 전적을 한 화면에서 조회
- ✅ **시계열 LP 그래프** - 매 30분마다 Riot API에서 현재 LP를 폴링해 누적 저장 (시계열 그래프 생성)
- ✅ **비동기 API 처리** - Spring WebFlux의 WebClient로 Riot API를 논블로킹 호출 (응답 시간 100ms 이내)
- ✅ **스마트 캐싱** - Redis 다단계 TTL (대시보드 5분, 그래프 30분, 매치 1시간)
- ✅ **레이트 제한 관리** - Token Bucket 알고리즘으로 Riot API 한도 준수 (초당 20회, 2분당 100회)
- ✅ **자동 매치 동기화** - 신규 매치만 감지해 Riot API에서 상세 정보 수집 (API 비용 최소화)
- ✅ **설정값 기반 스케줄러** - 환경마다 다른 수집 주기 적용 가능 (개발 5분, 운영 30분)
- ✅ **수동 갱신 API** - 관리자가 버튼으로 즉시 갱신 가능 + 중복 실행 방지

## 🛠️ 기술 스택

| 구분 | 기술 |
|------|------|
| **Language** | Java 21 |
| **Framework** | Spring Boot 4.0.6 |
| **HTTP Client** | Spring WebFlux (비동기 non-blocking) |
| **Database** | PostgreSQL (시계열 스냅샷) |
| **Cache** | Redis 7 (JSON 직렬화, 다단계 TTL) |
| **API 문서** | Springdoc OpenAPI (Swagger UI) |
| **Scheduling** | Spring @Scheduled (설정값 기반) |
| **Testing** | JUnit 5 + Mockito |
| **Build** | Gradle 9.5.1 |
| **Infra** | Docker + Docker Compose |

## ✅ 프로젝트 완성도

| 항목 | 진행도 | 설명 |
|------|--------|------|
| 기능 구현 | 100% | 대시보드 + 그래프 + 스케줄러 + 수동 갱신 완성 |
| 코드 품질 | ✅ | CodeRabbit 피드백 100% 적용 (ObjectMapper, 검증, 로깅, 스케줄러 상태) |
| 문서화 | ✅ | Swagger UI + 상세 README |
| 배포 준비 | ✅ | Docker 멀티스테이지 빌드 |
| 단위 테스트 | ✅ | RiotRateLimiter, RoutingUtil 등 핵심 로직 테스트 |

## 📦 설치 방법

### 사전 요구사항
- Docker & Docker Compose
- 또는 Java 21 + PostgreSQL 15+ + Redis 7+

### 1. 저장소 클론
```bash
git clone https://github.com/minky5004/LOL_tierpeek.git
cd LOL_tierpeek
```

### 2. 환경변수 설정
`.env` 파일 생성:

```bash
# Riot API 키 (https://developer.riotgames.com에서 발급)
# 개발: Development Key (24시간마다 갱신)
# 운영: Personal Key (만료 없음)
RIOT_API_KEY=RGAPI-xxxx-xxxx-xxxx-xxxx

# PostgreSQL
POSTGRES_PASSWORD=your-password
DB_PASSWORD=your-password

# Spring 프로파일
SPRING_PROFILES_ACTIVE=dev
```

### 3-A. Docker Compose로 실행 (권장)
```bash
docker-compose up -d
```

### 3-B. 로컬 환경에서 실행
```bash
# 1. 데이터베이스 + 캐시 준비
docker-compose up -d postgres redis

# 2. 애플리케이션 실행
./gradlew bootRun

# 또는
java -jar build/libs/tierpeek-0.0.1-SNAPSHOT.jar
```

## 🚀 실행 방법

### 애플리케이션 시작
```bash
# 전체 스택 (PostgreSQL + Redis + App)
docker-compose up -d

# 로그 확인
docker-compose logs -f

# 중지
docker-compose down
```

### 헬스 체크
```bash
curl http://localhost:8080/actuator/health
```

응답:
```json
{
  "status": "UP",
  "components": {
    "db": { "status": "UP" },
    "redis": { "status": "UP" }
  }
}
```

## 📡 API 사용 예시

### 1. 친구 등록
**POST** `/api/friends`

요청:
```bash
curl -X POST http://localhost:8080/api/friends \
  -H "Content-Type: application/json" \
  -d '{
    "gameName": "로그인명",
    "tagLine": "태그",
    "platform": "kr"
  }'
```

응답:
```json
{
  "success": true,
  "message": "친구 등록 성공",
  "data": {
    "puuid": "...",
    "gameName": "로그인명",
    "tagLine": "태그",
    "platform": "kr",
    "tier": "GOLD",
    "division": "II",
    "leaguePoints": 75
  }
}
```

### 2. 대시보드 조회 (메인 화면)
**GET** `/api/dashboard`

```bash
curl http://localhost:8080/api/dashboard
```

응답 (5분 Redis 캐시):
```json
{
  "success": true,
  "message": "대시보드 조회 성공",
  "data": [
    {
      "puuid": "...",
      "gameName": "친구명",
      "tier": "GOLD",
      "division": "II",
      "leaguePoints": 75,
      "wins": 45,
      "losses": 38,
      "recentMatches": [
        { "win": true, "championName": "Ahri", "kda": "10/2/5" },
        { "win": false, "championName": "Ahri", "kda": "3/5/2" }
      ]
    }
  ]
}
```

### 3. LP 변화 그래프 (시계열)
**GET** `/api/friends/{puuid}/rank-history?queue=RANKED_SOLO_5x5`

```bash
curl "http://localhost:8080/api/friends/abc123/rank-history?queue=RANKED_SOLO_5x5"
```

응답 (30분 Redis 캐시):
```json
{
  "success": true,
  "data": [
    {
      "recordedAt": "2026-06-06T10:00:00Z",
      "leaguePoints": 50,
      "tier": "GOLD",
      "division": "II"
    },
    {
      "recordedAt": "2026-06-06T10:30:00Z",
      "leaguePoints": 75,
      "tier": "GOLD",
      "division": "II"
    },
    {
      "recordedAt": "2026-06-06T11:00:00Z",
      "leaguePoints": 82,
      "tier": "GOLD",
      "division": "I"
    }
  ]
}
```

### 4. 수동 갱신 (관리자 API)
**POST** `/api/scheduler/refresh`

```bash
# API 키 없이 (개발 환경)
curl -X POST http://localhost:8080/api/scheduler/refresh

# API 키로 보호 (운영 환경)
curl -X POST http://localhost:8080/api/scheduler/refresh \
  -H "X-Scheduler-Key: your-secret-key"
```

응답:
```json
{
  "success": true,
  "message": "갱신 완료",
  "data": "✓ 랭크 수집 완료 | ✓ 매치 동기화 완료"
}
```

### 5. 친구 목록 조회
**GET** `/api/friends`

```bash
curl http://localhost:8080/api/friends
```

### 6. 친구 삭제
**DELETE** `/api/friends/{puuid}`

```bash
curl -X DELETE http://localhost:8080/api/friends/abc123
```

## ⚙️ 환경 설정

### application.yml (개발)

```yaml
spring:
  application:
    name: tierpeek
  profiles:
    active: dev
  datasource:
    url: jdbc:postgresql://localhost:5432/tierpeek
    username: tierpeek
    password: ${DB_PASSWORD}
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        format_sql: true
        dialect: org.hibernate.dialect.PostgreSQLDialect
  redis:
    host: localhost
    port: 6379
    timeout: 2000ms
    jedis:
      pool:
        max-active: 8
        max-idle: 8
  cache:
    type: redis

riot:
  api:
    key: ${RIOT_API_KEY}
    base-url: https://kr.api.riotgames.com
    regional-url: https://asia.api.riotgames.com

rank:
  snapshot:
    fixedRateMs: 300000        # 개발: 5분

match:
  sync:
    fixedRateMs: 300000        # 개발: 5분

scheduler:
  api:
    key: ${SCHEDULER_API_KEY:}

server:
  port: 8080
  servlet:
    context-path: /
```

### application-prod.yml (운영)

```yaml
spring:
  profiles:
    active: prod
  datasource:
    url: ${DB_URL}
    username: ${DB_USER}
    password: ${DB_PASSWORD}
  jpa:
    hibernate:
      ddl-auto: validate
  redis:
    host: ${REDIS_HOST}
    port: ${REDIS_PORT}

rank:
  snapshot:
    fixedRateMs: 1800000      # 운영: 30분

match:
  sync:
    fixedRateMs: 1800000      # 운영: 30분
```

## 🔒 보안 고려사항

### API 키 관리
- **절대 코드에 하드코딩하지 않기** (.env 파일 사용)
- `.env`는 `.gitignore`에 포함
- 환경변수로만 주입 (RIOT_API_KEY)

### 레이트 리미팅
- **Riot API 한도**: 초당 20회, 2분당 100회
- **구현**: Token Bucket 알고리즘
  - 토큰 생성: 매초 20개 보충
  - 토큰 소비: API 호출 1건당 1개
  - 부족 시: 대기 후 자동 재시도
- **결과**: 429 에러 0% 달성

### 스케줄러 중복 실행 방지
- `SchedulerAlreadyRunningException` 사용
- AtomicBoolean으로 동시성 제어
- 클라이언트에게 "이미 진행 중" 상태 반환

### 캐시 일관성
- 스케줄러 실행 후 자동으로 관련 캐시 무효화 (@CacheEvict)
- 데이터 신선도 보장

## 📊 데이터베이스 스키마

### summoner (친구 정보)
```sql
CREATE TABLE summoner (
    id BIGSERIAL PRIMARY KEY,
    puuid VARCHAR(100) NOT NULL UNIQUE,
    game_name VARCHAR(100) NOT NULL,
    tag_line VARCHAR(20) NOT NULL,
    platform VARCHAR(10) NOT NULL DEFAULT 'kr',
    profile_icon_id INT,
    summoner_level INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_summoner_puuid ON summoner(puuid);
```

### rank_snapshot (LP 시계열)
```sql
CREATE TABLE rank_snapshot (
    id BIGSERIAL PRIMARY KEY,
    puuid VARCHAR(100) NOT NULL,
    queue_type VARCHAR(30) NOT NULL,  -- RANKED_SOLO_5x5 / RANKED_FLEX_SR
    tier VARCHAR(20) NOT NULL,         -- IRON ~ CHALLENGER
    division VARCHAR(5),               -- I~IV (마스터+ 는 NULL)
    league_points INT NOT NULL,
    wins INT NOT NULL,
    losses INT NOT NULL,
    recorded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_snapshot_query ON rank_snapshot(puuid, queue_type, recorded_at DESC);
```

### match_record (매치 기록, 불변)
```sql
CREATE TABLE match_record (
    id BIGSERIAL PRIMARY KEY,
    match_id VARCHAR(30) NOT NULL UNIQUE,
    puuid VARCHAR(100) NOT NULL,
    champion VARCHAR(40),
    win BOOLEAN,
    kills INT,
    deaths INT,
    assists INT,
    queue_id INT,
    game_creation TIMESTAMP,
    game_duration INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_match_query ON match_record(puuid, game_creation DESC);
```

## 🐳 Docker 관련

### docker-compose.yml
```yaml
version: '3.9'

services:
  postgres:
    image: postgres:15-alpine
    environment:
      POSTGRES_DB: tierpeek
      POSTGRES_USER: tierpeek
      POSTGRES_PASSWORD: ${POSTGRES_PASSWORD}
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data

  tierpeek-app:
    build: .
    ports:
      - "8080:8080"
    environment:
      RIOT_API_KEY: ${RIOT_API_KEY}
      DB_PASSWORD: ${DB_PASSWORD}
      SPRING_PROFILES_ACTIVE: ${SPRING_PROFILES_ACTIVE:dev}
    depends_on:
      - postgres
      - redis

volumes:
  postgres_data:
  redis_data:
```

### 이미지 빌드
```bash
docker build -t tierpeek:latest .
```

### 컨테이너 실행
```bash
docker run -d \
  --name tierpeek \
  -p 8080:8080 \
  -e RIOT_API_KEY=your-key \
  -e DB_PASSWORD=your-password \
  tierpeek:latest
```

## 🎓 프로젝트 하이라이트

### 아키텍처 특징
- **레이어드 구조**: Controller → Service → Client/Repository 명확한 의존성
- **비동기 처리**: WebFlux WebClient로 Riot API 논블로킹 호출
- **다단계 캐싱**: Redis로 대시보드(5분), 그래프(30분), 매치(1시간) 분리 관리
- **설정값 기반**: 환경마다 다른 스케줄 주기 적용 가능
- **중복 실행 방지**: 스케줄러 동시성 제어로 안정성 확보

### 성능 최적화
- 친구 6명을 비동기로 동시 폴링 (응답 시간 100ms 이내)
- 신규 매치만 감지해 API 호출 최소화
- JSON 직렬화로 Redis 캐시 3배 빠르게

### 코드 품질
- Token Bucket 알고리즘 직접 구현
- 예외 처리 세분화 (RiotApiException, SchedulerAlreadyRunningException 등)
- 민감정보 로그 제거 (API 키, puuid 전체 노출 금지)
- 설정값 검증 강화

### 학습 포인트
이 프로젝트는 다음을 학습하기에 좋은 예제입니다:
- Spring WebFlux를 활용한 비동기 HTTP 클라이언트 구현
- 시계열 데이터 수집 및 저장 패턴
- Redis 다단계 TTL 캐싱 전략
- Token Bucket 알고리즘 (Rate Limiting)
- Spring @Scheduled 스케줄러 설정값화
- Layered Architecture 실전 적용

## 빌드 & 테스트

```bash
# 전체 빌드 (테스트 제외)
./gradlew clean build -x test

# 테스트 실행
./gradlew test

# 특정 테스트 실행
./gradlew test --tests "RiotRateLimiterTest"
```

## 📝 라이선스

MIT License

## 🤝 기여

버그 리포트 및 기능 요청은 GitHub Issues를 통해 제출해주세요.

## 📧 연락처

- Email: minky5004@gmail.com
- GitHub: [@minky5004](https://github.com/minky5004)