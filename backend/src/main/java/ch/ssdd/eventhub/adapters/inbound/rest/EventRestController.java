package ch.ssdd.eventhub.adapters.inbound.rest;

import ch.ssdd.eventhub.adapters.inbound.rest.config.SanitizedString;
import ch.ssdd.eventhub.adapters.inbound.rest.dto.CreateEventRequestDto;
import ch.ssdd.eventhub.adapters.inbound.rest.dto.EventResponseDto;
import ch.ssdd.eventhub.adapters.inbound.rest.dto.UpdateEventRequestDto;
import ch.ssdd.eventhub.adapters.inbound.rest.security.FileChecker;
import ch.ssdd.eventhub.ports.inbound.CreateEventUseCase;
import ch.ssdd.eventhub.ports.inbound.DeleteEventUseCase;
import ch.ssdd.eventhub.ports.inbound.LoadAllEventsUseCase;
import ch.ssdd.eventhub.ports.inbound.SearchEventsUseCase;
import ch.ssdd.eventhub.ports.inbound.UpdateEventUseCase;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/events")
public class EventRestController {

    private static final Logger logger = LoggerFactory.getLogger(EventRestController.class);

    private final LoadAllEventsUseCase loadAllEventsUseCase;
    private final CreateEventUseCase createEventUseCase;
    private final UpdateEventUseCase updateEventUseCase;
    private final DeleteEventUseCase deleteEventUseCase;
    private final SearchEventsUseCase searchEventsUseCase;

    public EventRestController(LoadAllEventsUseCase loadAllEventsUseCase, CreateEventUseCase createEventUseCase, UpdateEventUseCase updateEventUseCase, DeleteEventUseCase deleteEventUseCase, SearchEventsUseCase searchEventsUseCase) {
        this.loadAllEventsUseCase = loadAllEventsUseCase;
        this.createEventUseCase = createEventUseCase;
        this.updateEventUseCase = updateEventUseCase;
        this.deleteEventUseCase = deleteEventUseCase;
        this.searchEventsUseCase = searchEventsUseCase;
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('Admin', 'User')")
    public ResponseEntity<List<EventResponseDto>> getAllEvents(Authentication authentication) {
        var eventDtos = loadAllEventsUseCase.loadAllEvents()
                .stream()
                .map(EventResponseDto::of)
                .toList();
        return ResponseEntity.ok(eventDtos);
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyAuthority('Admin', 'User')")
    public ResponseEntity<List<EventResponseDto>> searchEvents(@RequestParam(name = "query") SanitizedString searchString) {
        var searchEvents = searchEventsUseCase.searchEvents(searchString.value())
                .stream()
                .map(EventResponseDto::of)
                .toList();
        return ResponseEntity.ok(searchEvents);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('Admin')")
    public ResponseEntity<EventResponseDto> createEvent(Authentication authentication,
                                                        @RequestBody CreateEventRequestDto request) {

        var event = createEventUseCase.create(request.toCommand(authentication.getName()));

        return ResponseEntity.status(HttpStatus.CREATED).body(EventResponseDto.of(event));
    }

    /**
     * Upload featured image to an event.
     * <p>
     * Max file size: see spring.servlet.multipart.max-file-size & maxAllowedFileSize in FileChecker
     * Allowed extensions: .jpg, .jpeg, .png
     * </p>
     *
     * @param principal authenticated user
     * @param file multipart/form-data encoded file
     * @return 200 OK, if upload succeeded
     */
    @PostMapping("/{id}/uploadFeaturedImage")
    @PreAuthorize("hasAuthority('Admin')")
    public ResponseEntity<String> uploadFeaturedImage(Authentication authentication,
                                                      @PathVariable UUID id,
                                                      @RequestParam("file") MultipartFile file) {

        // NOTE: Ideally an anti malware scan is executed against the file before processing and storing it

        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            logger.error("Error occurred during file processing", e);
            return ResponseEntity.internalServerError().body("Error occurred during file processing");
        }

        var isValid = FileChecker.isValid(file.getOriginalFilename(), bytes);
        if (!isValid) {
            return ResponseEntity.badRequest().body("Invalid file");
        }

        updateEventUseCase.updateFeaturedImage(id, bytes);

        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('Admin')")
    public ResponseEntity<EventResponseDto> updateEvent(Authentication authentication,
                                                        @PathVariable UUID id,
                                                        @RequestBody UpdateEventRequestDto request) {

        var updatedEvent = updateEventUseCase.update(id, request.title(), request.description(), request.from(), request.to(), request.location());

        return ResponseEntity.ok(EventResponseDto.of(updatedEvent));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('Admin')")
    public ResponseEntity<EventResponseDto> deleteEvent(Authentication authentication,
                                                        @PathVariable UUID id) {
        deleteEventUseCase.deleteEvent(id);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
