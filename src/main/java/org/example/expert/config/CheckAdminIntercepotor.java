package org.example.expert.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.enums.UserRole;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class CheckAdminIntercepotor implements HandlerInterceptor {
    private final JwtUtil jwtUtil;

    // pre -> 이전의 의미
    // 여기 controller method 가 존재
    // post 가 존재 (이후)
    public boolean preHandle (
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler) throws IOException{

        String jwt = jwtUtil.substringToken(request.getHeader("Authorization"));
        String role = (String) request.getAttribute("userRole");
        log.info(role +" "+UserRole.ADMIN);

        if (UserRole.of(role) != UserRole.ADMIN){
            log.warn("관리자 아님, 접근 거부 {}", jwtUtil.extractClaims(jwt).getSubject());
            throw new InvalidRequestException("관리자 아닌디?");
        }

        log.info("timestamp: {}", LocalDateTime.now());
        log.info("요청 method: {} {}", request.getMethod() ,request.getRequestURI());

        return true;
    }
}
