package org.example.expert.config;

import org.example.expert.domain.common.exception.ServerException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.InjectMocks;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class JwtUtilTest {

    @InjectMocks
    private JwtUtil jwtUtil;

    @Test
    @DisplayName("실패 - 토큰 시작이 bearer 이 아님")
    void substringToken_failed_by_not_bearer_start() {
        //given

        String tokenValue = "ihaveacat.tako.cute"; //bearer 으로 시작하지 않고
        // 7글자는 넘음

        assertThatThrownBy(()->jwtUtil.substringToken(tokenValue))
                .isInstanceOf(ServerException.class)
                .hasMessage("Not Found Token");
    }
}