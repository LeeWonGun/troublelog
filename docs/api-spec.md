# TroubleLog API 명세서

작성 기준: 2026-07-01  
범위: 현재 백엔드에 구현 완료된 API만 포함

## 1. 기본 정보

| 항목 | 내용 |
| --- | --- |
| Base URL | `http://localhost:8080` |
| Content-Type | `application/json` |
| 인증 방식 | JWT HttpOnly Cookie |
| 인증 쿠키 | `ACCESS_TOKEN` |

프론트에서 인증이 필요한 API를 호출할 때는 쿠키가 포함되도록 설정해야 한다.

```js
fetch("http://localhost:8080/api/auth/me", {
  credentials: "include"
});
```

JWT는 응답 body로 내려주지 않는다. 로그인 성공 시 서버가 `ACCESS_TOKEN` HttpOnly Cookie를 내려주고, 브라우저가 이후 요청에 자동으로 포함한다.

`Authorization: Bearer <token>` 방식은 사용하지 않는다.

## 2. 공통 응답 형식

### 성공 응답

```json
{
  "success": true,
  "message": "요청이 완료되었습니다.",
  "errorCode": null,
  "data": {}
}
```

### 실패 응답

```json
{
  "success": false,
  "message": "오류 메시지",
  "errorCode": "ERROR_CODE",
  "data": null
}
```

### 검증 실패 응답

```json
{
  "success": false,
  "message": "입력값이 올바르지 않습니다.",
  "errorCode": "VALIDATION_ERROR",
  "data": {
    "email": "이메일 형식이 올바르지 않습니다."
  }
}
```

## 3. 공통 검증 규칙

| 필드 | 규칙 |
| --- | --- |
| `email` | 이메일 형식, 최대 100자 |
| `password`, `newPassword` | 영문, 숫자, 특수문자 포함 8자 이상 |
| `verificationCode`, `code` | 6자리 숫자 |
| `nickname` | 특수문자 없이 2~50자 |
| `team.name` | 필수, 최대 100자 |
| `teamCode` | 필수, 최대 50자 |

## 4. 인증/회원 API

### POST `/api/auth/signup/send-code`

회원가입 이메일 인증 코드를 발송한다.

| 항목 | 내용 |
| --- | --- |
| 인증 | 불필요 |
| Request Body | `email` |
| Response Data | 없음 |
| 주요 에러 | `VALIDATION_ERROR`, `DUPLICATE_EMAIL`, `MAIL_SEND_FAILED` |

Request:

```json
{
  "email": "user@example.com"
}
```

Response:

```json
{
  "success": true,
  "message": "회원가입 인증 메일을 발송했습니다.",
  "errorCode": null,
  "data": null
}
```

### POST `/api/auth/signup/verify-code`

회원가입 이메일 인증 코드를 확인한다.

| 항목 | 내용 |
| --- | --- |
| 인증 | 불필요 |
| Request Body | `email`, `code` |
| Response Data | `verified` |
| 주요 에러 | `VALIDATION_ERROR`, `INVALID_VERIFICATION_CODE`, `EXPIRED_VERIFICATION_CODE` |

Request:

```json
{
  "email": "user@example.com",
  "code": "123456"
}
```

Response:

```json
{
  "success": true,
  "message": "이메일 인증이 완료되었습니다.",
  "errorCode": null,
  "data": {
    "verified": true
  }
}
```

### POST `/api/auth/signup`

이메일 인증이 완료된 LOCAL 사용자를 가입시킨다.

| 항목 | 내용 |
| --- | --- |
| 인증 | 불필요 |
| Request Body | `email`, `password`, `nickname`, `verificationCode` |
| Response Data | `UserResponse` |
| 주요 에러 | `VALIDATION_ERROR`, `DUPLICATE_EMAIL`, `DUPLICATE_NICKNAME`, `INVALID_VERIFICATION_CODE`, `EXPIRED_VERIFICATION_CODE` |

Request:

```json
{
  "email": "user@example.com",
  "password": "Abcd1234!",
  "nickname": "troubleUser",
  "verificationCode": "123456"
}
```

Response:

```json
{
  "success": true,
  "message": "회원가입이 완료되었습니다.",
  "errorCode": null,
  "data": {
    "userId": 1,
    "email": "user@example.com",
    "nickname": "troubleUser",
    "authProvider": "LOCAL"
  }
}
```

