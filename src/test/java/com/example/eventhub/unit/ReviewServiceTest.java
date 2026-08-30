package com.example.eventhub.unit;

import com.example.eventhub.booking.BookingRepository;
import com.example.eventhub.enums.BookingStatus;
import com.example.eventhub.enums.EventStatus;
import com.example.eventhub.event.Event;
import com.example.eventhub.event.EventRepository;
import com.example.eventhub.exception.NoConfirmedBookingException;
import com.example.eventhub.helper.HelpForService;
import com.example.eventhub.review.Review;
import com.example.eventhub.review.ReviewRepository;
import com.example.eventhub.review.ReviewService;
import com.example.eventhub.review.dto.ReviewCreateRequest;
import com.example.eventhub.review.dto.ReviewResponse;
import com.example.eventhub.user.User;
import jakarta.persistence.EntityNotFoundException;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReviewServiceTest {
    @Mock
    EventRepository eventRepository;
    @Mock
    BookingRepository bookingRepository;
    @Mock
    ReviewRepository reviewRepository;
    @Mock
    HelpForService helpForService;
    @InjectMocks
    ReviewService reviewService;

    @Nested
    @DisplayName("Тестирование метода получения отзывов по событию(с пагинацией)")
    class GetReviewsByEvent{
        @Test
        void getReviewsByEvent_NotExistsEvent_ThrowsException(){
            Pageable pageable= PageRequest.of(1,2, Sort.by("id").ascending());
            when(helpForService.idCheck(eq(1L),any(),eq("Event"))).thenThrow(new EntityNotFoundException());

            assertThatThrownBy(()->reviewService.getReviewsByEvent(1L,pageable)).isInstanceOf(EntityNotFoundException.class);
        }
        @Test
        void getReviewsByEvent_ValidData_ReturnsListOfReviewResponse(){
            User user = new User();
            user.setId(1L);
            Event event= new Event();
            event.setId(1L);
            Review review = new Review(event,user,5,"Was great!");
            Pageable pageable= PageRequest.of(1,2, Sort.by("id").ascending());
            Page<Review> page = new PageImpl<>(List.of(review));
            when(helpForService.idCheck(eq(1L),any(),eq("Event"))).thenReturn(event);
            when(reviewRepository.findByEvent_Id(event.getId(),pageable)).thenReturn(page);

            assertThat(reviewService.getReviewsByEvent(event.getId(),pageable)).isInstanceOf(List.class).hasSize(1).element(0).isEqualTo(reviewService.toResponse(review));

        }
    }

    @Nested
    @DisplayName("Тестирование метода создания отзыва")
    class CreateTest{
        @Test
        void create_NotExistsEvent_ThrowsException(){
            when(helpForService.idCheck(any(),any(),any())).thenThrow(new EntityNotFoundException());

            assertThatThrownBy(()->reviewService.create(new User(),1L,new ReviewCreateRequest(1,""))).isInstanceOf(EntityNotFoundException.class);
        }
        @Test
        void create_BookingNotConfirmed_ThrowsException(){
            User user=new User();
            user.setId(1L);
            Event event=new Event();
            event.setId(1L);
            event.setStatus(EventStatus.CANCELLED);
            ReviewCreateRequest request=new ReviewCreateRequest(5,"It was great!");
            when(helpForService.idCheck(any(),any(),any())).thenReturn(event);
            when(bookingRepository.existsByEvent_IdAndUser_IdAndStatus(event.getId(),user.getId(), BookingStatus.CONFIRMED)).thenReturn(false);


            assertThatThrownBy(()->reviewService.create(user,event.getId(),request)).isInstanceOf(NoConfirmedBookingException.class).hasMessage("You do not have a confirmed booking");
        }
        @Test
        void create_EventNotCompleted_ThrowsException(){
            User user=new User();
            user.setId(1L);
            Event event=new Event();
            event.setId(1L);
            event.setStatus(EventStatus.CANCELLED);
            ReviewCreateRequest request=new ReviewCreateRequest(5,"It was great!");
            when(helpForService.idCheck(any(),any(),any())).thenReturn(event);
            when(bookingRepository.existsByEvent_IdAndUser_IdAndStatus(event.getId(),user.getId(), BookingStatus.CONFIRMED)).thenReturn(true);

            assertThatThrownBy(()->reviewService.create(user,event.getId(),request)).isInstanceOf(IllegalStateException.class).hasMessage("You cannot leave a review for an event that has not yet concluded");
        }
        @Test
        void create_ReviewAlreadyExists_ThrowsException(){
            User user=new User();
            user.setId(1L);
            Event event=new Event();
            event.setId(1L);
            event.setStatus(EventStatus.COMPLETED);
            ReviewCreateRequest request=new ReviewCreateRequest(5,"It was great!");
            when(helpForService.idCheck(any(),any(),any())).thenReturn(event);
            when(bookingRepository.existsByEvent_IdAndUser_IdAndStatus(event.getId(),user.getId(), BookingStatus.CONFIRMED)).thenReturn(true);
            when(reviewRepository.existsByEvent_IdAndUser_Id(event.getId(),user.getId())).thenReturn(true);

            assertThatThrownBy(()->reviewService.create(user,event.getId(),request)).isInstanceOf(IllegalStateException.class).hasMessage("You have already left a review for this event");

        }
        @Test
        void create_ValidData_ReturnsReviewResponse(){
            User user=new User();
            user.setId(1L);
            Event event=new Event();
            event.setId(1L);
            event.setStatus(EventStatus.COMPLETED);
            ReviewCreateRequest request=new ReviewCreateRequest(5,"It was great!");
            Review review = new Review(event, user,request.rating(),request.comment());
            when(helpForService.idCheck(any(),any(),any())).thenReturn(event);
            when(bookingRepository.existsByEvent_IdAndUser_IdAndStatus(event.getId(),user.getId(), BookingStatus.CONFIRMED)).thenReturn(true);
            when(reviewRepository.existsByEvent_IdAndUser_Id(event.getId(),user.getId())).thenReturn(false);
            when(reviewRepository.save(any())).thenReturn(review);
            ArgumentCaptor<Review> captor=ArgumentCaptor.forClass(Review.class);

            assertThat(reviewService.create(user,event.getId(),request)).isInstanceOf(ReviewResponse.class);
            verify(reviewRepository).save(captor.capture());
            SoftAssertions.assertSoftly(soft->{
                soft.assertThat(captor.getValue().getUser()).isEqualTo(user);
                soft.assertThat(captor.getValue().getEvent()).isEqualTo(event);
                soft.assertThat(captor.getValue().getRating()).isEqualTo(request.rating());
                soft.assertThat(captor.getValue().getComment()).isEqualTo(request.comment());
            });
        }
    }

    @Nested
    @DisplayName("Тестирование метода удаления отзыва")
    class DeleteTest{
        @Test
        void delete_NotExistsReview_ThrowsException(){
            when(helpForService.idCheck(any(),any(),any())).thenThrow(new EntityNotFoundException());

            assertThatThrownBy(()->reviewService.delete(1L)).isInstanceOf(EntityNotFoundException.class);

        }
        @Test
        void delete_ValidData_ReturnsNothing(){
            Review review = new Review();
            when(helpForService.idCheck(any(),any(),any())).thenReturn(review);

            reviewService.delete(1L);
            verify(reviewRepository).delete(review);
            verifyNoMoreInteractions(reviewRepository);
        }
    }
}
