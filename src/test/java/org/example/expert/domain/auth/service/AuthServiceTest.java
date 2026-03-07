package org.example.expert.domain.auth.service;

import org.example.expert.config.JwtUtil;
import org.example.expert.config.PasswordEncoder;
import org.example.expert.domain.auth.dto.request.SigninRequest;
import org.example.expert.domain.auth.dto.request.SignupRequest;
import org.example.expert.domain.auth.dto.response.SigninResponse;
import org.example.expert.domain.auth.dto.response.SignupResponse;
import org.example.expert.domain.auth.exception.AuthException;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtUtil jwtUtil;
    @InjectMocks
    private AuthService authService;

    // 여기부턴 반복되는 로직을 따로 만들어서... 사용해보기!
    // 코드가 너무 길어지는듯함
    private final String email = "test@email.com";
    private final String password = "1234";

    @Test
    @DisplayName("성공 - 회원가입")
    void signup_succeed() {

        //given
        String email = "test@email.com";
        String password = "1234";
        String encodedPassword = "encoded1234";
        UserRole userRole = UserRole.USER;
        String bearerToken = "thisistoken";

        SignupRequest signupRequest = new SignupRequest(email, password, "USER");

        Long userId = 1L;
        User user = new User(email, encodedPassword, userRole);
        ReflectionTestUtils.setField(user, "id", userId);

        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(passwordEncoder.encode(password)).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(jwtUtil.createToken(userId, email, userRole)).thenReturn(bearerToken);

        //when
        SignupResponse response = authService.signup(signupRequest);

        //then
        verify(userRepository).save(any(User.class));
        assertThat(response.getBearerToken()).isEqualTo(bearerToken);
    }

    @Test
    @DisplayName("실패 - 중복된 이메일")
    void signup_failed_by_email_conflict() {
        //given
        String email = "test@email.com";
        String password = "1234";

        SignupRequest signupRequest = new SignupRequest(email, password, "USER");

        when(userRepository.existsByEmail(email)).thenReturn(true);

        //when
        //then
        assertThatThrownBy(()->authService.signup(signupRequest))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessage("이미 존재하는 이메일입니다.");
    }

    @Test
    @DisplayName("실패 - 잘못된 userRole")
    void signup_failed_by_invalid_userRole() {

        //given
        String email = "test@email.com";
        String password = "1234";
        String encodedPassword = "encoded1234";

        SignupRequest signupRequest = new SignupRequest(email, password, "CAT");

        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(passwordEncoder.encode(password)).thenReturn(encodedPassword);

        //when
        //then
        assertThatThrownBy(() -> authService.signup(signupRequest))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessage("유효하지 않은 UerRole");
    }

    // ------------------------------------------------------------
    @Test
    @DisplayName("성공 - 로그인")
    public void signin_succeed() {
        //given
        String bearerToken = "thisistoken";

        User user = getUser();

        SigninRequest signinRequest = getSigninRequest(email, password);


        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(password, user.getPassword())).thenReturn(true);
        when(jwtUtil.createToken(user.getId(), email, UserRole.USER)).thenReturn(bearerToken);

        //when
        SigninResponse response = authService.signin(signinRequest);

        //then
        verify(userRepository).findByEmail(email);
        verify(passwordEncoder).matches(password, user.getPassword());
        verify(jwtUtil).createToken(user.getId(), email, UserRole.USER);
        assertThat(response.getBearerToken()).isEqualTo(bearerToken);
    }

    @Test
    @DisplayName("실패 - 존재하지 않는 사용자")
    public void signin_failed_by_user_not_found() {
        //given
        SigninRequest signinRequest = getSigninRequest(email, password);

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        //when
        //then
        assertThatThrownBy(()->authService.signin(signinRequest))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessage("가입되지 않은 유저입니다.");
    }

    @Test
    @DisplayName("실패 - 비밀번호 불일치")
    public void signin_failed_by_incorrect_pw() {
        //given
        User user = getUser();
        SigninRequest signinRequest = getSigninRequest(email, "wrongPW");

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(signinRequest.getPassword(), user.getPassword())).thenReturn(false);

        //when
        //then
        assertThatThrownBy(()-> authService.signin(signinRequest))
                .isInstanceOf(AuthException.class)
                .hasMessage("잘못된 비밀번호입니다.");
    }

    // -----------------------------------------------------------
    private User getUser(){
        User user = new User(email, password, UserRole.USER);
        ReflectionTestUtils.setField(user, "id", 1L);

        return user;
    }

    private SigninRequest getSigninRequest(String email, String password){
        return new SigninRequest(email, password);
    }
}