package com.min.edu.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class SecurityConfig {

    /*
     * 초기 개발 단계에서는 JWT 인증 구조가 아직 구현되지 않았기 때문에
     * 모든 요청을 임시로 허용한다.
     *
     * 프론트엔드는 Vite 개발 서버인 localhost:5173에서 실행되고,
     * 백엔드는 localhost:8080에서 실행되므로 CORS 허용 설정이 필요하다.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                /*
                 * React에서 백엔드 API를 호출할 수 있도록 CORS 설정을 활성화한다.
                 */
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                /*
                 * REST API 서버에서는 JSON 요청을 주고받을 예정이므로
                 * 초기 개발 단계에서는 CSRF를 비활성화한다.
                 */
                .csrf(csrf -> csrf.disable())

                /*
                 * TODO: JWT 인증 구현 후 회원가입, 로그인, 공개 질문 조회 API만 permitAll로 열어두고,
                 * 나머지 API는 인증이 필요하도록 수정한다.
                 */
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/health").permitAll()
                        .anyRequest().permitAll()
                );

        return http.build();
    }

    /*
     * Vite React 개발 서버에서 Spring Boot API를 호출할 수 있도록 허용한다.
     * 현재 프론트 개발 서버 주소는 http://localhost:5173 이다.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(List.of("http://localhost:5173"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}