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

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomOAuth2UserService customOAuth2UserService;

    @Bean
    public OAuth2SuccessHandler oAuth2SuccessHandler() {
        return new OAuth2SuccessHandler(jwtTokenProvider);
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() { // security 를 적용하지 않을 리소스
        return web -> web.ignoring()
                .requestMatchers("/error", "/favicon.ico");
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        // Security Option 설정
        http
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .headers(headers -> headers.frameOptions(
                        FrameOptionsConfig::disable
                ))
                .sessionManagement(c ->
                        c.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        ;
        // JWT 필터
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

        // 기능 확인용 인가 필터
        http
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                );

        return http.build();

        // 인가 필터
//        http
//                .authorizeHttpRequests(auth -> auth
//                        // CORS preflight 요청은 인증 없이 통과
//                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
//                        // 해당 요청에 대해서는 권한 확인
//                        .requestMatchers("/admin/**").hasRole("ADMIN")
//                        .requestMatchers("/user/**").hasAnyRole("USER", "ADMIN")
//                        // 다음 요청에 대해서는 누구나 접근 허용
//                        .requestMatchers("/login/**", "/oauth2/**", "/public/**", "/plans/**", "/plans/names/**", "/api/user/onboarding/**", "/**").permitAll()
//                        // 그 외 모든 요청은 인증 인가 필요
//                        .anyRequest().authenticated()
//                );
//        return http.build();
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("http://localhost:3000", "https://ixi-u.site")
                        .allowedMethods("*")
                        .allowCredentials(true);
            }
        };
    }
}
