package com.example.eventhub.unit;

import com.example.eventhub.booking.Booking;
import com.example.eventhub.booking.BookingRepository;
import com.example.eventhub.booking.BookingService;
import com.example.eventhub.booking.dto.BookingCreateRequest;
import com.example.eventhub.booking.dto.BookingResponse;
import com.example.eventhub.enums.BookingStatus;
import com.example.eventhub.enums.EventStatus;
import com.example.eventhub.event.Event;
import com.example.eventhub.helper.HelpForService;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BookingServiceTest {

    @Mock
    HelpForService helpForService;
    @Mock
    BookingRepository bookingRepository;
    @InjectMocks
    BookingService bookingService;

    @Nested
    @DisplayName("Тестирование метода получения своих броней")
    class GetMyBookingsTest{
        @Test
        void getMyBookings_ValidData_ReturnsListOfBookingResponse(){
            User user=new User();
            user.setId(1L);
            Event event=new Event();
            event.setId(1L);
            Booking booking=new Booking();
            booking.setId(1L);
            booking.setUser(user);
            booking.setEvent(event);
            when(bookingRepository.findByUserId(any())).thenReturn(List.of(booking));

            assertThat(bookingService.getMyBookings(1L)).isInstanceOf(List.class).hasSize(1).element(0).isEqualTo(bookingService.toResponse(booking));
        }
    }

    @Nested
    @DisplayName("Тестирование метода создания брони")
    class CreateTest{
        @Test
        void create_NotExistsEvent_ThrowsException(){
            when(helpForService.idCheck(any(),any(),any())).thenThrow(new EntityNotFoundException());

            assertThatThrownBy(()->bookingService.create(any(),any(),any())).isInstanceOf(EntityNotFoundException.class);
        }
        @Test
        void create_NotPublishedEvent_ThrowsException(){
            User user = new User();
            user.setId(1L);
            Event event = new Event();
            event.setId(1L);
            event.setStatus(EventStatus.DRAFT);
            when(helpForService.idCheck(any(),any(),any())).thenReturn(event);

            assertThatThrownBy(()->bookingService.create(any(),any(),any())).isInstanceOf(IllegalStateException.class).hasMessage("You cannot make a reservation for this event");

        }
        @Test
        void create_NotEnoughSeats_ThrowsException(){
            User user = new User();
            user.setId(1L);
            Event event = new Event();
            event.setId(1L);
            event.setStatus(EventStatus.PUBLISHED);
            BookingCreateRequest request = new BookingCreateRequest(2);

            when(helpForService.idCheck(any(),any(),any())).thenReturn(event);
            when(helpForService.calculateAvailableSeats(event)).thenReturn(1);

            assertThatThrownBy(()->bookingService.create(user,event.getId(),request)).isInstanceOf(IllegalArgumentException.class).hasMessage("Not enough available seats for booking");

        }
        @Test
        void create_EndedEvent_ThrowsException(){
            User user = new User();
            user.setId(1L);
            Event event = new Event();
            event.setId(1L);
            event.setStatus(EventStatus.PUBLISHED);
            event.setEndTime(LocalDateTime.now().minusDays(2));
            BookingCreateRequest request = new BookingCreateRequest(2);

            when(helpForService.idCheck(any(),any(),any())).thenReturn(event);
            when(helpForService.calculateAvailableSeats(event)).thenReturn(2);

            assertThatThrownBy(()->bookingService.create(user,event.getId(),request)).isInstanceOf(IllegalStateException.class).hasMessage("The event has already concluded");

        }
        @Test
        void create_ValidData_ReturnsBookingResponse(){
            User user = new User();
            user.setId(1L);
            Event event = new Event();
            event.setId(1L);
            event.setStatus(EventStatus.PUBLISHED);
            event.setEndTime(LocalDateTime.now().plusDays(2));
            event.setPrice(BigDecimal.valueOf(1));
            BookingCreateRequest request = new BookingCreateRequest(2);
            Booking booking = new Booking(event,user,request.quantity(),event.getPrice().multiply(BigDecimal.valueOf(request.quantity())));

            when(bookingRepository.save(any())).thenReturn(booking);
            when(helpForService.idCheck(any(),any(),any())).thenReturn(event);
            when(helpForService.calculateAvailableSeats(event)).thenReturn(2);
            ArgumentCaptor<Booking> captor = ArgumentCaptor.forClass(Booking.class);

            assertThat(bookingService.create(user,event.getId(),request)).isInstanceOf(BookingResponse.class);

            verify(bookingRepository).save(captor.capture());
            SoftAssertions.assertSoftly(soft->{
                soft.assertThat(captor.getValue().getEvent()).isEqualTo(event);
                soft.assertThat(captor.getValue().getUser()).isEqualTo(user);
                soft.assertThat(captor.getValue().getQuantity()).isEqualTo(request.quantity());
                soft.assertThat(captor.getValue().getTotalPrice()).isEqualTo(event.getPrice().multiply(BigDecimal.valueOf(request.quantity())));
                soft.assertThat(captor.getValue().getStatus()).isEqualTo(BookingStatus.CONFIRMED);
                    }
            );
        }
    }

    @Nested
    @DisplayName("Тестирование метода отмены брони")
    class CancelTest{
        @Test
        void cancel_NotExists_ThrowsException(){
            when(helpForService.idCheck(any(),any(),any())).thenThrow(new EntityNotFoundException());

            assertThatThrownBy(()->bookingService.cancel(1L)).isInstanceOf(EntityNotFoundException.class);
        }
        @Test
        void cancel_StartedEvent_ThrowsException(){
            Event event = new Event();
            event.setId(1L);
            event.setStartTime(LocalDateTime.now().minusDays(2));
            Booking booking = new Booking();
            booking.setEvent(event);
            when(helpForService.idCheck(eq(1L),any(),any())).thenReturn(booking);

            assertThatThrownBy(()->bookingService.cancel(1L)).isInstanceOf(IllegalStateException.class).hasMessage("You cannot cancel a reservation for an event that has already started");
        }
        @Test
        void cancel_ValidData_ReturnsBookingResponse(){
            User user = new User();
            user.setId(1L);
            Event event = new Event();
            event.setId(1L);
            event.setStartTime(LocalDateTime.now().plusDays(2));
            Booking booking = new Booking(event,user,1,BigDecimal.valueOf(2.5));
            booking.setId(1L);
            when(helpForService.idCheck(eq(1L),any(),any())).thenReturn(booking);
            booking.setStatus(BookingStatus.CANCELLED);
            when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
            ArgumentCaptor<Booking> captor = ArgumentCaptor.forClass(Booking.class);

            assertThat(bookingService.cancel(1L)).isInstanceOf(BookingResponse.class);

            verify(bookingRepository).save(captor.capture());
            assertThat(captor.getValue().getStatus()).isEqualTo(BookingStatus.CANCELLED);

        }
    }

    @Nested
    @DisplayName("Тестирование метода получения броней по мероприятию")
    class GetBookingsByEventTest{
        @Test
        void getBookingsByEvent_NotExistsEvent_ThrowsException(){
            when(helpForService.idCheck(any(),any(),any())).thenThrow(new EntityNotFoundException());

            assertThatThrownBy(()->bookingService.getBookingsByEvent(1L)).isInstanceOf(EntityNotFoundException.class);
        }
        @Test
        void getBookingsByEvent_ValidData_ReturnsListOfBookingResponse(){
            Event event = new Event();
            event.setId(1L);
            User user = new User();
            user.setId(1L);
            Booking booking =new Booking();
            booking.setId(1L);
            booking.setEvent(event);
            booking.setUser(user);
            event.setBookings(List.of(booking));
            when(helpForService.idCheck(eq(1L),any(),any())).thenReturn(event);

            assertThat(bookingService.getBookingsByEvent(1L)).isInstanceOf(List.class).hasSize(1).element(0).isEqualTo(bookingService.toResponse(booking));
        }
    }



}
