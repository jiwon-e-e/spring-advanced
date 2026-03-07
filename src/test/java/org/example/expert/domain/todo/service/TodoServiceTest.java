package org.example.expert.domain.todo.service;

import org.example.expert.client.WeatherClient;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.todo.dto.request.TodoSaveRequest;
import org.example.expert.domain.todo.dto.response.TodoResponse;
import org.example.expert.domain.todo.dto.response.TodoSaveResponse;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.repository.TodoRepository;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TodoServiceTest {
    @Mock
    private TodoRepository todoRepository;
    @Mock
    private WeatherClient weatherClient;
    @InjectMocks
    private TodoService todoService;

    @Test
    @DisplayName("성공 - TODO 생성")
    public void saveTodo_succeed(){
        //given
        AuthUser authUser = new AuthUser(1L, "test@email.com", UserRole.USER);
        User user = User.fromAuthUser(authUser);

        String title = "할 일 제목";
        String contents = "할 일 내용";
        TodoSaveRequest request = new TodoSaveRequest(title, contents);

        Todo todo = new Todo(title, contents, "Sunny", user);
        ReflectionTestUtils.setField(todo, "id", 1L);


        when(weatherClient.getTodayWeather()).thenReturn("Sunny");
        when(todoRepository.save(any(Todo.class))).thenReturn(todo);

        //when
        TodoSaveResponse response = todoService.saveTodo(authUser, request);

        //then

        verify(todoRepository).save(any(Todo.class));
        verify(weatherClient).getTodayWeather();
        assertThat(response).isNotNull();
        assertThat(response.getTitle()).isEqualTo(title);
        assertThat(response.getUser().getEmail()).isEqualTo(authUser.getEmail());
    }

    // 실패 케이스... 작성할 게 없음
    // 해봤자 authUser 이 null 인 경우나 getTodayWeather 이 null 인 경우인데
    // 각각 필터랑 해당 클래스 내부에서 체크를 함
    // repository 에 save 하는 부분도 내가 확인할 필요 없고 아닌가...

    @Test
    @DisplayName("성공 - TODO 리스트 조회")
    public void getTodos_succeed(){

        //given
        int page = 1;
        int size = 5;
        Pageable pageable = PageRequest.of(0, 5);
        User user = new User("test@email.com", "1234", UserRole.USER);
        ReflectionTestUtils.setField(user, "id", 1L);

        Todo mockTodo = new Todo("할 일 제목","할 일 내용", "Sunny", user);
        List<Todo> todoList = List.of(mockTodo);
        Page<Todo> mockTodos = new PageImpl<>(todoList, pageable, todoList.size());
        // 실제 데이터 리스트
        // 페이지 정보
        // size
        // page 형태로 mock 만드는거 나중에 정리해두기, empty page 도 같이... -> TIL 에 작성 완료

        when(todoRepository.findAllByOrderByModifiedAtDesc(pageable)).thenReturn(mockTodos);

        //when
        Page<TodoResponse> responses = todoService.getTodos(page, size);

        //then
        verify(todoRepository).findAllByOrderByModifiedAtDesc(pageable);
        assertThat(responses.getContent()).hasSize(1);
        assertThat(responses.getContent().get(0).getTitle()).isEqualTo("할 일 제목");
    }

    @Test
    @DisplayName("성공 - 빈 TODOLIST")
    public void getTodos_succeed_empty(){

        //given
        int page = 1;
        int size = 5;
        Pageable pageable = PageRequest.of(0, 5);
        User user = new User("test@email.com", "1234", UserRole.USER);
        ReflectionTestUtils.setField(user, "id", 1L);

        Page<Todo> emptyPage = new PageImpl<>(List.of(), pageable, 0);

        when(todoRepository.findAllByOrderByModifiedAtDesc(pageable)).thenReturn(emptyPage);

        //when
        Page<TodoResponse> responses = todoService.getTodos(page, size);

        //then
        assertThat(responses.getContent()).isEmpty();
    }

    @Test
    @DisplayName("성공 - 개별 TODO 조회")
    public void getTodo_succeed(){
        //given
        Long todoId = 1L;
        User user = new User("test@email.com", "1234", UserRole.USER);
        Todo todo = new Todo("할 일 제목","할 일 내용", "Sunny", user);
        ReflectionTestUtils.setField(todo, "id", todoId);

        when(todoRepository.findByIdWithUser(todoId)).thenReturn(Optional.of(todo));
        //when
        TodoResponse response = todoService.getTodo(todoId);

        //then
        verify(todoRepository).findByIdWithUser(todoId);
        assertThat(response.getId()).isEqualTo(todoId);
        assertThat(response.getTitle()).isEqualTo("할 일 제목");
        assertThat(response.getUser().getEmail()).isEqualTo("test@email.com");
    }

    @Test
    @DisplayName("실패 - 존재하지 않는 TODO")
    public void getTodo_failed_by_todo_not_found(){
        //given
        Long todoId = 1L;

        when(todoRepository.findByIdWithUser(todoId)).thenReturn(Optional.empty());
        //when
        //then
        assertThatThrownBy(()-> todoService.getTodo(todoId))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessage("Todo not found");
    }

}