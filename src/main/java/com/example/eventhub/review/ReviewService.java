package com.example.eventhub.review;

import com.example.eventhub.booking.Booking;
import com.example.eventhub.booking.BookingRepository;
import com.example.eventhub.enums.BookingStatus;
import com.example.eventhub.enums.EventStatus;
import com.example.eventhub.event.Event;
import com.example.eventhub.event.EventRepository;
import com.example.eventhub.exception.NoConfirmedBookingException;
import com.example.eventhub.helper.HelpForService;
import com.example.eventhub.review.dto.ReviewCreateRequest;
import com.example.eventhub.review.dto.ReviewResponse;
import com.example.eventhub.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final HelpForService helpForService;
    private final EventRepository eventRepository;
    private final BookingRepository bookingRepository;

    @Transactional(readOnly = true)
    public List<ReviewResponse> getReviewsByEvent(Long eventId, Pageable pageable){
        helpForService.idCheck(eventId,eventRepository,"Event");
        return reviewRepository.findByEvent_Id(eventId,pageable).getContent().stream().map(this::toResponse).toList();
    }

    @Transactional
    public ReviewResponse create(User user, Long eventId, ReviewCreateRequest request){
        Event event = helpForService.idCheck(eventId,eventRepository,"Event");
        boolean bookingConfirmed=bookingRepository.existsByEvent_IdAndUser_IdAndStatus(eventId,user.getId(),BookingStatus.CONFIRMED);
        if (!bookingConfirmed){
            throw new NoConfirmedBookingException("You do not have a confirmed booking");
        }
        if(!event.getStatus().equals(EventStatus.COMPLETED)){
            throw new IllegalStateException("You cannot leave a review for an event that has not yet concluded");
        }
        if (reviewRepository.existsByEvent_IdAndUser_Id(eventId,user.getId())){
            throw new IllegalStateException("You have already left a review for this event");
        }
        Review review = new Review(event,user,request.rating(),request.comment());
        return toResponse(reviewRepository.save(review));

    }

    @Transactional
    public void delete(Long reviewId){
        Review review=helpForService.idCheck(reviewId,reviewRepository,"Review");
        reviewRepository.delete(review);
    }

    public ReviewResponse toResponse(Review review){
        return new ReviewResponse(review.getId(),review.getEvent().getId(),review.getUser().getId(),
                review.getRating(),review.getComment(),review.getCreatedAt());
    }
}
