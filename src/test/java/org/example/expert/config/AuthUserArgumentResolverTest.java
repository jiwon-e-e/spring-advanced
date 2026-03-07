package org.example.expert.config;

import org.example.expert.domain.auth.exception.AuthException;
import org.example.expert.domain.common.annotation.Auth;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class AuthUserArgumentResolverTest {

    private final AuthUserArgumentResolver resolver = new AuthUserArgumentResolver();

    @Test
    @DisplayName("실패 - @Auth 어노테이션은 있는데 타입이 AuthUser가 아님")
    void supportsParameter_failed_by_not_AuthUser_type() {
        // given
        MethodParameter parameter = mock(MethodParameter.class);
        given(parameter.getParameterAnnotation(Auth.class)).willReturn(mock(Auth.class));
        given(parameter.getParameterType()).willReturn((Class) String.class);

        // when & then
        assertThatThrownBy(()->resolver.supportsParameter(parameter))
                .isInstanceOf(AuthException.class)
                .hasMessage("@Auth와 AuthUser 타입은 함께 사용되어야 합니다.");
    }
}