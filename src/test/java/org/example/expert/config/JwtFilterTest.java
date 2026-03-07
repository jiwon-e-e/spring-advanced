package org.example.expert.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.InvalidClaimException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.mockito.InjectMocks;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import io.jsonwebtoken.Claims;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SpringBootTest
class JwtFilterTest {

    @InjectMocks
    private JwtFilter jwtFilter;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private FilterChain filterChain;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        MockitoAnnotations.openMocks(this);
        jwtFilter = new JwtFilter(jwtUtil, objectMapper);
    }

    @Test
    @DisplayName("성공 - JWT 필터 통과, request attribute 세팅")
    void doFilter_succeed() throws Exception {
        //given
        String token = "thisistoken";

        when(request.getRequestURI()).thenReturn("/users/1"); //auth 가 아닌 값
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtil.substringToken("Bearer " + token)).thenReturn(token);

        Claims claims = mock(Claims.class);
        when(jwtUtil.extractClaims(token)).thenReturn(claims);
        when(claims.getSubject()).thenReturn("1");
        when(claims.get("email", String.class)).thenReturn("testUser@email.com");
        when(claims.get("userRole", String.class)).thenReturn("USER");

        //when
        jwtFilter.doFilter(request, response, filterChain);
        //then
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("성공 - auth 라서 통과 ")
    void doFilter_succeed_auth() throws Exception {
        //given
        when(request.getRequestURI()).thenReturn("/auth/signup"); //auth
        //when
        jwtFilter.doFilter(request, response, filterChain);
        //then
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("실패 - 존재하지 않는 토큰")
    void doFilter_failed_by_jwt_is_null() throws Exception {
        //given
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);

        when(request.getRequestURI()).thenReturn("/users/1");
        when(request.getHeader("Authorization")).thenReturn(null);
        when(response.getWriter()).thenReturn(printWriter);

        //when
        jwtFilter.doFilter(request, response, filterChain);
        //then
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(filterChain, times(0)).doFilter(request, response);
        String result = stringWriter.toString();
        assertThat(result).contains("인증이 필요합니다.");
        assertThat(result).contains("UNAUTHORIZED");
    }

    @Test
    @DisplayName("실패 - claims 가 null")
    void doFilter_claims_is_null() throws Exception {
        //given
        String token = "thisisToken";
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);

        when(request.getRequestURI()).thenReturn("/users/1");
        when(request.getHeader("Authorization")).thenReturn("bearer "+token);
        when(jwtUtil.extractClaims(token)).thenReturn(null);
        when(response.getWriter()).thenReturn(printWriter);

        //when
        jwtFilter.doFilter(request, response, filterChain);
        //then
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(filterChain, times(0)).doFilter(request, response);
        String result = stringWriter.toString();
        assertThat(result).contains("인증이 필요합니다.");
        assertThat(result).contains("UNAUTHORIZED");
    }

    @Test
    @DisplayName("실패 - 만료된 토큰")
    void doFilter_expiredJwt_returnsUnauthorized() throws Exception {
        //given

        String token = "thisis.expired.token";
        Claims claims = mock(Claims.class);
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);

        when(request.getRequestURI()).thenReturn("/users/1");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtil.substringToken("Bearer " + token)).thenReturn(token);
        when(response.getWriter()).thenReturn(printWriter);
        when(jwtUtil.extractClaims(token)).thenThrow(new ExpiredJwtException(null, claims, "인증이 필요합니다."));

        // when
        jwtFilter.doFilter(request, response, filterChain);
        // then
        verify(filterChain, never()).doFilter(request, response);
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        String result = stringWriter.toString();
        assertThat(result).contains("인증이 필요합니다.");
    }

    @Test
    @DisplayName("실패 - 검증이 되지 않는 토큰")
    void doFilter_badRequest() throws Exception {
        //given

        String token = "thisis.Malformed.token";
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);

        when(request.getRequestURI()).thenReturn("/users/1");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtil.substringToken("Bearer " + token)).thenReturn(token);
        when(response.getWriter()).thenReturn(printWriter);
        when(jwtUtil.extractClaims(token)).thenThrow(new SecurityException("인증이 필요합니다."));

        //when
        jwtFilter.doFilter(request, response, filterChain);

        //then
        verify(filterChain, never()).doFilter(request, response);
        verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        String result = stringWriter.toString();
        assertThat(result).contains("인증이 필요합니다.");
    }


    @Test
    @DisplayName("실패 - 예상치 못한 오류")
    void doFilter_500() throws Exception {
        //given
        String token = "thisistoken";
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);


        when(request.getRequestURI()).thenReturn("/users/1");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtil.substringToken("Bearer " + token)).thenReturn(token);
        when(response.getWriter()).thenReturn(printWriter);
        when(jwtUtil.extractClaims(token)).thenThrow(new RuntimeException("요청 처리 중 오류가 발생했습니다."));

        //when
        jwtFilter.doFilter(request, response, filterChain);
        //then
        verify(filterChain, never()).doFilter(request, response);
        verify(response).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        String result = stringWriter.toString();
        assertThat(result).contains("요청 처리 중 오류가 발생했습니다.");
    }
}