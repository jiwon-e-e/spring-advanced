package org.example.expert.domain.comment.service;

import org.example.expert.domain.comment.entity.Comment;
import org.example.expert.domain.comment.repository.CommentRepository;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class CommentAdminServiceTest {
    @Mock
    private CommentRepository commentRepository;
    @InjectMocks
    private CommentAdminService commentAdminService;

    @Test
    @DisplayName("성공 - 댓글 삭제")
    public void deleteComment_succeed(){
        //given
        long commentId = 1L;
        Comment mockComment = getComment();

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(mockComment));
        //when
        commentAdminService.deleteComment(commentId);

        //then
        verify(commentRepository).delete(any(Comment.class));
    }

    @Test
    @DisplayName("실패 - 존재하지 않는 댓글")
    public void deleteComment_failed_by_comment_not_found(){
        //given
        long commentId = 1L;
        when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

        //when
        //then
        assertThatThrownBy(()-> commentAdminService.deleteComment(commentId))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessage("Comment not found");

    }

    private Comment getComment(){
        User user = new User("user1@example.com", "password", UserRole.USER);
        Todo todo = new Todo("Title", "Contents", "Sunny", user);

        return new Comment("테스트 내용", user, todo);
    }





}