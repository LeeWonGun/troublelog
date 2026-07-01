# 회원/인증/팀 구현 정리

팀원 공유용 정리 기준: 2026-06-30

## 1. 담당 범위

담당 구현 범위는 회원/인증, 이메일 인증, JWT 인증, Google 소셜 로그인, 마이페이지 회원 정보, 팀 기능이다.

## 2. 인증 구조

현재 인증은 JWT 기반이다.

다만 JWT를 프론트에 직접 넘기지 않고 `HttpOnly Cookie`로 전달한다. 프론트 JavaScript는 JWT 값을 읽을 수 없고, 브라우저가 쿠키를 자동으로 요청에 포함한다.

## 3. 인증 흐름

```text
사용자 로그인
-> 이메일/비밀번호 검증
-> 서버가 JWT 생성
-> ACCESS_TOKEN HttpOnly 쿠키로 응답
-> 이후 요청에서 ACCESS_TOKEN 쿠키 확인
-> JWT 서명/만료 검증
-> DB에서 활성 사용자 확인
-> Spring Security 인증 객체 등록
```

JWT 검증은 `JwtCookieAuthenticationFilter`에서 처리한다.

## 4. 로그인 API

| Method | URL | 설명 | 인증 |
| --- | --- | --- | --- |
| POST | `/api/auth/signup` | 회원가입 | 불필요 |
| POST | `/api/auth/login` | 이메일/비밀번호 로그인 및 JWT 쿠키 발급 | 불필요 |
| POST | `/api/auth/logout` | JWT 쿠키 만료 | 필요 |
| GET | `/api/auth/me` | 현재 로그인 사용자 조회 | 필요 |

로그인 응답:

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

## 5. 프론트 요청 방식

프론트는 `Authorization` 헤더를 만들지 않는다.

```js
fetch("http://localhost:8080/api/auth/me", {
  credentials: "include"
});
```

## 6. 이메일 인증

회원가입과 비밀번호 찾기는 인증 메일을 발송하고, 인증 코드를 확인하는 방식으로 구현했다.

인증 코드는 원문 저장하지 않고 BCrypt 해시로 저장한다. 기본 만료 시간은 5분이다.

## 7. 메일 발송

현재 Gmail SMTP 방식으로 인증 메일을 발송할 수 있다.

```properties
MAIL_PROVIDER=smtp
MAIL_ENABLED=true
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USERNAME=your-gmail@gmail.com
SMTP_PASSWORD=your-google-app-password
```

## 8. 보안 포인트

- 비밀번호는 BCrypt로 암호화해서 저장한다.
- 인증 코드는 BCrypt 해시로 저장한다.
- JWT는 응답 body에 포함하지 않는다.
- JWT는 `HttpOnly Cookie`로만 전달한다.
- 프론트 저장소에 토큰을 저장하지 않는다.
- HTTPS 운영 환경에서는 `JWT_COOKIE_SECURE=true`를 사용해야 한다.
- 쿠키 기반 전달이므로 CSRF 토큰 적용을 추가로 고려해야 한다.

## 9. 발표용 요약

회원 기능에서는 이메일 기반 회원가입과 로그인을 구현했고, 비밀번호는 BCrypt로 암호화해 저장했습니다. 인증 방식은 JWT 기반이지만, JWT를 프론트에 직접 노출하지 않기 위해 `HttpOnly Cookie`로 전달합니다. 로그인 성공 시 서버가 JWT를 생성해 `ACCESS_TOKEN` 쿠키로 내려주고, 이후 요청에서는 백엔드 필터가 쿠키의 JWT를 검증해 현재 사용자를 식별합니다.
