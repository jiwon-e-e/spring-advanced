package org.example.expert.domain.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.expert.domain.auth.dto.request.SigninRequest;
import org.example.expert.domain.auth.dto.request.SignupRequest;
import org.example.expert.domain.auth.dto.response.SigninResponse;
import org.example.expert.domain.auth.dto.response.SignupResponse;
import org.example.expert.domain.auth.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private AuthService authService;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("성공 - 회원가입")
    void signup_succeed() throws Exception {

        // given
        String bearerToken = "thisisToken";
        SignupRequest request = new SignupRequest("test@email.com", "1234", "USER");

        SignupResponse response = new SignupResponse(bearerToken);

        when(authService.signup(any(SignupRequest.class))).thenReturn(response);

        // when
        // then
        mockMvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(authService).signup(any(SignupRequest.class));
    }

    @Test
    @DisplayName("실패 - valid오류: email 포맷")
    void signup_failed_by_validation() throws Exception {

        // given
        SignupRequest request = new SignupRequest("notEmailFormat", "1234", "USER");

        // when
        // then
        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("실패 - valid오류: 필수값 누락")
    void signup_failed_by_validation2() throws Exception {

        // given
        SignupRequest request = new SignupRequest("test@email.com", "", "USER");

        // when
        // then
        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // --------------------------------------------
    @Test
    @DisplayName("성공 - 로그인")
    void signin_succeed() throws Exception {

        // given
        String bearerToken = "thisisToken";
        SigninRequest request = new SigninRequest("test@email.com", "1234");

        SigninResponse response = new SigninResponse(bearerToken);

        when(authService.signin(any(SigninRequest.class))).thenReturn(response);

        // when
        // then
        mockMvc.perform(post("/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(authService).signin(any(SigninRequest.class));
    }

    @Test
    @DisplayName("실패 - 로그인: 필수값 누락")
    void signin_failed_by_essential_value_not_found() throws Exception {

        // given
        SigninRequest request = new SigninRequest("test@email.com", "");

        // when
        // then
        mockMvc.perform(post("/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}