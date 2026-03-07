package org.example.expert.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CheckAdminInterceptorTest{
    @Mock
    private Object handler;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @InjectMocks
    private CheckAdminInterceptor checkAdminInterceptor;

    @Test
    @DisplayName("성공 - interceptor 통과")
    void prehandler_succeed () throws IOException {

        //given
        String token ="thisisToken";

        when(request.getHeader("Authorization")).thenReturn("bearer "+token);
        when(request.getAttribute("userRole")).thenReturn("ADMIN");

        //when
        boolean result = checkAdminInterceptor.preHandle(request, response, handler);
        //then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("실패 - header 에 authorization 이 없음")
    void prehandler_failed_by_header_is_null() {

        when(request.getHeader("Authorization")).thenReturn(null);
        // when & then
        assertThatThrownBy(()->checkAdminInterceptor.preHandle(request, response, handler))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessage("토큰이 없습니다.");
    }

    @Test
    @DisplayName("실패 - getAttribute 가 null")
    void prehandler_failed_by_attribute_userRole_is_null() {
        //when
        String token ="thisisToken";

        when(request.getHeader("Authorization")).thenReturn("bearer "+token);
        when(request.getAttribute("userRole")).thenReturn(null);

        //when //then
        assertThatThrownBy(()->checkAdminInterceptor.preHandle(request, response, handler))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessage("역할 정보가 없습니다.");
    }

    @Test
    @DisplayName("실패 - role 이 user")
    void prehandler_failed_by_forbidden () {

        //given
        String token ="thisisToken";

        when(request.getHeader("Authorization")).thenReturn("bearer "+token);
        when(request.getAttribute("userRole")).thenReturn("USER");

        assertThatThrownBy(()->checkAdminInterceptor.preHandle(request, response, handler))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessage("관리자 아닌디?");
    }
}