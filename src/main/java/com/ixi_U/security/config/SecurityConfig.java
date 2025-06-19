package com.ixi_U.security.config;

import com.ixi_U.security.exception.CustomAccessDeniedHandler;
import com.ixi_U.security.exception.CustomAuthenticationEntryPoint;
import com.ixi_U.security.jwt.filter.JwtAuthenticationFilter;
import com.ixi_U.security.oauth2.handler.OAuth2SuccessHandler;
import com.ixi_U.security.oauth2.service.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer.FrameOptionsConfig;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() { // security 를 적용하지 않을 리소스
        return web -> web.ignoring()
                .requestMatchers("/error", "/favicon.ico");
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable) // csrf 비활성화, 쿠키 사용시, 활성화 필요
                .formLogin(AbstractHttpConfigurer::disable) // 기본 로그인 비활성화
                .logout(AbstractHttpConfigurer::disable) // 기본 로그아웃 비활성화
                .httpBasic(AbstractHttpConfigurer::disable) // 기본 인증 로그인 비활성화
                .headers(headers -> headers.frameOptions(
                        FrameOptionsConfig::disable)) // X-Frame-Options 비활성화
                .sessionManagement(c ->
                        c.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 세션 사용하지 않음
        ;

        http    //커스텀 필터들 추가
                .addFilterBefore(jwtAuthenticationFilter, OAuth2AuthorizationRequestRedirectFilter.class);
//                .addFilterBefore(customLogoutFilter(), LogoutFilter.class);

        http
                .oauth2Login(oauth2 -> oauth2
                        // 로그인
                        .loginPage("/login")

                        // SNS 로그인 화면 요청 URI / default : "/oauth2/authorization/{로그인할 SNS 타입 ex) kakao}"
                        .authorizationEndpoint(authorization -> authorization
                                .baseUri("/login"))

                        // 인가 코드 리다이렉션 URI / default : "/oauth2/authorization/{registrationId}"
                        .redirectionEndpoint(redirection -> redirection
                                .baseUri("/login/code/{registrationId}"))

                        // OAuth2 로그인 성공 시 반환 정보 처리 로직
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService))

                        .successHandler(oAuth2SuccessHandler)

                );

        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login/**", "/oauth/**").permitAll()
                        .requestMatchers("/test/**").permitAll()
                        .requestMatchers("/**").permitAll()
                        .anyRequest().authenticated()
                );

        http    //예외 발생시 예외 처리 핸들러들
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(customAuthenticationEntryPoint)
                        .accessDeniedHandler(customAccessDeniedHandler));

        return http.build();
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("http://localhost:3000", "https://ixiu.site",
                                "https://www.ixiu.site")
                        .allowedMethods("*")
                        .allowCredentials(true);
            }
        };
    }
}
