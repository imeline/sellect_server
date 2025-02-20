package com.sellect.server.config;

import com.sellect.server.common.infrastructure.jwt.JwtFilter;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private static final String[] AUTH_WHITELIST = {
        "/swagger-resources/**", "/swagger-ui/**", "/v3/api-docs/**"
    };
    private static final String[] NO_JWT_PATHS = {
        "/api/v1/auth/login", "/api/v1/auth/signup", "/api/v1/auth/seller/signup"
    };
    private static final String[] PUBLIC_PATHS = {
        "/api/v1/payment/success/**", "/api/v1/payment/fail", "/api/v1/payment/cancel",
        "/api/v1/coupon/actives", "/api/v1/auth/seller/login"
    };
    private final JwtFilter jwtFilter;

    private HttpSecurity commonConfig(HttpSecurity http) throws Exception {
        return http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(AbstractHttpConfigurer::disable)
            .formLogin(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable);
    }

    @Order(1)
    @Bean
    public SecurityFilterChain loginFilterChain(HttpSecurity http) throws Exception {
        http = commonConfig(http);
        http
            .securityMatcher(request -> Arrays.stream(NO_JWT_PATHS)
                .anyMatch(path -> path.equals(request.getRequestURI())))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(NO_JWT_PATHS).permitAll()
                .anyRequest().denyAll()
            );
        return http.build();
    }

    @Order(2)
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http = commonConfig(http);
        http
            .securityMatcher(request -> Arrays.stream(NO_JWT_PATHS)
                .noneMatch(path -> path.equals(request.getRequestURI())))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(AUTH_WHITELIST).permitAll()
                .requestMatchers(PUBLIC_PATHS).permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedOrigin("http://localhost:3000");
        configuration.addAllowedMethod("*");
        configuration.addAllowedHeader("*");
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}