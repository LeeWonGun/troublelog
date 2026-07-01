# TroubleLog 로컬 개발 실행 가이드

## 1. 준비 사항

- Java 21
- MySQL 8.x
- Git
- IntelliJ IDEA 또는 Java IDE
- Postman
- 프론트엔드 개발용 Node.js 및 npm

## 2. 프로젝트 clone

```bash
git clone [repository-url]
cd TroubleLog
```

개발 작업은 `develop` 브랜치를 기준으로 진행합니다.

```bash
git checkout develop
git pull origin develop
```

기능 개발 시에는 `develop` 브랜치에서 `feature/*` 브랜치를 생성합니다.

```bash
git checkout -b feature/기능명
```

## 3. 환경 변수

실제 비밀값은 Git에 올리지 않고 `.env`에 저장합니다.

```bash
cp .env.example .env
```

`.env`에는 `SMTP_PASSWORD`, `JWT_SECRET`, `RESEND_API_KEY`, `GOOGLE_CLIENT_SECRET` 같은 실제 값을 넣습니다.
`.env`는 Git에서 무시됩니다.

## 4. 백엔드 실행

백엔드 프로젝트 위치로 이동한 뒤 Spring Boot 애플리케이션을 실행합니다.

```bash
cd troublelog_backend
./mvnw spring-boot:run
```

Windows PowerShell에서 Maven Wrapper 실행이 어려운 경우:

```bash
mvn spring-boot:run
```

기본 서버 주소:

```text
http://localhost:8080
```

## 5. 서버 상태 확인

브라우저 또는 Postman에서 호출합니다.

```http
GET http://localhost:8080/api/health
```

정상 응답 예시:

```json
{
  "success": true,
  "message": "서버가 정상적으로 실행 중입니다.",
  "errorCode": null,
  "data": "OK"
}
```

## 6. 테스트용 더미 데이터

테스트용 데이터는 아래 파일에 정리되어 있습니다.

```text
docs/sql/dummy_data.sql
```

주의 사항:

- 실행 전 기존 테스트 데이터가 초기화됩니다.
- `users`, `teams`, `team_members`, `questions`, `answers`, `likes` 등의 테스트 데이터가 다시 삽입됩니다.
- 실제 운영 데이터에는 사용하지 않습니다.

MySQL에서 프로젝트 DB를 선택한 뒤 실행합니다.

```sql
USE troublelog;
SOURCE docs/sql/dummy_data.sql;
```

또는 MySQL Workbench, DBeaver, DataGrip 등에서 `docs/sql/dummy_data.sql` 파일을 열고 전체 실행합니다.

## 7. DB Migration

테이블 생성과 기본 스키마 관리는 Flyway migration 파일로 관리합니다.

```text
troublelog_backend/src/main/resources/db/migration/V1__init_schema.sql
troublelog_backend/src/main/resources/db/migration/V2__insert_initial_tech_stacks.sql
troublelog_backend/src/main/resources/db/migration/V3__create_email_verification_codes.sql
```

이번 인증 작업에서 추가된 파일은 `V3__create_email_verification_codes.sql`입니다.
회원가입/비밀번호 재설정 이메일 인증번호 저장용 `email_verification_codes` 테이블을 생성합니다.

이미 적용된 migration 파일은 수정하지 않고, 스키마 변경이 필요하면 `V4__...sql`처럼 새 migration 파일을 추가합니다.

## 8. 테스트 계정

더미 데이터 기준 LOCAL 사용자 테스트 비밀번호는 모두 같습니다.

```text
password
```

테스트 계정:

```text
leader@example.com
backend@example.com
frontend@example.com
outsider@example.com
```

Google 로그인 테스트용 사용자는 서비스 비밀번호가 없습니다.

```text
googleuser@gmail.com
```

## 9. Postman 테스트

Postman Environment 이름:

```text
TroubleLog Local
```

기본 변수 예시:

```text
baseUrl = http://localhost:8080
userEmail = leader@example.com
userPassword = password
teamId = 1
teamCode = BACK1234
questionId = 1
answerId = 1
commentId = 2
replyId = 3
fileId = 1
```

권장 테스트 순서:

```text
1. Health Check
2. Login
3. Get Current User
4. Get My Teams
5. Get Public Questions
6. Get Question Detail
7. Get Answer Tree
8. Like Question
9. Search Questions
```

현재 인증은 JWT 기반이며, JWT는 응답 본문이 아니라 `ACCESS_TOKEN` HttpOnly Cookie로 전달됩니다.
프론트 요청은 `credentials: "include"`를 사용해야 합니다.
