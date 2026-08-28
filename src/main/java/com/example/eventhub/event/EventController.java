package com.example.eventhub.event;

import com.example.eventhub.auth.details.UserPrincipal;
import com.example.eventhub.event.dto.EventCreateRequest;
import com.example.eventhub.event.dto.EventResponse;
import com.example.eventhub.event.dto.EventUpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @GetMapping
    public ResponseEntity<List<EventResponse>> getAll(){

    }

    @GetMapping("/{id}")
    public ResponseEntity<EventResponse> getOne(@PathVariable Long id){

    }

    @PostMapping
    public ResponseEntity<EventResponse> create(@AuthenticationPrincipal UserPrincipal principal,@Valid@RequestBody EventCreateRequest request){

    }

    @PutMapping("/{id}")
    public ResponseEntity<EventResponse> update(@AuthenticationPrincipal UserPrincipal principal, @Valid@RequestBody EventUpdateRequest request, @PathVariable Long id){

    }

    @PatchMapping("{id}/publish")
    public ResponseEntity<EventResponse> publish(@PathVariable Long id){

    }

    @PatchMapping("{id}/cancel")
    public ResponseEntity<EventResponse> cancel(@PathVariable Long id){

    }

    @PatchMapping("{id}/complite")
    public ResponseEntity<EventResponse> complite(@PathVariable Long id){

    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id){

    }
}