### POST `/api/auth/login`

이메일과 비밀번호로 로그인하고 JWT 쿠키를 발급한다.

| 항목 | 내용 |
| --- | --- |
| 인증 | 불필요 |
| Request Body | `email`, `password` |
| Response Header | `Set-Cookie: ACCESS_TOKEN=...; HttpOnly; SameSite=Lax` |
| Response Data | `authType`, `user` |
| 주요 에러 | `VALIDATION_ERROR`, `INVALID_PASSWORD`, `INVALID_AUTH_PROVIDER` |

Request:

```json
{
  "email": "user@example.com",
  "password": "Abcd1234!"
}
```

Response:

```json
{
  "success": true,
  "message": "로그인되었습니다.",
  "errorCode": null,
  "data": {
    "authType": "JWT_COOKIE",
    "user": {
      "userId": 1,
      "email": "user@example.com",
      "nickname": "troubleUser",
      "authProvider": "LOCAL"
    }
  }
}
```

### POST `/api/auth/logout`

JWT 쿠키를 만료시켜 로그아웃한다.

| 항목 | 내용 |
| --- | --- |
| 인증 | 필요 |
| Request Body | 없음 |
| Response Header | `Set-Cookie: ACCESS_TOKEN=; Max-Age=0` |
| Response Data | 없음 |
| 주요 에러 | `UNAUTHORIZED`, `INVALID_TOKEN` |

Response:

```json
{
  "success": true,
  "message": "로그아웃되었습니다.",
  "errorCode": null,
  "data": null
}
```

### GET `/api/auth/me`

현재 로그인 사용자를 조회한다.

| 항목 | 내용 |
| --- | --- |
| 인증 | 필요 |
| Response Data | `UserResponse` |
| 주요 에러 | `UNAUTHORIZED`, `INVALID_TOKEN`, `USER_NOT_FOUND` |

Response:

```json
{
  "success": true,
  "message": "현재 로그인 사용자 조회가 완료되었습니다.",
  "errorCode": null,
  "data": {
    "userId": 1,
    "email": "user@example.com",
    "nickname": "troubleUser",
    "authProvider": "LOCAL"
  }
}
```

### GET `/api/auth/check-email`

이메일 사용 가능 여부를 확인한다.

| 항목 | 내용 |
| --- | --- |
| 인증 | 불필요 |
| Query Parameter | `email` |
| Response Data | `available` |
| 주요 에러 | `VALIDATION_ERROR` |

Request:

```text
GET /api/auth/check-email?email=user@example.com
```

Response:

```json
{
  "success": true,
  "message": "이메일 중복 확인이 완료되었습니다.",
  "errorCode": null,
  "data": {
    "available": true
  }
}
```

### GET `/api/auth/check-nickname`

닉네임 사용 가능 여부를 확인한다.

| 항목 | 내용 |
| --- | --- |
| 인증 | 불필요 |
| Query Parameter | `nickname` |
| Response Data | `available` |
| 주요 에러 | `VALIDATION_ERROR` |

Request:

```text
GET /api/auth/check-nickname?nickname=troubleUser
```

Response:

```json
{
  "success": true,
  "message": "닉네임 중복 확인이 완료되었습니다.",
  "errorCode": null,
  "data": {
    "available": true
  }
}
```

### POST `/api/auth/password-reset/send-code`

비밀번호 재설정 인증 코드를 발송한다.

| 항목 | 내용 |
| --- | --- |
| 인증 | 불필요 |
| Request Body | `email` |
| Response Data | 없음 |
| 주요 에러 | `VALIDATION_ERROR`, `USER_NOT_FOUND`, `GOOGLE_USER_PASSWORD_RESET_NOT_ALLOWED`, `MAIL_SEND_FAILED` |

Request:

```json
{
  "email": "user@example.com"
}
```

Response:

```json
{
  "success": true,
  "message": "비밀번호 재설정 인증 메일을 발송했습니다.",
  "errorCode": null,
  "data": null
}
```

### POST `/api/auth/password-reset/verify-code`

비밀번호 재설정 인증 코드를 확인한다.

| 항목 | 내용 |
| --- | --- |
| 인증 | 불필요 |
| Request Body | `email`, `code` |
| Response Data | `verified` |
| 주요 에러 | `VALIDATION_ERROR`, `INVALID_VERIFICATION_CODE`, `EXPIRED_VERIFICATION_CODE` |

