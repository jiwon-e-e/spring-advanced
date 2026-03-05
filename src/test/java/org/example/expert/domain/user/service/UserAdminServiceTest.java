package org.example.expert.domain.user.service;

import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.dto.request.UserRoleChangeRequest;
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
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserAdminServiceTest {
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private UserAdminService userAdminService;

    @Test
    @DisplayName("성공 - 변경")
    void changeUserRole_succeed() {
        //given
        Long userId = 1L;
        String userRoleString = "ADMIN";
        UserRoleChangeRequest request = new UserRoleChangeRequest(userRoleString);

        User user = new User("user@example.com", "1234", UserRole.USER);
        ReflectionTestUtils.setField(user, "id", userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        //when
        userAdminService.changeUserRole(userId, request);

        //then
        assertThat(user.getUserRole()).isEqualTo(UserRole.ADMIN);
    }

    @Test
    @DisplayName("실패 - 존재하지 않는 사용자")
    void changeUserRole_failed_by_user_not_found() {
        //given
        Long userId = 1L;
        String userRoleString = "ADMIN";
        UserRoleChangeRequest request = new UserRoleChangeRequest(userRoleString);

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        //when
        //then
        assertThatThrownBy(()->userAdminService.changeUserRole(userId, request))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessage("User not found")
        ;
    }

    @Test
    @DisplayName("성공 - 변경")
    void changeUserRole() {
        //given
        Long userId = 1L;
        String userRoleString = "USER";
        UserRoleChangeRequest request = new UserRoleChangeRequest(userRoleString);

        User user = new User("user@example.com", "1234", UserRole.USER);
        ReflectionTestUtils.setField(user, "id", userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        //when
        //then
        assertThatThrownBy(()-> userAdminService.changeUserRole(userId, request))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessage("동일한 역할로 변경할 수 없습니다.");
    }
}