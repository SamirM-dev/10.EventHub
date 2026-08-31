package com.example.eventhub.unit;

import com.example.eventhub.booking.Booking;
import com.example.eventhub.booking.BookingRepository;
import com.example.eventhub.event.Event;
import com.example.eventhub.event.EventRepository;
import com.example.eventhub.helper.HelpForService;
import com.example.eventhub.review.Review;
import com.example.eventhub.review.ReviewRepository;
import com.example.eventhub.user.User;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class HelpForServiceTest {
    @Mock
    BookingRepository bookingRepository;
    @Mock
    EventRepository eventRepository;
    @Mock
    ReviewRepository reviewRepository;
    @InjectMocks
    HelpForService helpForService;

    @Nested
    @DisplayName("Тестирование метода проверки айди и получения сущности из базы данных")
    class IdCheckTest{
        @Test
        void idCheck_InValidId_ThrowsIllegalArgumentException(){
            assertThatThrownBy(()->helpForService.idCheck(-5L,eventRepository,"")).isInstanceOf(IllegalArgumentException.class);
        }
        @Test
        void idCheck_NotExistsEntity_ThrowsEntityNitFoundException(){
            when(eventRepository.findById(any())).thenReturn(Optional.empty());

            assertThatThrownBy(()->helpForService.idCheck(1L,eventRepository,"")).isInstanceOf(EntityNotFoundException.class);
        }
        @Test
        void idCheck_ExistsEntity_ReturnsEntity(){
            Event event=new Event();
            when(eventRepository.findById(any())).thenReturn(Optional.of(event));

            assertThat(helpForService.idCheck(1L,eventRepository,"")).isEqualTo(event);
        }
    }
    @Nested
    @DisplayName("Тестирование метода расчёта свободных мест на мероприятие")
    class CalculateAvailableSeatsTest{
        @Test
        void calculateAvailableSeats_NotEnoughSeats_ReturnsNegativeNumber(){
            Event event = new Event();
            event.setId(1L);
            event.setCapacity(5);
            when(bookingRepository.countOccupiedSeatByEventId(1L)).thenReturn(10);

            assertThat(helpForService.calculateAvailableSeats(event)).isLessThan(0);
        }
        @Test
        void calculateAvailableSeats_EnoughSeats_ReturnsPosetiveNumber(){
            Event event = new Event();
            event.setId(1L);
            event.setCapacity(5);
            when(bookingRepository.countOccupiedSeatByEventId(1L)).thenReturn(1);

            assertThat(helpForService.calculateAvailableSeats(event)).isGreaterThan(0);
        }
    }
    @Nested
    @DisplayName("Тестирование метода проверки является ли пользователь организатором мероприятия")
    class IsEventOwnerTest{
        @Test
        void isEventOwner_NotExistsEvent_ReturnsFalse(){
            User user=new User();
            user.setId(1L);
            Event event=new Event();
            event.setId(1L);
            event.setOrganizer(user);
            when(eventRepository.findById(any())).thenReturn(Optional.empty());

            assertThat(helpForService.isEventOwner(1L,1L)).isFalse();
        }
        @Test
        void isEventOwner_NotOwnerEvent_ReturnsFalse(){
            User user=new User();
            user.setId(1L);
            Event event=new Event();
            event.setId(1L);
            event.setOrganizer(user);
            when(eventRepository.findById(any())).thenReturn(Optional.of(event));

            assertThat(helpForService.isEventOwner(1L,2L)).isFalse();
        }
        @Test
        void isEventOwner_Owner_ReturnsTrue(){
            User user=new User();
            user.setId(1L);
            Event event=new Event();
            event.setId(1L);
            event.setOrganizer(user);
            when(eventRepository.findById(any())).thenReturn(Optional.of(event));

            assertThat(helpForService.isEventOwner(1L,1L)).isTrue();
        }

    }
    @Nested
    @DisplayName("Тестирование метода проверки является ли пользователь владельцем брони")
    class IsBookingOwnerTest{
        @Test
        void isBookingOwner_NotExistsEvent_ReturnsFalse(){
            User user=new User();
            user.setId(1L);
            Booking booking=new Booking();
            booking.setUser(user);
            when(bookingRepository.findById(any())).thenReturn(Optional.empty());

            assertThat(helpForService.isBookingOwner(1L,1L)).isFalse();
        }
        @Test
        void isBookingOwner_NotOwnerEvent_ReturnsFalse(){
            User user=new User();
            user.setId(1L);
            Booking booking=new Booking();
            booking.setUser(user);
            when(bookingRepository.findById(any())).thenReturn(Optional.of(booking));

            assertThat(helpForService.isBookingOwner(1L,2L)).isFalse();
        }
        @Test
        void isBookingOwner_Owner_ReturnsTrue(){
            User user=new User();
            user.setId(1L);
            Booking booking=new Booking();
            booking.setUser(user);
            booking.setId(1L);
            when(bookingRepository.findById(any())).thenReturn(Optional.of(booking));

            assertThat(helpForService.isBookingOwner(1L,1L)).isTrue();
        }
    }
    @Nested
    @DisplayName("Тестирование метода проверки является ли пользователь автором отзыва")
    class IsReviewOwnerTest{
        @Test
        void isReviewOwner_NotExistsEvent_ReturnsFalse(){
            User user=new User();
            user.setId(1L);
            Review review=new Review();
            review.setUser(user);
            when(reviewRepository.findById(any())).thenReturn(Optional.empty());

            assertThat(helpForService.isReviewOwner(1L,1L)).isFalse();
        }
        @Test
        void isReviewOwner_NotOwnerEvent_ReturnsFalse(){
            User user=new User();
            user.setId(1L);
            Review review=new Review();
            review.setUser(user);
            when(reviewRepository.findById(any())).thenReturn(Optional.of(review));

            assertThat(helpForService.isReviewOwner(1L,2L)).isFalse();
        }
        @Test
        void isReviewOwner_Owner_ReturnsTrue(){
            User user=new User();
            user.setId(1L);
            Review review=new Review();
            review.setUser(user);
            when(reviewRepository.findById(any())).thenReturn(Optional.of(review));

            assertThat(helpForService.isReviewOwner(1L,1L)).isTrue();
        }
    }

}