Request:

```json
{
  "email": "user@example.com",
  "code": "123456"
}
```

Response:

```json
{
  "success": true,
  "message": "비밀번호 재설정 인증이 완료되었습니다.",
  "errorCode": null,
  "data": {
    "verified": true
  }
}
```

### PATCH `/api/auth/password-reset`

인증 코드를 확인한 뒤 새 비밀번호로 재설정한다.

| 항목 | 내용 |
| --- | --- |
| 인증 | 불필요 |
| Request Body | `email`, `verificationCode`, `newPassword` |
| Response Data | 없음 |
| 주요 에러 | `VALIDATION_ERROR`, `USER_NOT_FOUND`, `GOOGLE_USER_PASSWORD_RESET_NOT_ALLOWED`, `INVALID_VERIFICATION_CODE`, `EXPIRED_VERIFICATION_CODE` |

Request:

```json
{
  "email": "user@example.com",
  "verificationCode": "123456",
  "newPassword": "Changed1234!"
}
```

Response:

```json
{
  "success": true,
  "message": "비밀번호가 재설정되었습니다.",
  "errorCode": null,
  "data": null
}
```

### GET `/oauth2/authorization/google`

Google OAuth 로그인을 시작한다. Spring Security OAuth 기본 경로다.

| 항목 | 내용 |
| --- | --- |
| 인증 | 불필요 |
| 동작 | Google 로그인 화면으로 리다이렉트 |

### GET `/login/oauth2/code/google`

Google OAuth 콜백 경로다. 인증 성공 시 LOCAL 비밀번호 로그인과 동일하게 `ACCESS_TOKEN` JWT 쿠키를 발급하고 설정된 성공 URL로 리다이렉트한다.

| 항목 | 내용 |
| --- | --- |
| 인증 | 불필요 |
| Response Header | `Set-Cookie: ACCESS_TOKEN=...; HttpOnly; SameSite=Lax` |
| Redirect Query | `userId`, `email`, `nickname`, `authProvider` |

## 5. 마이페이지 API

### GET `/api/users/me`

내 사용자 정보를 조회한다.

| 항목 | 내용 |
| --- | --- |
| 인증 | 필요 |
| Response Data | `UserResponse` |
| 주요 에러 | `UNAUTHORIZED`, `INVALID_TOKEN`, `USER_NOT_FOUND` |

Response:

```json
{
  "success": true,
  "message": "내 정보 조회가 완료되었습니다.",
  "errorCode": null,
  "data": {
    "userId": 1,
    "email": "user@example.com",
    "nickname": "troubleUser",
    "authProvider": "LOCAL"
  }
}
```

### PATCH `/api/users/me/nickname`

내 닉네임을 변경한다.

| 항목 | 내용 |
| --- | --- |
| 인증 | 필요 |
| Request Body | `nickname` |
| Response Data | `UserResponse` |
| 주요 에러 | `VALIDATION_ERROR`, `UNAUTHORIZED`, `DUPLICATE_NICKNAME`, `USER_NOT_FOUND` |

Request:

```json
{
  "nickname": "changedNick"
}
```

Response:

```json
{
  "success": true,
  "message": "닉네임이 변경되었습니다.",
  "errorCode": null,
  "data": {
    "userId": 1,
    "email": "user@example.com",
    "nickname": "changedNick",
    "authProvider": "LOCAL"
  }
}
```

### PATCH `/api/users/me/password`

LOCAL 사용자의 비밀번호를 변경한다. Google 사용자는 서비스 비밀번호가 없으므로 변경할 수 없다.

| 항목 | 내용 |
| --- | --- |
| 인증 | 필요 |
| Request Body | `currentPassword`, `newPassword` |
| Response Data | 없음 |
| 주요 에러 | `VALIDATION_ERROR`, `UNAUTHORIZED`, `INVALID_PASSWORD`, `GOOGLE_USER_PASSWORD_RESET_NOT_ALLOWED`, `USER_NOT_FOUND` |

Request:

```json
{
  "currentPassword": "Abcd1234!",
  "newPassword": "Changed1234!"
}
```

Response:

```json
{
  "success": true,
  "message": "비밀번호가 변경되었습니다.",
  "errorCode": null,
  "data": null
}
```

## 6. 팀 API

