# TroubleLog 현재 구현 정리

정리 기준: 2026-06-30

## 1. 구현 범위

- 이메일/비밀번호 회원가입
- 회원가입 이메일 인증
- 이메일/비밀번호 로그인
- JWT HttpOnly Cookie 인증
- Google OAuth2 로그인
- 비밀번호 찾기 및 재설정
- 마이페이지 사용자 정보 조회/수정
- 팀 생성, 참가, 조회, 탈퇴, 삭제
- Gmail SMTP 기반 인증 메일 발송
- 목업 API 테스트 페이지

## 2. 인증 방식

현재 인증은 JWT 기반이다.

로그인 성공 시 서버가 JWT를 생성하고, `ACCESS_TOKEN`이라는 `HttpOnly Cookie`에 담아 응답한다. 프론트는 JWT 값을 직접 읽거나 저장하지 않는다.

보호 API 요청 시에는 쿠키가 포함되도록 요청한다.

```js
fetch("/api/auth/me", {
  credentials: "include"
});
```

## 3. 인증 관련 주요 파일

| 역할 | 파일 |
| --- | --- |
| Security 설정 | `troublelog_backend/src/main/java/com/min/edu/common/config/SecurityConfig.java` |
| JWT 생성/검증 | `troublelog_backend/src/main/java/com/min/edu/auth/security/JwtTokenProvider.java` |
| JWT 쿠키 생성/삭제 | `troublelog_backend/src/main/java/com/min/edu/auth/security/JwtCookieService.java` |
| JWT 쿠키 인증 필터 | `troublelog_backend/src/main/java/com/min/edu/auth/security/JwtCookieAuthenticationFilter.java` |
| 인증 API | `troublelog_backend/src/main/java/com/min/edu/auth/controller/AuthController.java` |
| 인증 서비스 | `troublelog_backend/src/main/java/com/min/edu/auth/service/AuthService.java` |
| Google OAuth 성공 처리 | `troublelog_backend/src/main/java/com/min/edu/auth/security/GoogleOAuth2SuccessHandler.java` |

## 4. 로그인 응답

```json
{
  "success": true,
  "message": "로그인되었습니다.",
  "data": {
    "authType": "JWT_COOKIE",
    "user": {
      "userId": 1,
      "email": "user@example.com",
      "nickname": "userNick",
      "authProvider": "LOCAL"
    }
  }
}
```

응답 body에는 JWT가 포함되지 않는다. JWT는 `Set-Cookie` 헤더로만 전달된다.

## 5. 이메일 인증

회원가입과 비밀번호 찾기는 이메일 인증 코드를 사용한다.

- `POST /api/auth/signup/send-code`
- `POST /api/auth/signup/verify-code`
- `POST /api/auth/password-reset/send-code`
- `POST /api/auth/password-reset/verify-code`
- `PATCH /api/auth/password-reset`

인증 코드는 DB에 해시로 저장하고, 기본 만료 시간은 5분이다.

## 6. SMTP 메일 발송

현재 메일 발송은 Gmail SMTP를 사용할 수 있도록 구성되어 있다.

```properties
MAIL_PROVIDER=smtp
MAIL_ENABLED=true
MAIL_FROM=your-gmail@gmail.com
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USERNAME=your-gmail@gmail.com
SMTP_PASSWORD=your-google-app-password
```

## 7. JWT 쿠키 보안 설정

```properties
app.jwt.secret=${JWT_SECRET:...}
app.jwt.expiration-ms=${JWT_EXPIRATION_MS:86400000}
app.jwt.cookie.secure=${JWT_COOKIE_SECURE:false}
app.jwt.cookie.same-site=${JWT_COOKIE_SAME_SITE:Lax}
```

운영 서버가 HTTPS이면 `JWT_COOKIE_SECURE=true`로 설정한다.

## 8. 팀 기능

팀 기능은 JWT 쿠키에서 인증된 사용자 ID를 기준으로 동작한다.

- `POST /api/teams`: 팀 생성
- `POST /api/teams/join`: 팀 코드로 참가
- `GET /api/teams/my`: 내 팀 목록
- `GET /api/teams/{teamId}/members`: 팀원 목록
- `PATCH /api/teams/{teamId}/leave`: 팀 탈퇴
- `DELETE /api/teams/{teamId}`: 팀 삭제

팀장은 팀 탈퇴가 제한되고, 팀 삭제는 팀장만 가능하다.
