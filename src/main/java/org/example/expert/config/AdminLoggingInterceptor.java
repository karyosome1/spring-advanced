package org.example.expert.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.common.exception.ServerException;
import org.example.expert.domain.user.enums.UserRole;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.LocalDateTime;

// 4 API 로깅
@Slf4j
@Component
public class AdminLoggingInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 핸들러가 컨트롤러 메서드인지 확인
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true; // 컨트롤러 메서드가 아니면 인터셉트하지 않음
        }

        // 특정 컨트롤러 및 메서드만 필터링
        String controllerName = handlerMethod.getBeanType().getSimpleName();
        String methodName = handlerMethod.getMethod().getName();

        boolean isTargetMethod =
                ("CommentAdminController".equals(controllerName) && "deleteComment".equals(methodName)) ||
                        ("UserAdminController".equals(controllerName) && "changeUserRole".equals(methodName));

        if (!isTargetMethod) {
            return true; // 해당 메서드가 아니면 인터셉트하지 않음
        }

        // AuthUserArgumentResolver에서 설정한 사용자 정보 가져오기
        Long userId = (Long) request.getAttribute("userId");
        String email = (String) request.getAttribute("email");
        UserRole userRole = UserRole.of((String) request.getAttribute("userRole"));

        if (userId == null || email == null || userRole == null) {
            throw new ServerException("서버 인증 정보가 누락되었습니다.");
        }

        // 어드민 권한 확인
        if (userRole != UserRole.ADMIN) {
            throw new InvalidRequestException("어드민 권한이 필요합니다.");
        }

        // 로그 기록
        log.info("어드민 사용자 접근 - userId: {}, email: {}, URL: {}, 요청 시간: {}",
                userId, email, request.getRequestURI(), LocalDateTime.now());

        return true;
    }
}