package org.example.expert.config;

import org.example.expert.domain.auth.exception.AuthException;
import org.example.expert.domain.common.exception.ServerException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GlobalExceptionHandler.class) // 테스트 대상이 되는 Advice 클래스
class GlobalExceptionHandlerTest {


    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new TestController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @RestController
    static class TestController {
        @GetMapping("/test/auth-error")
        public void throwAuthException() {
            throw new AuthException("인증 실패 테스트");
        }

        @GetMapping("/test/server-error")
        public void throwServerException() {
            throw new ServerException("서버 오류 테스트");
        }
    }

    @Test
    @DisplayName("성공 - AuthException -> 401")
    void handleAuthExceptionTest() throws Exception {
        mockMvc.perform(get("/test/auth-error"))
                .andExpect(status().isUnauthorized()) // 401 확인
                .andExpect(jsonPath("$.status").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("인증 실패 테스트"));
    }

    @Test
    @DisplayName("성공 - ServerException -> 500")
    void handleServerExceptionTest() throws Exception {
        mockMvc.perform(get("/test/server-error"))
                .andExpect(status().isInternalServerError()) // 500 확인
                .andExpect(jsonPath("$.status").value("INTERNAL_SERVER_ERROR"))
                .andExpect(jsonPath("$.message").value("서버 오류 테스트"));
    }
}