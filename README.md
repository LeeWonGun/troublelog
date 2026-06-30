## TroubleLog 로컬 개발 실행 가이드

### 1. 프로젝트 실행 전 준비 사항

TroubleLog 프로젝트를 로컬에서 실행하기 위해 아래 항목이 필요합니다.

* Java 21
* MySQL 8.x
* Git
* IntelliJ IDEA 또는 사용 중인 Java IDE
* Postman
* 프론트엔드 개발 시 Node.js 및 npm

---

### 2. 프로젝트 clone

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

---

### 3. 백엔드 실행

백엔드 프로젝트 위치로 이동한 뒤 Spring Boot 애플리케이션을 실행합니다.

```bash
./mvnw spring-boot:run
```

Windows 환경에서 Maven Wrapper 실행이 안 될 경우 아래 명령어를 사용할 수 있습니다.

```bash
mvn spring-boot:run
```

서버가 정상 실행되면 기본 포트는 다음과 같습니다.

```text
http://localhost:8080
```

---

### 4. 서버 상태 확인

브라우저 또는 Postman에서 아래 API를 호출합니다.

```http
GET http://localhost:8080/api/health
```

정상 응답 예시는 다음과 같습니다.

```json
{
  "success": true,
  "message": "서버가 정상적으로 실행 중입니다.",
  "errorCode": null,
  "data": "OK"
}
```

---

### 5. 테스트용 더미 데이터 실행

테스트용 데이터는 아래 파일에 정리되어 있습니다.

```text
docs/sql/dummy_data.sql
```

이 파일은 로컬 개발, API 테스트, 프론트 화면 테스트를 위한 더미 데이터입니다.

주의 사항:

* 실행 시 기존 테스트 데이터가 초기화됩니다.
* `users`, `teams`, `team_members`, `questions`, `answers`, `likes` 등의 테스트 데이터가 다시 삽입됩니다.
* 실제 운영 데이터에는 사용하지 않습니다.

MySQL에서 프로젝트 DB를 선택한 뒤 `dummy_data.sql` 파일을 실행합니다.

예시:

```sql
USE troublelog;
SOURCE docs/sql/dummy_data.sql;
```

또는 MySQL Workbench, DBeaver, DataGrip 등에서 `docs/sql/dummy_data.sql` 파일을 열고 전체 실행합니다.

---

### 6. 테스트 계정

더미 데이터 기준 LOCAL 사용자 테스트 비밀번호는 모두 다음과 같습니다.

```text
password
```

대표 테스트 계정:

```text
leader@example.com
backend@example.com
frontend@example.com
outsider@example.com
```

Google 로그인 테스트용 사용자는 비밀번호가 없습니다.

```text
googleuser@gmail.com
```

---

### 7. Postman 테스트

Postman Environment 이름은 다음과 같이 사용합니다.

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

API 테스트는 아래 순서로 진행하는 것을 권장합니다.

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

로그인 API가 완성되면 응답으로 받은 JWT를 `accessToken` 변수에 저장한 뒤 인증이 필요한 API에서 Bearer Token으로 사용합니다.
