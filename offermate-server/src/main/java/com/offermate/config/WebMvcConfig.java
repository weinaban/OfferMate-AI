package com.offermate.config;

import com.offermate.interceptor.JwtLoginInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final JwtLoginInterceptor jwtLoginInterceptor;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtLoginInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/api/user/register",
                        "/api/user/login",
                        "/ws/chat",
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/doc.html",
                        "/webjars/**"
                );
    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(String.class, LocalDateTime.class, source -> {
            if (source == null || source.isBlank()) {
                return null;
            }
            String value = source.trim();
            if (value.contains("T")) {
                return LocalDateTime.parse(value);
            }
            return LocalDateTime.parse(value, DATE_TIME_FORMATTER);
        });
    }
}
