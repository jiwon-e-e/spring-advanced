package org.example.expert.domain.todo.integration;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.expert.client.WeatherClient;
import org.example.expert.config.JwtUtil;
import org.example.expert.domain.todo.dto.request.TodoSaveRequest;
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
import org.springframework.web.accept.ContentNegotiationManager;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
public class TodoIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private TodoRepository todoRepository;
    @Autowired
    private WeatherClient weatherClient;
    @Autowired
    private UserRepository userRepository;
    //어차피 user 를 저장해줘야함

    private String userToken;
    private User user;
    @Autowired
    private ContentNegotiationManager mvcContentNegotiationManager;

    @BeforeEach
    void setup(){
        user = userRepository.save(new User("testUser@email.com", "1234", UserRole.USER));
        userToken = jwtUtil.createToken(user.getId(), user.getEmail(), user.getUserRole());
    }



    @Test
    @DisplayName("성공 - 할 일 저장")
    void saveTodo_succeed() throws Exception{
        //given
        TodoSaveRequest request = new TodoSaveRequest("title", "contents");
        String weather = weatherClient.getTodayWeather();
        todoRepository.save(new Todo(request.getTitle(), request.getContents(), weather, user));

        //when //then
        mockMvc.perform(post("/todos")
                .header("Authorization", userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("title"))
                .andExpect(jsonPath("$.weather").value(weatherClient.getTodayWeather()))
                .andExpect(jsonPath("$.user.email").value("testUser@email.com"));
    }

    @Test
    @DisplayName("실패 - 로그인 안 함")
    void saveTodo_failed_by_unAuthorized() throws Exception{
        //given
        TodoSaveRequest request = new TodoSaveRequest("title", "contents");
        String weather = weatherClient.getTodayWeather();
        todoRepository.save(new Todo(request.getTitle(), request.getContents(), weather, user));

        //when //then
        mockMvc.perform(post("/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("실패 - 필수값 누락")
    void saveTodo_failed_by_validation() throws Exception{
        //given
        TodoSaveRequest request = new TodoSaveRequest("", "contents");
        String weather = weatherClient.getTodayWeather();
        todoRepository.save(new Todo(request.getTitle(), request.getContents(), weather, user));

        //when //then
        mockMvc.perform(post("/todos")
                        .header("Authorization", userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }


    @Test
    @DisplayName("성공 - 다건조회 성공")
    void getTodos_succeed() throws Exception{
        //given
        String weather = weatherClient.getTodayWeather();
        todoRepository.save(new Todo("집가고싶은사람 손?", "는 나였고", weather, user));
        todoRepository.save(new Todo("아니", "님 집이잖아요", weather, user));

        //when //then
        mockMvc.perform(get("/todos")
                .header("Authorization", userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].weather").value(weather))
                .andExpect(jsonPath("$.content[0].user.email").value("testUser@email.com"))
                .andExpect(jsonPath("$.content[0].title").value("아니"))
                .andExpect(jsonPath("$.content[0].contents").value("님 집이잖아요"))
                .andExpect(jsonPath("$.content[1].title").value("집가고싶은사람 손?"))
                .andExpect(jsonPath("$.content[1].contents").value("는 나였고"))
                ;

        //지금 paging 이 정렬순서에 맞게 되고 있어서 그런듯함
        //테스트 기대값을 순서에 맞춰서 바꿔주기
    }

    @Test
    @DisplayName("성공 - 단건조회 성공")
    void getTodo_succeed() throws Exception{
        // given
        String weather = weatherClient.getTodayWeather();
        Todo todo = todoRepository.save(new Todo("title", "contents", weather, user));

        //when //then
        mockMvc.perform(get("/todos/{todoId}", todo.getId())
                .header("Authorization", userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("title"))
                .andExpect(jsonPath("$.contents").value("contents"))
                .andExpect(jsonPath("$.user.email").value("testUser@email.com"));
    }

}
