# TroubleLog

개발 오류 해결 과정을 기록하고 공유하는 Q&A 기반 팀 지식 저장소 프로젝트입니다.

## 프로젝트 구조

```text
TroubleLog
├── troublelog_backend
├── troublelog_frontend
├── docs
└── docker-compose.yml

Backend
- Java 21
- Spring Boot
- Spring Data JPA
- MyBatis
- MySQL
- Flyway
- Spring Security
- OAuth2 Client

Database 실행
- docker compose up -d

Backend 실행
- cd troublelog_backend
- ./mvnw spring-boot:run

Windows PowerShell에서는 다음 명령어를 사용할 수 있습니다.

- cd troublelog_backend
- .\mvnw spring-boot:run


Health Check (postman)
 - GET http://localhost:8080/api/health