package org.example.expert.config;

import org.example.expert.domain.common.exception.ServerException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.StringUtils;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
@ExtendWith(MockitoExtension.class)
class JwtUtilTest {

    @InjectMocks
    private JwtUtil jwtUtil;

    @Test
    void substringToken() {
        //given

        String tokenValue = "ihaveacat.tako.cute"; //bearer 으로 시작하지 않고
        // 7글자는 넘음

        assertThatThrownBy(()->jwtUtil.substringToken(tokenValue))
                .isInstanceOf(ServerException.class)
                .hasMessage("Not Found Token");
    }
}