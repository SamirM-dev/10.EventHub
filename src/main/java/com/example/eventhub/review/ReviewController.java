package com.example.eventhub.review;

import com.example.eventhub.auth.details.UserPrincipal;
import com.example.eventhub.review.dto.ReviewCreateRequest;
import com.example.eventhub.review.dto.ReviewResponse;
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
@RequestMapping("/api/v1")
@RequiredArgsConstructor public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping("/events/{eventId}/reviews")
    public ResponseEntity<List<ReviewResponse>> getReviewsByEvent(@PathVariable Long eventId, @ParameterObject Pageable pageable){
        return ResponseEntity.ok(reviewService.getReviewsByEvent(eventId,pageable));
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/events/{eventId}/reviews")
    public ResponseEntity<ReviewResponse> getReviewsByEvent(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long eventId, @RequestBody ReviewCreateRequest request){
        ReviewResponse created= reviewService.create(principal.getUser(),eventId,request);
        return ResponseEntity.created(URI.create("/api/v1/events/"+eventId+"/reviews/"+created.id())).body(created);
    }

    @PreAuthorize("@helpForService.isReviewOwner(#id,authentication.principal.id) OR hasRole('ADMIN')")
    @DeleteMapping("/reviews/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id){
        reviewService.delete(id);
        return ResponseEntity.noContent().build();
    }


}
