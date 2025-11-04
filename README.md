# TransCall Server – 실시간 번역 영상 통화 백엔드

## 개요
**TransCall Server**는 Android 애플리케이션인 TransCall의 백엔드 서버로서, 다자간 영상 통화와 실시간 번역 기능을 제공합니다. 이 서버는 **Spring Boot 3(WebFlux)**와 **코틀린(Kotlin)**을 기반으로 구축되어 **Non‑Blocking I/O**와 **Reactive Streams**를 통해 높은 동시성을 지원합니다. Janus SFU와 Whisper STT/번역 서비스, PostgreSQL, Redis를 통합하여 안정적인 실시간 서비스와 데이터 영속화를 구현했습니다.

## 기술 스택
- **Spring Boot 3 (WebFlux)**: 비동기, 논블로킹 웹 어플리케이션 프레임워크로 reactive API와 WebSocket을 지원합니다. `build.gradle.kts`에서 WebFlux, R2DBC, Redis Reactive, Security, Validation 등이 의존성으로 추가되어 있습니다. 
- **R2DBC + PostgreSQL**: 반응형 관계형 데이터베이스 드라이버를 사용해 DB와 통신합니다. Flyway를 통해 스키마를 버전 관리하며, UUID를 기본 키로 사용하는 도메인 모델을 정의합니다.
- **Redis**: 캐시 및 세션 저장소로 사용하며, Janus 방 ID와 리프레쉬 토큰을 저장합니다.
- **Janus WebRTC Server**: 미디어 스트림을 중계하는 SFU로, 서버는 Janus 세션/플러그인을 관리하는 `JanusHandler`를 통해 방 생성·참여·publish/subscribe 등의 제어를 수행합니다.
- **Whisper STT/번역 서비스**: 별도 컨테이너로 배포된 Python 기반 Whisper 마이크로서비스와 REST/WS로 통신하여 음성→텍스트→번역을 수행합니다.
- **OAuth 2.0 + JWT**: Google OAuth 로그인 후 서버가 JWT(Access/Refresh)를 발급하고, 요청마다 Security 필터에서 토큰을 검증합니다.
- **Docker Compose**: `docker-compose.yml`을 통해 PostgreSQL, Redis, Janus, Whisper 서비스, 서버 컨테이너를 한 번에 실행할 수 있습니다.

## 핵심 기능 및 모듈
### 방(Room) 관리
- **CallRoom 도메인**: UUID, 방 코드, 제목, 최대/현재 참여자 수, Janus 방 ID 등으로 구성된 엔티티이며, 공개/비공개 여부와 상태(대기/진행/종료)를 관리합니다.
- **CallRoomService**: 방 생성, 입장, 정보 조회, 참가자 수 업데이트를 담당합니다. 방 생성 시 Janus API를 호출해 SFU 방을 생성하고 이를 캐시에 저장합니다. 공용 방 목록은 커서 기반 페이지네이션으로 제공됩니다.

### 참여자 관리
- **CallParticipant 도메인**: 방 ID, 사용자 ID, 언어, 국가, 입장/퇴장 시각을 저장합니다.
- **CallParticipantService**: 사용자의 방 입장 여부 확인, 참여자 저장 및 퇴장 시각 업데이트, 현재 참여자 목록 조회 등을 제공합니다.

### 대화 및 번역 관리
- **대화**: 사용자가 말하는 내용은 Whisper 서비스에서 STT 결과를 받은 후 서버에서 브로드캐스트 합니다.
- **대화 영속화**: 대화가 완료되면 `CallConversation` 엔티티에 저장되고, 번역 결과는 `CallConversationTrans`에 사용자 별로 저장됩니다. 대화 조회 API는 시간 범위와 커서를 사용해 최신 메시지를 페이징하며, `getConversationsSyncTimeRange`는 특정 시점 이후 업데이트된 메시지만 반환합니다.
- **WebSocket 시그널링**: 서버는 클라이언트와의 WebSocket 연결을 통해 방 입장, ICE candidate 교환, publish/subscribe, 카메라/마이크 상태 변경 등의 신호를 주고받습니다. `SignalingMessageHandler`는 Janus VideoRoom 플러그인과 연동해 모든 이벤트를 비동기적으로 처리합니다.

### 통화 기록 관리
- **CallHistory 도메인**: 방 ID, 유저 ID, 제목, 요약, 메모, 좋아요 여부, 삭제 여부, 퇴장 시각 등을 저장합니다.
- **CallHistoryService**: 사용자가 통화에서 퇴장할 때 기록을 생성하거나 업데이트하며, 히스토리 목록을 페이징하여 제공합니다.

