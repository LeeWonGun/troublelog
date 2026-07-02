package com.min.edu.common.config;

import com.min.edu.auth.security.GoogleOAuth2SuccessHandler;
import com.min.edu.auth.security.JwtCookieAuthenticationFilter;
import com.min.edu.auth.security.JsonAccessDeniedHandler;
import com.min.edu.auth.security.JsonAuthenticationEntryPoint;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.http.HttpMethod;

import java.util.List;

@Configuration
public class SecurityConfig {

    private final JwtCookieAuthenticationFilter jwtCookieAuthenticationFilter;
    private final JsonAuthenticationEntryPoint authenticationEntryPoint;
    private final JsonAccessDeniedHandler accessDeniedHandler;
    private final GoogleOAuth2SuccessHandler googleOAuth2SuccessHandler;
    private final ObjectProvider<ClientRegistrationRepository> clientRegistrationRepositoryProvider;
    private final boolean csrfEnabled;

    public SecurityConfig(
            JwtCookieAuthenticationFilter jwtCookieAuthenticationFilter,
            JsonAuthenticationEntryPoint authenticationEntryPoint,
            JsonAccessDeniedHandler accessDeniedHandler,
            GoogleOAuth2SuccessHandler googleOAuth2SuccessHandler,
            ObjectProvider<ClientRegistrationRepository> clientRegistrationRepositoryProvider,
            @Value("${app.security.csrf.enabled:true}") boolean csrfEnabled
    ) {
        this.jwtCookieAuthenticationFilter = jwtCookieAuthenticationFilter;
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.accessDeniedHandler = accessDeniedHandler;
        this.googleOAuth2SuccessHandler = googleOAuth2SuccessHandler;
        this.clientRegistrationRepositoryProvider = clientRegistrationRepositoryProvider;
        this.csrfEnabled = csrfEnabled;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/health",
                                "/api/auth/signup",
                                "/api/auth/signup/send-code",
                                "/api/auth/signup/verify-code",
                                "/api/auth/login",
                                "/api/auth/check-email",
                                "/api/auth/check-nickname",
                                "/api/auth/csrf",
                                "/api/auth/password-reset/send-code",
                                "/api/auth/password-reset/verify-code",
                                "/api/auth/password-reset",
                                "/oauth2/**",
                                "/login/oauth2/**"
                        ).permitAll()

                        // 비회원도 조회 가능한 공개 API
                        .requestMatchers(HttpMethod.GET,
                                "/api/questions/public",
                                "/api/questions/popular",
                                "/api/questions/search",
                                "/api/questions/*",
                                "/api/questions/*/answers",
                                "/api/files/*",
                                "/api/tech-stacks"
                        ).permitAll()
                        
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtCookieAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        if (csrfEnabled) {
            http.csrf(csrf -> csrf
                    .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                    .ignoringRequestMatchers("/oauth2/**", "/login/oauth2/**")
            );
        } else {
            http.csrf(AbstractHttpConfigurer::disable);
        }

        if (clientRegistrationRepositoryProvider.getIfAvailable() != null) {
            http.oauth2Login(oauth2 -> oauth2.successHandler(googleOAuth2SuccessHandler));
        }

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(List.of(
                "http://localhost:5173",
                "http://127.0.0.1:5173"
        ));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}
