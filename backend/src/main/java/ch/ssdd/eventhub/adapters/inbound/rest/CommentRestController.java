package ch.ssdd.eventhub.adapters.inbound.rest;

import ch.ssdd.eventhub.adapters.inbound.rest.dto.CommentResponseDto;
import ch.ssdd.eventhub.adapters.inbound.rest.dto.CreateCommentRequestDto;
import ch.ssdd.eventhub.ports.inbound.AddCommentUseCase;
import ch.ssdd.eventhub.ports.inbound.LoadCommentsByEventUseCase;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/events/{eventId}/comments")
public class CommentRestController {

    private final AddCommentUseCase addCommentUseCase;
    private final LoadCommentsByEventUseCase loadCommentsByEventUseCase;

    public CommentRestController(AddCommentUseCase addCommentUseCase,
                                 LoadCommentsByEventUseCase loadCommentsByEventUseCase) {
        this.addCommentUseCase = addCommentUseCase;
        this.loadCommentsByEventUseCase = loadCommentsByEventUseCase;
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('Admin', 'User')")
    public ResponseEntity<List<CommentResponseDto>> getCommentsByEvent(@PathVariable UUID eventId) {
        var commentDtos = loadCommentsByEventUseCase.loadCommentsByEvent(eventId)
                .stream()
                .map(CommentResponseDto::of)
                .toList();
        return ResponseEntity.ok(commentDtos);
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('Admin', 'User')")
    public ResponseEntity<CommentResponseDto> addComment(
            Authentication authentication,
            @PathVariable UUID eventId,
            @RequestBody CreateCommentRequestDto request) {
        // The author is the authenticated principal — never trust a client-supplied identity.
        var comment = addCommentUseCase.addComment(
                eventId,
                request.content(),
                authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(CommentResponseDto.of(comment));
    }
}
