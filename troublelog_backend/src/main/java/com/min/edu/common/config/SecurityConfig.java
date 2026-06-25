package com.min.edu.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    /*
     * 초기 개발 단계에서는 JWT 인증 구조가 아직 구현되지 않았기 때문에
     * 모든 요청을 임시로 허용한다.
     *
     * 추후 JWT 인증 기능을 구현하면 아래 설정에서
     * 회원가입, 로그인, 공개 질문 조회 API만 permitAll로 열어두고,
     * 팀, 질문 작성, 답변 작성, 좋아요, 마이페이지 API는 인증이 필요하도록 수정한다.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                /*
                 * REST API 서버에서는 세션 기반 화면 요청이 아니라
                 * JSON 요청을 주고받을 예정이므로 초기 개발 단계에서는 CSRF를 비활성화한다.
                 */
                .csrf(csrf -> csrf.disable())

                /*
                 * TODO: JWT 인증 구현 후 권한 규칙에 맞게 requestMatchers를 세분화해야 한다.
                 * 현재는 공통 응답 구조와 health check API 테스트를 위해 모든 요청을 허용한다.
                 */
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/health").permitAll()
                        .anyRequest().permitAll()
                );

        return http.build();
    }
}