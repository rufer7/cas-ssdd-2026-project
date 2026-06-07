package ch.ssdd.eventhub.adapters.inbound.rest;

import ch.ssdd.eventhub.adapters.inbound.rest.dto.CommentResponseDto;
import ch.ssdd.eventhub.adapters.inbound.rest.dto.CreateCommentRequestDto;
import ch.ssdd.eventhub.domain.Comment;
import ch.ssdd.eventhub.domain.User;
import ch.ssdd.eventhub.ports.inbound.AddCommentUseCase;
import ch.ssdd.eventhub.ports.inbound.LoadCommentsByEventUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommentRestControllerTest {

    @Mock
    AddCommentUseCase addCommentUseCase;

    @Mock
    LoadCommentsByEventUseCase loadCommentsByEventUseCase;

    @InjectMocks
    CommentRestController controller;

    @Test
    void shouldReturnCommentsByEvent() {
        // given
        UUID eventId = UUID.randomUUID();
        Comment comment1 = mock(Comment.class);
        Comment comment2 = mock(Comment.class);

        User dummyUser = mock(User.class);

        when(comment1.createdBy()).thenReturn(dummyUser);
        when(comment2.createdBy()).thenReturn(dummyUser);

        when(loadCommentsByEventUseCase.loadCommentsByEvent(eventId))
                .thenReturn(List.of(comment1, comment2));

        // when
        ResponseEntity<List<CommentResponseDto>> response = controller.getCommentsByEvent(eventId);

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());

        verify(loadCommentsByEventUseCase, times(1)).loadCommentsByEvent(eventId);
    }

    @Test
    void shouldAddComment() {
        // given
        UUID eventId = UUID.randomUUID();
        CreateCommentRequestDto request = new CreateCommentRequestDto("Great event!", "john");
        Comment comment = mock(Comment.class);
        User dummyUser = mock(User.class);

        when(comment.createdBy()).thenReturn(dummyUser);

        when(addCommentUseCase.addComment(eventId, request.content(), request.username()))
                .thenReturn(comment);

        // when
        ResponseEntity<CommentResponseDto> response = controller.addComment(eventId, request);

        // then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());

        verify(addCommentUseCase, times(1)).addComment(eventId, request.content(), request.username());
    }
}
