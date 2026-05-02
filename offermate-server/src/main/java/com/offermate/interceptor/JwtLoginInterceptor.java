package com.offermate.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.offermate.common.Result;
import com.offermate.constant.RedisConstants;
import com.offermate.dto.LoginUserDTO;
import com.offermate.service.RedisCacheService;
import com.offermate.util.JwtUtil;
import com.offermate.util.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class JwtLoginInterceptor implements HandlerInterceptor {

    private static final String TOKEN_PREFIX = "Bearer ";
    private static final Pattern JOB_DETAIL_PATTERN = Pattern.compile("^/api/jobs/\\d+$");
    private static final Pattern COMPANY_DETAIL_PATTERN = Pattern.compile("^/api/company/\\d+$");

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;
    private final RedisCacheService redisCacheService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (isPublicJobApi(request)) {
            return true;
        }

        String authorization = request.getHeader("Authorization");
        if (!StringUtils.hasText(authorization)) {
            writeUnauthorized(response, "未登录");
            return false;
        }

        String token = authorization;
        if (authorization.startsWith(TOKEN_PREFIX)) {
            token = authorization.substring(TOKEN_PREFIX.length());
        }

        if (!StringUtils.hasText(token) || !jwtUtil.validateToken(token)) {
            writeUnauthorized(response, "未登录");
            return false;
        }

        Boolean blacklisted = redisCacheService.hasKey(RedisConstants.TOKEN_BLACKLIST_KEY + token);
        if (Boolean.TRUE.equals(blacklisted)) {
            writeUnauthorized(response, "登录已失效，请重新登录");
            return false;
        }

        LoginUserDTO loginUser = jwtUtil.getLoginUser(token);
        UserContext.setUser(loginUser);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.removeUser();
    }

    private void writeUnauthorized(HttpServletResponse response, String msg) throws Exception {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(Result.error(Result.UNAUTHORIZED_CODE, msg)));
    }

    private boolean isPublicJobApi(HttpServletRequest request) {
        if (!"GET".equalsIgnoreCase(request.getMethod())) {
            return false;
        }
        String uri = request.getRequestURI();
        return "/api/jobs/page".equals(uri)
                || "/api/jobs/search".equals(uri)
                || JOB_DETAIL_PATTERN.matcher(uri).matches()
                || COMPANY_DETAIL_PATTERN.matcher(uri).matches();
    }
}
