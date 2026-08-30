package com.example.eventhub.booking;

import com.example.eventhub.auth.details.UserPrincipal;
import com.example.eventhub.booking.dto.BookingCreateRequest;
import com.example.eventhub.booking.dto.BookingResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/bookings")
    public ResponseEntity<List<BookingResponse>> getMy(@AuthenticationPrincipal UserPrincipal principal){
        return ResponseEntity.ok(bookingService.getMyBookings(principal.getId()));
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/events/{eventId}/bookings")
    public ResponseEntity<BookingResponse> create(@AuthenticationPrincipal UserPrincipal principal,@PathVariable Long eventId,@RequestBody BookingCreateRequest request){
        BookingResponse created = bookingService.create(principal.getUser(),eventId,request);
        return ResponseEntity.created(URI.create("/api/v1/events/"+eventId+"/bookings/"+created.id())).body(created);
    }
    @PreAuthorize("@helpForService.isBookingOwner(#id,authentication.principal.id)")
    @PatchMapping("/bookings/{id}/cancel")
    public ResponseEntity<BookingResponse> cancel(@PathVariable Long id){
        return ResponseEntity.ok(bookingService.cancel(id));
    }

    @PreAuthorize("@helpForService.isEventOwner(#eventId,authentication.principal.id) OR hasRole('ADMIN')")
    @GetMapping("/events/{eventId}/bookings")
    public ResponseEntity<List<BookingResponse>> bookingsByEvent(@PathVariable Long eventId){
        return ResponseEntity.ok(bookingService.getBookingsByEvent(eventId));
    }

}
