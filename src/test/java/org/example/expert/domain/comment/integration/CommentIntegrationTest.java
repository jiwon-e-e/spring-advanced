package org.example.expert.domain.comment.integration;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.expert.config.JwtUtil;
import org.example.expert.domain.comment.dto.request.CommentSaveRequest;
import org.example.expert.domain.comment.entity.Comment;
import org.example.expert.domain.comment.repository.CommentRepository;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.repository.TodoRepository;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
public class CommentIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private TodoRepository todoRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtUtil jwtUtil;

    private String userToken;
    private String adminToken;

    private User user;
    private User adminUser;

    private Todo savedTodo;

    // 필요한 데이터
    // 일반 user, Admin user
    // 저장된 투두 가 필요
    // userToken 과 adminToken 필요

    @BeforeEach
    void setup(){
        user = userRepository.save(new User("testUser@email.com", "1234", UserRole.USER));
        adminUser = userRepository.save(new User("testAdmin@email.com", "1234", UserRole.ADMIN));

        savedTodo = todoRepository.save(new Todo("title", "contents", "SUNNY", user));

        userToken = jwtUtil.createToken(user.getId(), user.getEmail(), user.getUserRole());
        adminToken = jwtUtil.createToken(adminUser.getId(), adminUser.getEmail(), adminUser.getUserRole());
        // 와~
    }
    // 댓글 저장
    @Test
    @DisplayName("성공 - 댓글 저장")
    void save_comment_succeed() throws Exception{
        //given
        CommentSaveRequest commentSaveRequest = new CommentSaveRequest("contents");

        //when //then
        mockMvc.perform(post("/todos/{todoId}/comments", savedTodo.getId())
                        .header("Authorization", userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentSaveRequest)))
                        .andExpect(status().isOk())
                .andExpect(jsonPath("$.contents").value("contents"));

    }


    // 댓글 저장 실패
    // - 인증실패 authUser = null
    @Test
    void save_comment_failed_by_authUser_is_null() throws Exception{
        //given
        CommentSaveRequest commentSaveRequest = new CommentSaveRequest("contents");

        //when //then
        mockMvc.perform(post("/todos/{todoId}/comments", savedTodo.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentSaveRequest)))
                .andExpect(status().isUnauthorized());
    }

    // - validation 내용 누락
    @Test
    void save_comment_failed_by_validation() throws Exception{
        //given
        CommentSaveRequest commentSaveRequest = new CommentSaveRequest("");

        //when //then
        mockMvc.perform(post("/todos/{todoId}/comments", savedTodo.getId())
                        .header("Authorization", userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentSaveRequest)))
                .andExpect(status().isBadRequest());
    }

    // 댓글 목록 조회 성공
    @Test
    @DisplayName("성공 - 댓글 조회")
    void getComments_succeed() throws Exception{
        //given
        commentRepository.save(new Comment("이건 첫번째 레슨...", user, savedTodo));
        commentRepository.save(new Comment("이건 두번째 레슨...", adminUser, savedTodo));

        //when //then
        mockMvc.perform(get("/todos/{todoId}/comments", savedTodo.getId())
                .header("Authorization", userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].contents").value("이건 첫번째 레슨..."))
                .andExpect(jsonPath("$[1].contents").value("이건 두번째 레슨..."))
                .andExpect(jsonPath("$[0].user.id").value(user.getId()))
                .andExpect(jsonPath("$[1].user.id").value(adminUser.getId()));
    }

    // 댓글 삭제 성공
    @Test
    @DisplayName("성공 - 댓글 삭제")
    void deleteComment_succeed() throws Exception{
        //given
        Comment savedComment = commentRepository.save(new Comment("이건 첫번째 레슨...", user, savedTodo));

        //when //then
        mockMvc.perform(delete("/admin/comments/{commentId}", savedComment.getId())
                .header("Authorization", adminToken))
                .andExpect(status().isOk());
    }

    // 댓글 삭제 권한 없음
    @Test
    @DisplayName("실패 - 권한 없음")
    void deleteComment_failed_by_unauthorized() throws Exception{
        //given
        Comment savedComment = commentRepository.save(new Comment("이건 첫번째 레슨...", user, savedTodo));

        //when //then
        mockMvc.perform(delete("/admin/comments/{commentId}", savedComment.getId())
                        .header("Authorization", userToken))
                .andExpect(status().isBadRequest());
    }



}
