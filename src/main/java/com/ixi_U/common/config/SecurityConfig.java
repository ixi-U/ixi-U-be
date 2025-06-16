package com.ixi_U.common.config;

import com.ixi_U.auth.handler.OAuth2SuccessHandler;
import com.ixi_U.auth.service.CustomOAuth2UserService;
import com.ixi_U.jwt.JwtAuthenticationFilter;
import com.ixi_U.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer.FrameOptionsConfig;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.web.OAuth2LoginAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/*
 jwt 인증 + OAuth2 로그인(카카오) 환경에서 사용자의 인증/인가를 처리하도록 구현
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider; // jwt 토큰 생성/검증 클래스
    private final CustomOAuth2UserService customOAuth2UserService; // 카카오 로그인 후 받아온 사용자 정보를 파싱하여 DTO로 매핑

    // 소셜 로그인 성공 시 동작 정의
    @Bean
    public OAuth2SuccessHandler oAuth2SuccessHandler() {
        return new OAuth2SuccessHandler(jwtTokenProvider);
    }

    // Spring security 필터를 적용하지 않을 경로
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.ignoring()
                .requestMatchers("/error", "/favicon.ico");
    }

    // 필터 체인 설정
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        // Security Option 설정 : csrf, 폼 로그인, 기본 인증 등을 사용하지 않고 jwt 기반으로 동작하겠다.
        http
//                .cors(cors->cors.disable())
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .headers(headers -> headers.frameOptions(
                        FrameOptionsConfig::disable
                ))
                .sessionManagement(c -> // 세션 만들지 않음 : 모든 인증은 jwt로만 처리 (stateless)
                        c.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        ;
        // JWT 필터 추가 : JWT 인증 필터를 OAuth2 인증 필터 이후에 등록 -> 쿠키로 받은 access token 을 읽고, 검증 후 Security Context에 사용자 정보 설정
        http
                .addFilterAfter(new JwtAuthenticationFilter(jwtTokenProvider),
                        OAuth2LoginAuthenticationFilter.class);

        // OAuth2 필터
        http
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService))
                        .successHandler(oAuth2SuccessHandler())
                );
        // 인가 필터
        http
                .authorizeHttpRequests(auth -> auth
//                        .requestMatchers("/login/**", "/oauth2/**", "/public/**", "/**").permitAll()
                                .requestMatchers("/login/**", "/oauth2/**", "/public/**", "/plans/**").permitAll()
                                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
//                        .requestMatchers("/api/user/me", "/api/auth/**").authenticated()
                                .anyRequest().authenticated()
                );
        return http.build();
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("http://localhost:3000")
//                        .allowedOrigins("http://localhost:3000", "https://ixi-u.site")
                        .allowedMethods("*")
                        .allowCredentials(true);
            }
        };
    }
}

