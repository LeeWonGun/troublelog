# JWT HttpOnly Cookie 인증 구현 정리

정리 기준: 2026-06-30

## 1. 현재 인증 방식

현재 TroubleLog 인증은 JWT 기반이다.

다만 JWT를 응답 body로 내려주거나 프론트 저장소에 저장하지 않는다. 서버가 JWT를 `HttpOnly Cookie`에 담아 내려주고, 브라우저가 이후 요청마다 쿠키를 자동으로 전송한다.

즉 내부 인증 방식은 JWT이고, 전달 방식은 쿠키 방식이다.

```text
로그인 성공
-> 서버가 JWT 생성
-> ACCESS_TOKEN HttpOnly 쿠키로 응답
-> 브라우저가 쿠키 저장
-> 이후 API 요청마다 쿠키 자동 전송
-> 백엔드가 쿠키의 JWT 검증
```

## 2. 왜 이렇게 했는가

초기 설계 방향은 JWT였다.

하지만 일반적인 JWT 방식처럼 프론트가 `accessToken`을 응답 body로 받고 `localStorage`나 `sessionStorage`에 저장하면, XSS 상황에서 토큰이 노출될 수 있다.

이를 줄이기 위해 JWT는 유지하되, 프론트 JavaScript가 읽을 수 없는 `HttpOnly Cookie`로 전달하도록 바꿨다.

## 3. 로그인 응답

로그인 성공 시 응답 body에는 JWT 값이 없다.

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

JWT는 `Set-Cookie` 헤더로만 전달된다.

```http
Set-Cookie: ACCESS_TOKEN=jwt-value; Path=/; Max-Age=86400; HttpOnly; SameSite=Lax
```

운영 HTTPS 환경에서는 `Secure` 옵션도 붙인다.

## 4. 프론트 요청 방식

프론트는 JWT 값을 직접 다루지 않는다.

API 요청에는 쿠키가 포함되도록 `credentials: "include"`만 설정한다.

```js
fetch("http://localhost:8080/api/auth/me", {
  credentials: "include"
});
```

더 이상 사용하지 않는 방식:

```http
Authorization: Bearer <accessToken>
```

## 5. 백엔드 인증 흐름

보호 API 요청이 들어오면 `JwtCookieAuthenticationFilter`가 실행된다.

```text
요청 수신
-> ACCESS_TOKEN 쿠키 조회
-> JWT 서명 검증
-> JWT 만료 검증
-> subject에서 userId 추출
-> DB에서 활성 사용자 확인
-> Spring Security Authentication 등록
```

탈퇴 처리된 사용자는 JWT가 아직 만료되지 않았더라도 인증으로 인정하지 않는다.

## 6. 주요 파일

| 역할 | 파일 |
| --- | --- |
| JWT 생성/검증 | `auth/security/JwtTokenProvider.java` |
| JWT 쿠키 생성/삭제 | `auth/security/JwtCookieService.java` |
| JWT 쿠키 인증 필터 | `auth/security/JwtCookieAuthenticationFilter.java` |
| Security 설정 | `common/config/SecurityConfig.java` |
| 로그인/로그아웃 API | `auth/controller/AuthController.java` |
| Google OAuth 성공 처리 | `auth/security/GoogleOAuth2SuccessHandler.java` |

## 7. 로그아웃

로그아웃은 서버 세션을 삭제하는 것이 아니라 `ACCESS_TOKEN` 쿠키를 만료시킨다.

```http
Set-Cookie: ACCESS_TOKEN=; Path=/; Max-Age=0; HttpOnly; SameSite=Lax
```

## 8. 환경 변수

```properties
JWT_SECRET=change-this-to-a-long-random-secret-at-least-32-bytes
JWT_EXPIRATION_MS=86400000
JWT_COOKIE_SECURE=false
JWT_COOKIE_SAME_SITE=Lax
```

운영 서버가 HTTPS이면 다음처럼 설정한다.

```properties
JWT_COOKIE_SECURE=true
```

## 9. 보안상 주의점

- JWT가 프론트 JS에 노출되지 않는다.
- `localStorage`, `sessionStorage`에 토큰을 저장하지 않는다.
- 쿠키 기반 전달이므로 CSRF 방어가 중요하다.
- 현재는 `SameSite=Lax`로 기본 방어를 적용했다.
- 운영 수준에서는 CSRF 토큰 적용까지 고려하는 것이 좋다.

## 10. 한 줄 요약

JWT 인증 구조는 유지하되, JWT를 프론트에 직접 넘기지 않고 `HttpOnly Cookie`로만 주고받는 방식이다.
