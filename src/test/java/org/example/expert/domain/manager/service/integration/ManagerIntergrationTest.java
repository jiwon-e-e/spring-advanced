package org.example.expert.domain.manager.service.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.expert.config.GlobalExceptionHandler;
import org.example.expert.config.JwtFilter;
import org.example.expert.config.JwtUtil;
import org.example.expert.domain.comment.dto.request.CommentSaveRequest;
import org.example.expert.domain.comment.entity.Comment;
import org.example.expert.domain.comment.repository.CommentRepository;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.common.exception.ServerException;
import org.example.expert.domain.manager.controller.ManagerController;
import org.example.expert.domain.manager.dto.request.ManagerSaveRequest;
import org.example.expert.domain.manager.entity.Manager;
import org.example.expert.domain.manager.repository.ManagerRepository;
import org.example.expert.domain.manager.service.ManagerService;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.repository.TodoRepository;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
public class ManagerIntergrationTest {


    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private TodoRepository todoRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtUtil jwtUtil;

    private String userToken;

    private User user;
    private User user2;
    private Todo savedTodo;
    @Autowired
    private ManagerRepository managerRepository;

    @BeforeEach
    void setup(){
        user = userRepository.save(new User("testUser@email.com", "1234", UserRole.USER));
        user2 = userRepository.save(new User("newUser@email.com", "1234", UserRole.USER));

        savedTodo = todoRepository.save(new Todo("title", "contents", "SUNNY", user));

        userToken = jwtUtil.createToken(user.getId(), user.getEmail(), user.getUserRole());
    }


    //manager 저장

    @Test
    void save_manager_succeed() throws Exception{
        ManagerSaveRequest managerSaveRequest = new ManagerSaveRequest(user2.getId());

        mockMvc.perform(post("/todos/{todoId}/managers", savedTodo.getId())
                        .header("Authorization", userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(managerSaveRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.email").value("newUser@email.com"));
    }

    // 저장 실패 - 인증 실패

    @Test
    void save_manager_failed_by_user_is_null() throws Exception{
        ManagerSaveRequest managerSaveRequest = new ManagerSaveRequest(user2.getId());

        mockMvc.perform(post("/todos/{todoId}/managers", savedTodo.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(managerSaveRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("성공 - 관리자 조회")
    void getManagers_succeed() throws Exception{
        // todo 왜이러는건지확인하기....
        managerRepository.save(new Manager(user2, savedTodo));

        mockMvc.perform(get("/todos/{todoId}/managers", savedTodo.getId())
                        .header("Authorization", userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].user.id").value(user.getId()))
                .andExpect(jsonPath("$[1].user.id").value(user2.getId()));
    }

    @Test
    @DisplayName("성공 - 관리자 삭제")
    void deleteManager_succeed() throws Exception{
        Manager manager = managerRepository.save(new Manager(user2, savedTodo));

        mockMvc.perform(delete("/todos/{todoId}/managers/{managerId}", savedTodo.getId(), manager.getId())
                        .header("Authorization", userToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("실패 - 인증 실패")
    void deleteManager_failed_by_unauthorized() throws Exception{
        Manager manager = managerRepository.save(new Manager(user2, savedTodo));

        mockMvc.perform(delete("/todos/{todoId}/managers/{managerId}", savedTodo.getId(),manager.getId()))
                .andExpect(status().isUnauthorized());
    }

//    @Test
//    @DisplayName("실패 - substring 오류")
//    void deleteManager_failed_by_substring_error() throws Exception {
//        Manager manager = managerRepository.save(new Manager(user2, savedTodo));
//
//        String invalidToken = "bearer token";
//
//        // when & then
//        mockMvc.perform(delete("/todos/{todoId}/managers/{managerId}", savedTodo.getId(), manager.getId())
//                        .header("Authorization", invalidToken))
//                .andExpect(status().isInternalServerError());
//    }

}
