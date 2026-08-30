package com.example.eventhub.event;

import com.example.eventhub.auth.details.UserPrincipal;
import com.example.eventhub.event.dto.EventCreateRequest;
import com.example.eventhub.event.dto.EventResponse;
import com.example.eventhub.event.dto.EventUpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @PreAuthorize("hasAnyRole('ORGANIZER','ADMIN')")
    @PostMapping
    public ResponseEntity<EventResponse> create(@AuthenticationPrincipal UserPrincipal principal,@Valid@RequestBody EventCreateRequest request){
        EventResponse created=eventService.create(request,principal.getUser());
        return ResponseEntity.created(URI.create("/api/v1/events/"+created.id())).body(created);
    }

    @GetMapping
        public ResponseEntity<List<EventResponse>> getAll(@RequestParam(required = false) String category,@ParameterObject Pageable pageable){
        return ResponseEntity.ok(eventService.getAll(category, pageable));
    }

    @GetMapping("/{id}")
        public ResponseEntity<EventResponse> getOne(@AuthenticationPrincipal(expression = "username != null ? this : null") UserPrincipal principal,@PathVariable Long id){
        return ResponseEntity.ok(eventService.getById(principal,id));
    }

    @PreAuthorize("@helpForService.isEventOwner(#id,authentication.principal.id) OR hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<EventResponse> update(@Valid@RequestBody EventUpdateRequest request, @PathVariable Long id){
        return ResponseEntity.ok(eventService.update(id,request));
    }

    @PreAuthorize("@helpForService.isEventOwner(#id,authentication.principal.id) OR hasRole('ADMIN')")
    @PatchMapping("{id}/publish")
    public ResponseEntity<EventResponse> publish(@PathVariable Long id){
        return ResponseEntity.ok(eventService.publish(id));
    }

    @PreAuthorize("@helpForService.isEventOwner(#id,authentication.principal.id) OR hasRole('ADMIN')")
    @PatchMapping("{id}/cancel")
    public ResponseEntity<EventResponse> cancel(@PathVariable Long id){
        return ResponseEntity.ok(eventService.cancel(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("{id}/complite")
    public ResponseEntity<EventResponse> complete(@PathVariable Long id){
        return ResponseEntity.ok(eventService.complete(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id){
        eventService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