### 사용자 및 인증
- **User 도메인**: 이메일, 소셜 타입(Google), 닉네임, 언어·국가 코드, 멤버십 플랜, 프로필 이미지 등을 포함합니다.
- **AuthService**: Google OAuth로 받은 idToken과 nonce를 검증하고 Access/Refresh 토큰을 발급합니다. Refresh 토큰 저장소를 사용해 토큰 재발급을 관리합니다.
- **SecurityConfig**: Spring Security WebFlux로 모든 API를 보호하며 JWT Authentication Filter를 통해 토큰을 검증하고, Swagger/OpenAPI 경로만 인증 없이 허용합니다.

## 아키텍처 개요
1. **Reactive Layered Architecture**
   - **Controller**: REST API 및 WebSocket 엔드포인트를 정의합니다. 예: `CallRoomController`, `CallConversationController`.
   - **Service**: 비즈니스 로직을 구현하며, 트랜잭션과 캐시 로직을 관리합니다. 예: `CallConversationServiceImpl`은 캐시 발행/저장, 대화 조회와 페이징을 처리합니다.
   - **Repository**: R2DBC를 사용한 비동기 데이터 액세스를 제공합니다. Custom 쿼리를 통해 시간 범위와 커서 기반 페이징을 구현합니다.
2. **WebSocket + Janus**
   - **RoomWebSocketHandler**: 클라이언트 연결 시 JWT를 검증하고 세션을 초기화합니다. Janus와 Whisper 연결을 설정한 후, 사용자 연결/해제 이벤트를 참여자 목록과 방 상태에 반영합니다.
   - **JanusHandler**: Janus 세션 및 VideoRoom 플러그인을 제어합니다. 방 생성, 핸들 attach, publish/subscribe 요청을 수행하고, 이벤트를 `SignalingMessageHandler`로 전달합니다.
   - **WhisperHandler**: Whisper WebSocket 클라이언트와 연결해 STT 이벤트를 수신하고 번역 요청을 전송합니다. 번역된 자막은 Session Manager를 통해 클라이언트에 실시간 브로드캐스트됩니다.
3. **Database & Caching**
   - **PostgreSQL**: 모든 핵심 엔티티(CallRoom, CallParticipant, CallConversation, CallHistory, User 등)를 저장합니다. Flyway를 통해 스키마 버전을 관리합니다.
   - **Redis**: Janus 방 ID 및 대화 캐시를 저장하여 빠른 조회와 멱등성을 보장합니다. TTL을 통해 불필요한 데이터를 자동으로 제거합니다.

## 배포 및 실행 방법
1. **환경 요구 사항**: JDK 17, Docker(Docker Compose), PostgreSQL, Redis.
2. **클론**:
   ```bash
   git clone https://github.com/YeonjunNotFR/transcall_server.git
   cd transcall_server
   ```
3. **로컬 실행**:
   - `docker-compose up`으로 PostgreSQL, Redis, Janus, Whisper, 서버 컨테이너를 한 번에 실행합니다.
   - 서버 애플리케이션만 실행하려면 `./gradlew bootRun`을 사용하되, 외부에서 PostgreSQL, Redis, Janus, Whisper가 구동되고 있는지 확인합니다.
4. **마이그레이션**: 애플리케이션이 시작되면 Flyway가 `src/main/resources/db/migration`의 SQL 스크립트를 적용해 스키마를 생성합니다.
5. **환경 변수**: Google OAuth 클라이언트 ID/Secret, JWT 비밀키, 데이터베이스 접속 정보, Whisper 서비스 URL 등은 `application.yml` 또는 Docker Compose `.env` 파일에 설정합니다.

## 프로젝트 구조 (발췌)
```
transcall_server/
  ├─ src/main/kotlin/com/youhajun/transcall/
  │    ├─ call/
  │    │    ├─ room/                 # CallRoom 도메인, 서비스, 컨트롤러
  │    │    ├─ participant/          # CallParticipant 도메인, 서비스
  │    │    ├─ conversation/         # 대화/번역, 서비스, 컨트롤러
  │    │    ├─ history/              # 통화 기록 도메인, 서비스
  │    ├─ user/                      # 사용자 도메인, 서비스
  │    ├─ auth/                      # 인증/권한 관련 서비스, 컨트롤러
  │    ├─ janus/                     # Janus 세션 및 VideoRoom 플러그인 연동
  │    ├─ whisper/                   # Whisper STT/번역 클라이언트
  │    ├─ common/                    # 공통 VO, 예외, config
  │    ├─ pagination/                # 커서 기반 페이징 로직
  │    └─ global/config/             # WebFlux, Security, Redis, R2DBC 설정
  ├─ src/main/resources/
  │    ├─ application.yml            # 설정 파일 (DB, Redis, JWT 등)
  │    └─ db/migration/              # Flyway 스키마 마이그레이션 SQL
  ├─ build.gradle.kts                # 빌드 설정과 의존성 선언
  └─ docker-compose.yml              # 로컬 개발 환경을 위한 컨테이너 정의
```