### POST `/api/teams`

팀을 생성하고 생성자를 `LEADER`로 등록한다.

| 항목 | 내용 |
| --- | --- |
| 인증 | 필요 |
| Request Body | `name`, `description` |
| Response Data | `TeamResponse` |
| 주요 에러 | `VALIDATION_ERROR`, `UNAUTHORIZED`, `USER_NOT_FOUND`, `DUPLICATE_RESOURCE` |

Request:

```json
{
  "name": "Backend Study",
  "description": "Spring Boot 스터디 팀"
}
```

Response:

```json
{
  "success": true,
  "message": "팀이 생성되었습니다.",
  "errorCode": null,
  "data": {
    "teamId": 1,
    "name": "Backend Study",
    "description": "Spring Boot 스터디 팀",
    "teamCode": "AB12CD",
    "role": "LEADER",
    "ownerId": 1,
    "createdAt": "2026-07-01T04:00:00"
  }
}
```

### POST `/api/teams/join`

팀 코드로 팀에 참여한다. 이전에 탈퇴한 팀이면 기존 멤버 row를 복구한다.

| 항목 | 내용 |
| --- | --- |
| 인증 | 필요 |
| Request Body | `teamCode` |
| Response Data | `TeamResponse` |
| 주요 에러 | `VALIDATION_ERROR`, `UNAUTHORIZED`, `INVALID_TEAM_CODE`, `ALREADY_JOINED_TEAM`, `USER_NOT_FOUND` |

Request:

```json
{
  "teamCode": "AB12CD"
}
```

Response:

```json
{
  "success": true,
  "message": "팀에 참여되었습니다.",
  "errorCode": null,
  "data": {
    "teamId": 1,
    "name": "Backend Study",
    "description": "Spring Boot 스터디 팀",
    "teamCode": "AB12CD",
    "role": "MEMBER",
    "ownerId": 1,
    "createdAt": "2026-07-01T04:00:00"
  }
}
```

### GET `/api/teams/my`

내가 참여 중인 팀 목록을 조회한다. 탈퇴한 팀과 삭제된 팀은 제외한다.

| 항목 | 내용 |
| --- | --- |
| 인증 | 필요 |
| Response Data | `TeamResponse[]` |
| 주요 에러 | `UNAUTHORIZED`, `USER_NOT_FOUND` |

Response:

```json
{
  "success": true,
  "message": "내 팀 목록 조회가 완료되었습니다.",
  "errorCode": null,
  "data": [
    {
      "teamId": 1,
      "name": "Backend Study",
      "description": "Spring Boot 스터디 팀",
      "teamCode": "AB12CD",
      "role": "LEADER",
      "ownerId": 1,
      "createdAt": "2026-07-01T04:00:00"
    }
  ]
}
```

### GET `/api/teams/code/{teamCode}`

팀 코드가 유효한지 확인하고 팀 기본 정보를 조회한다. 이미 참여 중인 사용자는 `role`이 포함되고, 미가입 사용자는 `role`이 `null`이다.

| 항목 | 내용 |
| --- | --- |
| 인증 | 필요 |
| Path Parameter | `teamCode` |
| Response Data | `TeamResponse` |
| 주요 에러 | `UNAUTHORIZED`, `INVALID_TEAM_CODE`, `USER_NOT_FOUND` |

Response:

```json
{
  "success": true,
  "message": "팀 코드 확인이 완료되었습니다.",
  "errorCode": null,
  "data": {
    "teamId": 1,
    "name": "Backend Study",
    "description": "Spring Boot 스터디 팀",
    "teamCode": "AB12CD",
    "role": null,
    "ownerId": 1,
    "createdAt": "2026-07-01T04:00:00"
  }
}
```

### GET `/api/teams/{teamId}`

팀 상세 정보를 조회한다. 해당 팀의 활성 팀원만 조회할 수 있다.

| 항목 | 내용 |
| --- | --- |
| 인증 | 필요 |
| Path Parameter | `teamId` |
| Response Data | `TeamResponse` |
| 주요 에러 | `UNAUTHORIZED`, `FORBIDDEN` |

Response:

```json
{
  "success": true,
  "message": "팀 상세 조회가 완료되었습니다.",
  "errorCode": null,
  "data": {
    "teamId": 1,
    "name": "Backend Study",
    "description": "Spring Boot 스터디 팀",
    "teamCode": "AB12CD",
    "role": "MEMBER",
    "ownerId": 1,
    "createdAt": "2026-07-01T04:00:00"
  }
}
```

