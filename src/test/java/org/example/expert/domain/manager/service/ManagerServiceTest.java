package org.example.expert.domain.manager.service;

import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.manager.dto.request.ManagerSaveRequest;
import org.example.expert.domain.manager.dto.response.ManagerResponse;
import org.example.expert.domain.manager.dto.response.ManagerSaveResponse;
import org.example.expert.domain.manager.entity.Manager;
import org.example.expert.domain.manager.repository.ManagerRepository;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.repository.TodoRepository;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ManagerServiceTest {

    @Mock
    private ManagerRepository managerRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TodoRepository todoRepository;
    @InjectMocks
    private ManagerService managerService;

    @Test // 테스트코드 샘플
    @DisplayName("성공 - 관리자 등록")
    void saveManager_succeed() {
        // given
        AuthUser authUser = new AuthUser(1L, "testUser@email.com", UserRole.USER);
        User user = User.fromAuthUser(authUser);  // 일정을 만든 유저

        long todoId = 1L;
        Todo todo = new Todo("Test Title", "Test Contents", "Sunny", user);

        long managerUserId = 2L;
        User managerUser = new User("testAdmin@email.com", "1234", UserRole.USER);  // 매니저로 등록할 유저
        ReflectionTestUtils.setField(managerUser, "id", managerUserId);

        long managerId = 3L;
        Manager manager = new Manager(managerUser, todo);
        ReflectionTestUtils.setField(manager, "id", managerId);

        ManagerSaveRequest managerSaveRequest = new ManagerSaveRequest(managerUserId); // request dto 생성

        when(todoRepository.findById(todoId)).thenReturn(Optional.of(todo));
        when(userRepository.findById(managerUserId)).thenReturn(Optional.of(managerUser));
        when(managerRepository.save(any(Manager.class))).thenReturn(manager);

        // when
        ManagerSaveResponse response = managerService.saveManager(authUser, todoId, managerSaveRequest);

        // then
        assertNotNull(response);
        assertEquals(managerUser.getId(), response.getUser().getId());
        assertEquals(managerUser.getEmail(), response.getUser().getEmail());
    }

    @Test // 테스트코드 샘플
    @DisplayName("실패 - 존재하지 않는 TODO")
    void saveManager_failed_by_todo_not_found() {
        // given
        AuthUser authUser = new AuthUser(1L, "testUser@email.com", UserRole.USER);
        //User user = User.fromAuthUser(authUser);  // 일정을 만든 유저

        long todoId = 1L;
        long managerUserId = 2L;
        ManagerSaveRequest managerSaveRequest = new ManagerSaveRequest(managerUserId); // request dto 생성

        when(todoRepository.findById(todoId)).thenReturn(Optional.empty());

        // when
        // then

        assertThatThrownBy(()-> managerService.saveManager(authUser, todoId, managerSaveRequest))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessage("Todo not found");

    }

    @Test // 테스트코드 샘플
    @DisplayName("실패 - 존재하지 않는 todo 작성자")
    void saveManager_failed_by_todo_user_is_null() {
        // given
        AuthUser authUser = new AuthUser(1L, "testUser@email.com", UserRole.USER);
        //User user = User.fromAuthUser(authUser);  // 일정을 만든 유저

        long todoId = 1L;
        Todo todo = new Todo("Test Title", "Test Contents", "Sunny", null);

        long managerUserId = 2L;

        ManagerSaveRequest managerSaveRequest = new ManagerSaveRequest(managerUserId); // request dto 생성

        when(todoRepository.findById(todoId)).thenReturn(Optional.of(todo));

        // when
        // then
        assertThatThrownBy(()-> managerService.saveManager(authUser, todoId, managerSaveRequest))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessage("일정을 생성한 유저만 담당자를 지정할 수 있습니다.");
    }

    @Test // 테스트코드 샘플
    @DisplayName("실패 - 작성자 불일치")
    void saveManager_failed_by_id_not_equal() {
        // given
        AuthUser authUser = new AuthUser(1L, "testUser@email.com", UserRole.USER);
        User user = User.fromAuthUser(authUser);  // 로그인 한 사람

        long newUserId = 99L;
        User newUser = new User("newUser@email.com", "1234", UserRole.USER);
        ReflectionTestUtils.setField(newUser, "id", newUserId); // 일정 만든 사람

        long todoId = 1L;
        Todo todo = new Todo("Test Title", "Test Contents", "Sunny", newUser);

        long managerUserId = 2L;
        User managerUser = new User("testAdmin@email.com", "1234", UserRole.USER);  // 매니저로 등록할 유저
        ReflectionTestUtils.setField(managerUser, "id", managerUserId);

        long managerId = 3L;
        Manager manager = new Manager(managerUser, todo);
        ReflectionTestUtils.setField(manager, "id", managerId);

        ManagerSaveRequest managerSaveRequest = new ManagerSaveRequest(managerUserId); // request dto 생성

        when(todoRepository.findById(todoId)).thenReturn(Optional.of(todo));
        //when(userRepository.findById(managerUserId)).thenReturn(Optional.of(managerUser));
        // when
        // then

        assertThatThrownBy(()-> managerService.saveManager(authUser, todoId, managerSaveRequest))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessage("일정을 생성한 유저만 담당자를 지정할 수 있습니다.");
    }

    @Test // 테스트코드 샘플
    @DisplayName("실패 - 존재하지 않는 담당자 ")
    void saveManager_failed_by_manager_not_found() {
        // given
        AuthUser authUser = new AuthUser(1L, "testUser@email.com", UserRole.USER);
        User user = User.fromAuthUser(authUser);  // 로그인 한 사람

        long todoId = 1L;
        Todo todo = new Todo("Test Title", "Test Contents", "Sunny", user);

        long managerUserId = 2L;
        User managerUser = new User("testAdmin@email.com", "1234", UserRole.USER);  // 매니저로 등록할 유저
        ReflectionTestUtils.setField(managerUser, "id", managerUserId);

        long managerId = 3L;
        Manager manager = new Manager(managerUser, todo);
        ReflectionTestUtils.setField(manager, "id", managerId);

        ManagerSaveRequest managerSaveRequest = new ManagerSaveRequest(managerUserId); // request dto 생성

        when(todoRepository.findById(todoId)).thenReturn(Optional.of(todo));
        when(userRepository.findById(managerUserId)).thenReturn(Optional.empty());
        // when
        // then

        assertThatThrownBy(()-> managerService.saveManager(authUser, todoId, managerSaveRequest))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessage("등록하려고 하는 담당자 유저가 존재하지 않습니다.");
    }

    @Test // 테스트코드 샘플
    @DisplayName("실패 - 일정 작성자와 담당자가 동일 ")
    void saveManager_failed_by_manager_is_equal_to_user() {
        // given
        AuthUser authUser = new AuthUser(1L, "testUser@email.com", UserRole.USER);
        User user = User.fromAuthUser(authUser);  // 로그인 한 사람

        long todoId = 1L;
        Todo todo = new Todo("Test Title", "Test Contents", "Sunny", user);

        long managerUserId = 1L;
        User managerUser = new User("testAdmin@email.com", "1234", UserRole.USER);  // 매니저로 등록할 유저
        ReflectionTestUtils.setField(managerUser, "id", managerUserId);

        long managerId = 3L;
        Manager manager = new Manager(managerUser, todo);
        ReflectionTestUtils.setField(manager, "id", managerId);

        ManagerSaveRequest managerSaveRequest = new ManagerSaveRequest(managerUserId); // request dto 생성

        when(todoRepository.findById(todoId)).thenReturn(Optional.of(todo));
        when(userRepository.findById(managerUserId)).thenReturn(Optional.of(managerUser));
        // when
        // then

        assertThatThrownBy(()-> managerService.saveManager(authUser, todoId, managerSaveRequest))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessage("일정 작성자는 본인을 담당자로 등록할 수 없습니다.");
    }


    // -----------------------------------------------------------------------------

    @Test // 테스트코드 샘플
    @DisplayName("성공 - 관리자 조회")
    public void getManager_succeed() {
        // given
        long todoId = 1L;
        User user = new User("user1@example.com", "password", UserRole.USER);
        Todo todo = new Todo("Title", "Contents", "Sunny", user);
        ReflectionTestUtils.setField(todo, "id", todoId);

        Manager mockManager = new Manager(todo.getUser(), todo);
        List<Manager> managerList = List.of(mockManager);

        when(todoRepository.findById(todoId)).thenReturn(Optional.of(todo));
        when(managerRepository.findByTodoIdWithUser(todoId)).thenReturn(managerList);

        // when
        List<ManagerResponse> managerResponses = managerService.getManagers(todoId);

        // then
        assertEquals(1, managerResponses.size());
        assertEquals(mockManager.getId(), managerResponses.get(0).getId());
        assertEquals(mockManager.getUser().getEmail(), managerResponses.get(0).getUser().getEmail());
    }

    @Test
    @DisplayName("실패 - 존재하지 않는 TODO")
    public void getManager_failed_by_todo_not_found() {
        // given
        long todoId = 1L;
        when(todoRepository.findById(todoId)).thenReturn(Optional.empty());

        // when
        // then
        assertThatThrownBy(()-> managerService.getManagers(todoId))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessage("Todo not found");
    }

    @Test
    @DisplayName("성공 - 빈 managerList")// 테스트코드 샘플
    public void getManager_succeed_empty() {
        // given
        long todoId = 1L;
        User user = new User("user1@example.com", "password", UserRole.USER);
        Todo todo = new Todo("Title", "Contents", "Sunny", user);
        ReflectionTestUtils.setField(todo, "id", todoId);

        when(todoRepository.findById(todoId)).thenReturn(Optional.of(todo));
        when(managerRepository.findByTodoIdWithUser(todoId)).thenReturn(Collections.emptyList());

        // when
        List<ManagerResponse> managerResponses = managerService.getManagers(todoId);

        // then
        assertThat(managerResponses).isEmpty();
    }

    // --------------------------------------------------------------------------------

    @Test
    @DisplayName("성공 - 담당자 삭제")
    public void deleteManager_succeed(){

        //given

        long userId = 1L;
        User user = new User("user1@example.com", "password", UserRole.USER);
        ReflectionTestUtils.setField(user, "id", userId);

        long todoId = 1L;
        Todo todo = new Todo("Title", "Contents", "Sunny", user);
        ReflectionTestUtils.setField(todo, "id", todoId);

        long managerUserId = 2L;
        User managerUser = new User("testAdmin@email.com", "1234", UserRole.USER);  // 매니저로 등록할 유저
        ReflectionTestUtils.setField(managerUser, "id", managerUserId);

        long managerId = 3L;
        Manager manager = new Manager(managerUser, todo);
        ReflectionTestUtils.setField(manager, "id", managerId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(todoRepository.findById(todoId)).thenReturn(Optional.of(todo));
        when(managerRepository.findById(managerId)).thenReturn(Optional.of(manager));

        //when
        managerService.deleteManager(userId, todoId, managerId);


        //then
        verify(managerRepository).delete(manager);
    }

    @Test
    @DisplayName("실패 - 존재하지 않는 사용자")
    public void deleteManager_failed_by_user_not_found(){

        //given
        long userId = 1L;
        long todoId = 2L;
        long managerId = 3L;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        //when
        //then
        assertThatThrownBy(()-> managerService.deleteManager(userId, todoId, managerId))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessage("User not found");
    }

    @Test
    @DisplayName("실패 - 존재하지 않는 todo")
    public void deleteManager_failed_by_todo_not_found(){

        //given
        long userId = 1L;
        User user = new User("user1@example.com", "password", UserRole.USER);
        ReflectionTestUtils.setField(user, "id", userId);

        long todoId = 2L;

        long managerId = 3L;

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(todoRepository.findById(todoId)).thenReturn(Optional.empty());

        //when
        //then
        assertThatThrownBy(()-> managerService.deleteManager(userId, todoId, managerId))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessage("Todo not found");
    }

    @Test
    @DisplayName("실패 - todo 의 user가 null")
    public void deleteManager_failed_by_todo_user_is_null(){
        //given
        long userId = 1L;
        User user = new User("user1@example.com", "password", UserRole.USER);
        ReflectionTestUtils.setField(user, "id", userId);

        long todoId = 1L;
        Todo todo = new Todo("Title", "Contents", "Sunny", null);
        ReflectionTestUtils.setField(todo, "id", todoId);

        long managerId = 3L;

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(todoRepository.findById(todoId)).thenReturn(Optional.of(todo));

        //when
        //then
        assertThatThrownBy(()-> managerService.deleteManager(userId, todoId, managerId))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessage("해당 일정을 만든 유저가 유효하지 않습니다.");
    }

    @Test
    @DisplayName("실패 - 작성자와 로그인유저 불일치")
    public void deleteManager_failed_by_user_not_equals(){
        //given
        long userId = 1L; // 얘가 로그인
        User user = new User("user1@example.com", "password", UserRole.USER);
        ReflectionTestUtils.setField(user, "id", userId);

        long newUserId = 99L; // 얘가 투두 작성
        User newUser = new User("newUser@email.com", "1234", UserRole.USER);
        ReflectionTestUtils.setField(newUser, "id", newUserId);

        long todoId = 2L;
        Todo todo = new Todo("Title", "Contents", "Sunny", newUser);
        ReflectionTestUtils.setField(todo, "id", newUserId);

        long managerId = 3L;

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(todoRepository.findById(todoId)).thenReturn(Optional.of(todo));

        //when
        //then
        assertThat(todo.getUser()).isNotNull();
        assertThatThrownBy(()-> managerService.deleteManager(userId, todoId, managerId))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessage("해당 일정을 만든 유저가 유효하지 않습니다.");
    }

    @Test
    @DisplayName("실패 - 존재하지 않는 manager")
    public void deleteManager_failed_by_manager_not_found(){

        //given
        long userId = 1L;
        User user = new User("user1@example.com", "password", UserRole.USER);
        ReflectionTestUtils.setField(user, "id", userId);

        long todoId = 1L;
        Todo todo = new Todo("Title", "Contents", "Sunny", user);
        ReflectionTestUtils.setField(todo, "id", todoId);

        long managerUserId = 2L;
        User managerUser = new User("testAdmin@email.com", "1234", UserRole.USER);  // 매니저로 등록할 유저
        ReflectionTestUtils.setField(managerUser, "id", managerUserId);

        long managerId = 3L;
        Manager manager = new Manager(managerUser, todo);
        ReflectionTestUtils.setField(manager, "id", managerId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(todoRepository.findById(todoId)).thenReturn(Optional.of(todo));

        //when
        //then
        assertThatThrownBy(()-> managerService.deleteManager(userId, todoId, managerId))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessage("Manager not found");
    }


    @Test
    @DisplayName("실패 - 담당자 불일치")
    public void deleteManager_failed_by_manager_not_equals(){
        //given
        long userId = 1L;
        User user = new User("user1@example.com", "password", UserRole.USER);
        ReflectionTestUtils.setField(user, "id", userId);

        long otherTodoId = 99L; // 매니저가 관리하고 있는 투두
        Todo otherTodo = new Todo("Title", "Contents", "Sunny", user);
        ReflectionTestUtils.setField(otherTodo, "id", otherTodoId);

        long todoId = 2L;
        Todo todo = new Todo("Title", "Contents", "Sunny", user);
        ReflectionTestUtils.setField(todo, "id", todoId);

        long managerUserId = 1L;
        User managerUser = new User("testAdmin@email.com", "1234", UserRole.USER);
        ReflectionTestUtils.setField(managerUser, "id", managerUserId);

        long managerId = 1L;
        Manager manager = new Manager(managerUser, otherTodo);
        ReflectionTestUtils.setField(manager, "id", managerId);


        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(todoRepository.findById(todoId)).thenReturn(Optional.of(todo));
        when(managerRepository.findById(managerId)).thenReturn(Optional.of(manager));

        //when
        //then
        assertThatThrownBy(()-> managerService.deleteManager(userId, todoId, managerId))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessage("해당 일정에 등록된 담당자가 아닙니다.");
    }


}
