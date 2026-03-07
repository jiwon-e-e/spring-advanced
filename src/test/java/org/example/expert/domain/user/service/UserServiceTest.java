package org.example.expert.domain.user.service;

import org.example.expert.config.PasswordEncoder;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.dto.request.UserChangePasswordRequest;
import org.example.expert.domain.user.dto.response.UserResponse;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("성공 - 유저 반환")
    void getUser_success() {
        // given
        Long userId = 1L;
        User testUser = new User("test@email.com", "1234", UserRole.USER);
        ReflectionTestUtils.setField(testUser, "id", userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        // when
        UserResponse response = userService.getUser(userId);


        // then
        assertThat(response.getId()).isEqualTo(userId);
        assertThat(response.getEmail()).isEqualTo("test@email.com");

    }

    @Test
    @DisplayName("실패 - 유저 찾기 실패")
    void getUser_failed_by_user_not_found() {
        // given
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // when & then

        assertThatThrownBy(()-> userService.getUser(userId))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessage("User not found");

    }

    // ------------------------------------------------------------------------------------------
    @Test
    @DisplayName("성공 - 비밀번호 수정 성공")
    void changePassword_success(){

        //given
        Long userId = 1L;
        String oldPassword = "1234";
        String newPassword = "5678";
        UserChangePasswordRequest request = new UserChangePasswordRequest(oldPassword, newPassword);

        String testUserPassword = "1234";
        String encodedPassword = "encodedPassword";

        User testUser = new User("test@email.com", testUserPassword, UserRole.USER);
        ReflectionTestUtils.setField(testUser, "id", userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(oldPassword, testUserPassword)).thenReturn(true);
        when(passwordEncoder.matches(newPassword, testUserPassword)).thenReturn(false);
        when(passwordEncoder.encode(newPassword)).thenReturn(encodedPassword);

        //when
        userService.changePassword(userId, request);

        //then
        assertThat(testUser.getPassword()).isEqualTo(encodedPassword);
        verify(passwordEncoder).encode(newPassword);
    }



    @Test
    @DisplayName("실패 - 수정하려는 유저가 존재하지 않음")
    void changePassword_failed_by_user_not_found() {
        // given
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // when & then

        assertThatThrownBy(()-> userService.getUser(userId))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessage("User not found");
    }

    @Test
    @DisplayName("실패 - 비밀번호 오류")
    void changePassword_failed_by_unauthorized(){
        //given
        Long userId = 1L;
        String newPassword = "1234";
        String oldPassword = "5678";
        User testUser = new User("test@email.com", "1234", UserRole.USER);
        ReflectionTestUtils.setField(testUser, "id", userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(oldPassword, testUser.getPassword())).thenReturn(false);


        UserChangePasswordRequest request = new UserChangePasswordRequest(oldPassword, newPassword);

        //when
        //then

        assertThatThrownBy(()-> userService.changePassword(userId, request))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessage("잘못된 비밀번호입니다.");

    }

    @Test
    @DisplayName("실패 - 새 비밀번호와 기존 비밀번호가 동일")
    void changePassword_failed_by_password_conflict(){
        //given
        Long userId = 1L;
        String newPassword = "1234";
        String oldPassword = "1234";
        User testUser = new User("test@email.com", "1234", UserRole.USER);
        ReflectionTestUtils.setField(testUser, "id", userId);
        UserChangePasswordRequest request = new UserChangePasswordRequest(oldPassword, newPassword);


        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(oldPassword, testUser.getPassword())).thenReturn(true);
        when(passwordEncoder.matches(newPassword, testUser.getPassword())).thenReturn(true);

        //when
        //then

        assertThatThrownBy(()-> userService.changePassword(userId, request))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessage("새 비밀번호는 기존 비밀번호와 같을 수 없습니다.");
    }
}