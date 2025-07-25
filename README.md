# 🗨️ KakaoTalk Clone Project

> Java + Spring Boot 기반 카카오톡 핵심 기능 클론 코딩 프로젝트

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![WebSocket](https://img.shields.io/badge/WebSocket-Real--time-blue.svg)](https://spring.io/guides/gs/messaging-stomp-websocket/)
[![Redis](https://img.shields.io/badge/Redis-Pub%2FSub-red.svg)](https://redis.io/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-blue.svg)](https://www.mysql.com/)

## 📋 프로젝트 개요

### 🎯 목표
Java + Spring Boot를 활용하여 **카카오톡의 핵심 채팅 기능**을 구현하는 클론 프로젝트

### 🚀 개발 범위 (MVP)
- ✅ **회원가입 및 친구 추가** 시스템
- ✅ **1:1 및 단체 채팅방** 생성/관리
- ✅ **실시간 채팅** (WebSocket 기반)
- ✅ **채팅방 나가기/삭제** 기능

### ⏰ 개발 조건
- **개발 기간**: 1주일 내 MVP 완성
- **플랫폼**: 웹 기반 서버 (모바일 앱 제외)
- **환경**: 로컬에서 실행 가능한 테스트 환경 구성

## 🏗️ 시스템 아키텍처

### 📐 구조 설계도
```
[Client] ⇄ [WebSocket Handler]
               ↓
          [MessageService]
               ↓
    [RedisPublisher] ← [Validator/Auth]
               ↓
          [Redis Pub/Sub]
               ↓
    [Subscriber → DB 저장]
    [Subscriber → WebSocket Push]
```

### 🎯 핵심 요구사항
- **실시간 메시지 전송** (WebSocket)
- **대화 기록 보존** (Database 저장)
- **오프라인 메시지 처리** (사용자 접속 종료 후에도 메시지 불러오기)
- **확장성과 유연성** (Kafka로 교체 가능한 구조)

## 🛠️ 기술 스택

### Core Technologies
| 기술 | 버전 | 선택 이유 |
|------|------|----------|
| **Java** | 17+ | 안정성과 성능 |
| **Spring Boot** | 3.x | 빠른 개발 생산성, 구조화된 서비스 설계 |
| **Spring WebSocket** | - | 실시간 채팅을 위한 연결 유지형 통신 |
| **Redis** | 7.x | Pub/Sub 메시지 브로커, 비동기 처리 |
| **MySQL** | 8.0 | 채팅 기록 영구 저장 |
| **Kafka** | - | 추후 도입 예정 (대용량 메시징 처리) |

### Development Tools
- **Build Tool**: Maven/Gradle
- **IDE**: IntelliJ IDEA
- **Database Tool**: Intellij DataGrip
- **API Testing**: Postman
- **Version Control**: Git

## 🔄 메시지 처리 플로우

### 📨 실시간 메시지 전송 과정
1. **발신**: 사용자 A가 메시지 전송 → WebSocket으로 서버 전달
2. **발행**: 서버가 Redis에 메시지 publish
3. **구독**: Redis Subscriber가 메시지 수신 후:
    - MySQL DB에 메시지 저장
    - 수신자 B의 WebSocket 세션 탐색
4. **전달**:
    - **온라인**: 실시간 메시지 푸시
    - **오프라인**: 미읽음 메시지로 저장

### 📋 메시지 포맷 (JSON)
```json
{
  "type": "CHAT",              // 메시지 유형 (CHAT, ENTER, EXIT)
  "roomId": "room-abc-123",    // 채팅방 고유 ID
  "senderId": "user-1",        // 발신자 ID
  "content": "안녕하세요!",     // 메시지 내용
  "timestamp": "2024-01-01T12:00:00Z"
}
```

## 📦 프로젝트 구조

### 🏛️ 패키지 구조 (DDD 기반)
```
src/main/java/com/kakaoclone/
├── message/
│   ├── domain/
│   │   ├── ChatMessage.java          // 메시지 도메인 모델
│   │   ├── ChatRoom.java             // 채팅방 도메인 모델
│   │   └── MessageType.java          // 메시지 타입 열거형
│   ├── application/
│   │   ├── MessageService.java       // 메시지 비즈니스 로직
│   │   └── ChatRoomService.java      // 채팅방 관리 서비스
│   └── infrastructure/
│       ├── RedisPublisher.java       // Redis 메시지 발행
│       ├── RedisSubscriber.java      // Redis 메시지 구독
│       └── WebSocketSessionManager.java // WebSocket 세션 관리
├── user/
│   ├── domain/
│   │   └── User.java                 // 사용자 도메인 모델
│   ├── application/
│   │   └── UserService.java          // 사용자 관리 서비스
│   └── infrastructure/
│       └── UserRepository.java       // 사용자 데이터 접근
└── config/
    ├── WebSocketConfig.java          // WebSocket 설정
    ├── RedisConfig.java              // Redis 설정
    └── DatabaseConfig.java           // 데이터베이스 설정
```

### 🗃️ 데이터베이스 스키마
```sql
-- 사용자 테이블
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 채팅방 테이블
CREATE TABLE chat_rooms (
    id VARCHAR(100) PRIMARY KEY,
    name VARCHAR(100),
    type ENUM('DIRECT', 'GROUP'),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 메시지 테이블
CREATE TABLE messages (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    room_id VARCHAR(100),
    sender_id BIGINT,
    content TEXT,
    type ENUM('CHAT', 'ENTER', 'EXIT'),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (room_id) REFERENCES chat_rooms(id),
    FOREIGN KEY (sender_id) REFERENCES users(id)
);
```

## 🚀 구현 로드맵

### Phase 1: 기반 구조 (1-2일)
- [ ] Spring Boot 프로젝트 설정
- [ ] WebSocket 메시지 핸들러 구현
- [ ] Redis Pub/Sub 시스템 구축
- [ ] 기본 도메인 모델 설계

### Phase 2: 핵심 기능 (3-4일)
- [ ] 사용자 회원가입/로그인 API
- [ ] 채팅방 생성/관리 기능
- [ ] 실시간 메시지 송수신
- [ ] 메시지 저장/조회 기능

### Phase 3: 고급 기능 (5-6일)
- [ ] 친구 추가/관리 시스템
- [ ] 단체 채팅방 기능
- [ ] 채팅방 나가기/삭제
- [ ] 미읽음 메시지 처리

### Phase 4: 테스트 및 최적화 (7일)
- [ ] 통합 테스트 작성
- [ ] 성능 최적화
- [ ] 로컬 환경 구성 가이드
- [ ] 문서화 완료

## 🔧 로컬 설치 및 실행

### 📋 사전 요구사항
- Java 17 이상
- Maven 3.6+ 또는 Gradle 7+
- MySQL 8.0
- Redis 7.x
- Git

### 📋 docker-compose 사용법

```
docker-compose.yml

# 1. docker-compose 실행 (백그라운드 실행)
docker-compose up -d

# 2. 컨테이너 상태 확인
docker ps

# 3. 종료 시
docker-compose down

```

### 🚀 설치 및 실행
```bash
# 1. 프로젝트 클론
git clone https://github.com/yourusername/kakaotalk-clone.git
cd kakaotalk-clone

# 2. 데이터베이스 설정
mysql -u root -p
CREATE DATABASE kakaoclone;

# 3. Redis 서버 시작
redis-server

# 4. 애플리케이션 설정
cp src/main/resources/application.yml.example application.yml
# application.yml에서 데이터베이스 연결 정보 수정

# 5. 프로젝트 빌드 및 실행
./mvnw spring-boot:run
```

### 🌐 접속 정보
- **Web Application**: http://localhost:8080
- **WebSocket Endpoint**: ws://localhost:8080/chat
- **API Base URL**: http://localhost:8080/api

## 📊 성능 고려사항

### 🔄 확장성 설계
- **모듈화된 구조**: Redis → Kafka 전환 용이
- **세션 관리**: Redis 기반 분산 세션
- **데이터베이스**: 인덱싱 및 파티셔닝 고려

### ⚡ 최적화 포인트
- WebSocket 연결 풀링
- Redis 연결 풀 관리
- 메시지 배치 처리
- 캐시 전략 수립
