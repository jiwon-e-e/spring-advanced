package org.example.expert.domain.comment.controller;

import org.example.expert.config.FilterConfig;
import org.example.expert.config.WebConfig;
import org.example.expert.domain.comment.service.CommentAdminService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = CommentAdminController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {FilterConfig.class, WebConfig.class}
        )
)
@AutoConfigureMockMvc(addFilters = false)
class CommentAdminControllerTest {

    // 사실상 Controller 하나만 테스트하는일은 잘 없다
    // controller 가 어떻게 요청을 받기전에 Filter , interceptor 등을 거치는지 아는게 더 중요하기 때문에
    // 통합테스트 위주로 진행함
    // 하지만 controller 내부에 로직이 존재해서 테스트가 필요한 경우
    // excludeFilters 와 addFilters=false 를 사용하여 조정해주기

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CommentAdminService commentAdminService;

    @Test
    void deleteComment_succeed() throws Exception {
        // given
        long commentId = 1L;

        // when & then
        mockMvc.perform(delete("/admin/comments/{commentId}", commentId))
                .andExpect(status().isOk());

        verify(commentAdminService, times(1)).deleteComment(commentId);
    }
}