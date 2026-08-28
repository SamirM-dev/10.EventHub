package com.example.eventhub.event;

import com.example.eventhub.auth.details.UserPrincipal;
import com.example.eventhub.event.dto.EventCreateRequest;
import com.example.eventhub.event.dto.EventResponse;
import com.example.eventhub.event.dto.EventUpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
    //    @GetMapping
    //    public ResponseEntity<List<EventResponse>> getAll(){
    //
    //    }
    //
    //    @GetMapping("/{id}")
    //    public ResponseEntity<EventResponse> getOne(@PathVariable Long id){
    //
    //    }

    @PreAuthorize("@helpForService.isOwner(#id,authentication.principal.id)&&hasRole('ORGANIZER') or hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<EventResponse> update(@Valid@RequestBody EventUpdateRequest request, @PathVariable Long id){
        return ResponseEntity.ok(eventService.update(id,request));
    }
//
//    @PatchMapping("{id}/publish")
//    public ResponseEntity<EventResponse> publish(@PathVariable Long id){
//
//    }
//
//    @PatchMapping("{id}/cancel")
//    public ResponseEntity<EventResponse> cancel(@PathVariable Long id){
//
//    }
//
//    @PatchMapping("{id}/complite")
//    public ResponseEntity<EventResponse> complite(@PathVariable Long id){
//
//    }
//
//    @DeleteMapping("/{id}")
//    public ResponseEntity<Void> delete(@PathVariable Long id){
//
//    }
}