### GET `/api/teams/{teamId}/members`

팀원 목록을 조회한다. 해당 팀의 활성 팀원만 조회할 수 있다.

| 항목 | 내용 |
| --- | --- |
| 인증 | 필요 |
| Path Parameter | `teamId` |
| Response Data | `TeamMemberResponse[]` |
| 주요 에러 | `UNAUTHORIZED`, `FORBIDDEN` |

Response:

```json
{
  "success": true,
  "message": "팀원 목록 조회가 완료되었습니다.",
  "errorCode": null,
  "data": [
    {
      "userId": 1,
      "nickname": "leaderNick",
      "role": "LEADER",
      "joinedAt": "2026-07-01T04:00:00"
    },
    {
      "userId": 2,
      "nickname": "memberNick",
      "role": "MEMBER",
      "joinedAt": "2026-07-01T04:10:00"
    }
  ]
}
```

### PATCH `/api/teams/{teamId}/leave`

팀에서 탈퇴한다. 팀장은 탈퇴할 수 없다.

| 항목 | 내용 |
| --- | --- |
| 인증 | 필요 |
| Path Parameter | `teamId` |
| Response Data | 없음 |
| 주요 에러 | `UNAUTHORIZED`, `FORBIDDEN`, `CANNOT_LEAVE_LEADER` |

Response:

```json
{
  "success": true,
  "message": "팀 탈퇴가 완료되었습니다.",
  "errorCode": null,
  "data": null
}
```

### DELETE `/api/teams/{teamId}`

팀을 삭제한다. 팀장만 삭제할 수 있으며, 실제 row 삭제가 아니라 `delflag = 'Y'`로 처리한다.

| 항목 | 내용 |
| --- | --- |
| 인증 | 필요 |
| Path Parameter | `teamId` |
| Response Data | 없음 |
| 주요 에러 | `UNAUTHORIZED`, `TEAM_NOT_FOUND`, `FORBIDDEN` |

Response:

```json
{
  "success": true,
  "message": "팀이 삭제되었습니다.",
  "errorCode": null,
  "data": null
}
```

## 7. Health Check

### GET `/api/health`

서버 상태를 확인한다.

| 항목 | 내용 |
| --- | --- |
| 인증 | 불필요 |
| Response Data | `"OK"` |

Response:

```json
{
  "success": true,
  "message": "서버가 정상적으로 실행 중입니다.",
  "errorCode": null,
  "data": "OK"
}
```

## 8. 주요 에러 코드

| Error Code | 의미 |
| --- | --- |
| `VALIDATION_ERROR` | 요청 값 검증 실패 |
| `UNAUTHORIZED` | 인증되지 않은 요청 |
| `INVALID_TOKEN` | JWT가 없거나 유효하지 않음 |
| `FORBIDDEN` | 권한 없음 |
| `USER_NOT_FOUND` | 사용자를 찾을 수 없음 |
| `DUPLICATE_EMAIL` | 이미 사용 중인 이메일 |
| `DUPLICATE_NICKNAME` | 이미 사용 중인 닉네임 |
| `INVALID_PASSWORD` | 비밀번호 불일치 또는 로그인 실패 |
| `INVALID_AUTH_PROVIDER` | 로그인 방식 불일치 |
| `GOOGLE_USER_PASSWORD_RESET_NOT_ALLOWED` | Google 사용자의 비밀번호 변경/재설정 불가 |
| `INVALID_VERIFICATION_CODE` | 인증번호 불일치 |
| `EXPIRED_VERIFICATION_CODE` | 인증번호 만료 |
| `MAIL_SEND_FAILED` | 메일 발송 실패 |
| `TEAM_NOT_FOUND` | 팀을 찾을 수 없음 |
| `INVALID_TEAM_CODE` | 유효하지 않은 팀 코드 |
| `ALREADY_JOINED_TEAM` | 이미 참여 중인 팀 |
| `CANNOT_LEAVE_LEADER` | 팀장은 팀 탈퇴 불가 |
| `DUPLICATE_RESOURCE` | 중복 리소스 생성 충돌 |
| `INTERNAL_SERVER_ERROR` | 서버 내부 오류 |
