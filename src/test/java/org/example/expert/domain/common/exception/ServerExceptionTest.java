package org.example.expert.domain.common.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ServerExceptionTest {

    @Test
    @DisplayName("성공 - 메시지가 없으면 오류")
    void throwExceptionWhenDataNotFound() {
        // given
        String errorMessage = "인증이 필요합니다.";

        //when //then
        assertThatThrownBy(() -> {throw new ServerException(errorMessage);})
                .isInstanceOf(ServerException.class)
                .hasMessageContaining(errorMessage);
    }
}