package org.example.expert.domain.user.intergration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.expert.config.JwtUtil;
import org.example.expert.config.PasswordEncoder;
import org.example.expert.domain.comment.repository.CommentRepository;
import org.example.expert.domain.todo.repository.TodoRepository;
import org.example.expert.domain.user.dto.request.UserChangePasswordRequest;
import org.example.expert.domain.user.dto.request.UserRoleChangeRequest;
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
public class UserIntergrationTest {
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
    @Autowired
    private PasswordEncoder passwordEncoder;

    private String userToken;
    private String adminToken;

    private User user;
    private User adminUser;

    @BeforeEach
    void setup(){
        user = userRepository.save(new User("testUser@email.com", passwordEncoder.encode("1234") , UserRole.USER));
        adminUser = userRepository.save(new User("testAdmin@email.com", "1234", UserRole.ADMIN));

        userToken = jwtUtil.createToken(user.getId(), user.getEmail(), user.getUserRole());
        adminToken = jwtUtil.createToken(adminUser.getId(), adminUser.getEmail(), adminUser.getUserRole());
    }

    @Test
    @DisplayName("성공 - 유저 조회")
    void getUser_succeed() throws Exception{
        //when, then
        mockMvc.perform(get("/users/{userId}", adminUser.getId())
                .header("Authorization", userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("testAdmin@email.com"));
    }

    //성공 - password 수정 완료
    @Test
    @DisplayName("성공 - 비밀번호 수정 완료")
    void changePassword_succeed() throws Exception{
        //given
        String oldPassword = "1234";
        String newPassword = "5678ABCDE";

        UserChangePasswordRequest request = new UserChangePasswordRequest(oldPassword, newPassword);

        //when, then
        mockMvc.perform(put("/users")
                        .header("Authorization", userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

    }


    //실패 - validation

    @Test
    @DisplayName("실패 - 필수값 누락")
    void changePassword_failed_by_validation() throws Exception{
        //given
        String oldPassword = "1234";
        String newPassword = "";

        UserChangePasswordRequest request = new UserChangePasswordRequest(oldPassword, newPassword);

        //when, then
        mockMvc.perform(put("/users")
                        .header("Authorization", userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

    }

    // 성공 - userRole 수정 완료

    @Test
    @DisplayName("성공 - 역할 수정 완료")
    void changeUserRole_succeed() throws Exception{

        //given
        UserRoleChangeRequest request = new UserRoleChangeRequest("ADMIN");

        //when, then
        mockMvc.perform(patch("/admin/users/{userId}", user.getId())
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }


    //실패 - validation

    @Test
    @DisplayName("실패 - 필수값 누락")
    void changeUserRole_failed_by_validation() throws Exception{
        // given
        UserRoleChangeRequest request = new UserRoleChangeRequest("");

        //when, then
        mockMvc.perform(patch("/admin/users/{userId}", user.getId())
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    //실패 - 권한없음

    @Test
    @DisplayName("실패 - 권한 없음")
    void changeUserRole_failed_by_unauthorized() throws Exception{

        //given
        UserRoleChangeRequest request = new UserRoleChangeRequest("ADMIN");

        //when, then
        mockMvc.perform(patch("/admin/users/{userId}", user.getId())
                        .header("Authorization", userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }


}
