package com.example.eventhub.integrity;

import com.example.eventhub.auth.details.CustomUserDetailsService;
import com.example.eventhub.auth.details.UserPrincipal;
import com.example.eventhub.auth.jwt.JwtTokenProvider;
import com.example.eventhub.booking.Booking;
import com.example.eventhub.booking.BookingController;
import com.example.eventhub.booking.BookingRepository;
import com.example.eventhub.booking.BookingService;
import com.example.eventhub.booking.dto.BookingCreateRequest;
import com.example.eventhub.booking.dto.BookingResponse;
import com.example.eventhub.config.TestSecurityConfig;
import com.example.eventhub.enums.BookingStatus;
import com.example.eventhub.enums.UserRole;
import com.example.eventhub.event.Event;
import com.example.eventhub.event.EventRepository;
import com.example.eventhub.helper.HelpForService;
import com.example.eventhub.review.ReviewRepository;
import com.example.eventhub.user.User;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.autoconfigure.WebMvcProperties;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookingController.class)
@Import({TestSecurityConfig.class, HelpForService.class})
public class BookingControllerTest {
    @Autowired
    MockMvc mock;
    @Autowired
    ObjectMapper json;
    @MockitoBean
    BookingService bookingService;
    @MockitoBean
    EventRepository eventRepository;
    @MockitoBean
    BookingRepository bookingRepository;
    @MockitoBean
    ReviewRepository reviewRepository;
    @MockitoBean
    JwtTokenProvider jwtTokenProvider;
    @MockitoBean
    CustomUserDetailsService customUserDetailsService;

    @Nested
    @DisplayName("Тестирование метода получения своей брони")
    class GetMyTest{
        @Test
        void getMy_NotAuthenticated_Returns403()throws Exception{
            mock.perform(get("/api/v1/bookings").with(anonymous())).andExpect(status().isForbidden());
        }
        @Test
        void getMy_ValidRequest_Returns200()throws Exception{
            User user=new User("User","user@gmail.com","12345678", UserRole.USER);
            UserPrincipal principal=new UserPrincipal(user);
            Authentication auth= new UsernamePasswordAuthenticationToken(principal,null,principal.getAuthorities());
            mock.perform(get("/api/v1/bookings").with(authentication(auth))).andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("Тестирование метода оформления брони")
    class CreateTest{
        @Test
        void create_InValidQuantity_Returns400()throws Exception{
            User user=new User("User","user@gmail.com","12345678", UserRole.USER);
            UserPrincipal principal=new UserPrincipal(user);
            Authentication auth= new UsernamePasswordAuthenticationToken(principal,null,principal.getAuthorities());
            BookingCreateRequest request=new BookingCreateRequest(-1);

            mock.perform(post("/api/v1/events/1/bookings").with(authentication(auth))
                    .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(request))).andExpect(status().isBadRequest());
        }
        @Test
        void create_ValidRequest_Returns201()throws Exception{
            User user=new User("User","user@gmail.com","12345678", UserRole.USER);
            UserPrincipal principal=new UserPrincipal(user);
            Authentication auth= new UsernamePasswordAuthenticationToken(principal,null,principal.getAuthorities());
            BookingCreateRequest request=new BookingCreateRequest(1);
            BookingResponse response=new BookingResponse(1L,1L,1L,request.quantity(), BigDecimal.TWO, BookingStatus.CONFIRMED, LocalDateTime.now());
            when(bookingService.create(any(),any(),any())).thenReturn(response);

            mock.perform(post("/api/v1/events/1/bookings").with(authentication(auth))
                    .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(request))).andExpect(status().isCreated());
        }
    }

    @Nested
    @DisplayName("Тестирование метода отмены своей брони")
    class CancelTest{
        @Test
        void cancel_NotExistsBooking_Returns404()throws Exception{
            when(bookingService.cancel(any())).thenThrow(new EntityNotFoundException());
            User user=new User("User","user@gmail.com","12345678", UserRole.USER);
            user.setId(1L);
            UserPrincipal principal=new UserPrincipal(user);
            Authentication auth= new UsernamePasswordAuthenticationToken(principal,null,principal.getAuthorities());
            Booking booking=new Booking();
            booking.setUser(user);
            when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

            mock.perform(patch("/api/v1/bookings/1/cancel").with(authentication(auth))).andExpect(status().isNotFound());
        }
        @Test
        void cancel_ValidRequest_Returns200()throws Exception{
            User user=new User("User","user@gmail.com","12345678", UserRole.USER);
            user.setId(1L);
            UserPrincipal principal=new UserPrincipal(user);
            Authentication auth= new UsernamePasswordAuthenticationToken(principal,null,principal.getAuthorities());
            Booking booking=new Booking();
            booking.setUser(user);
            when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

            mock.perform(patch("/api/v1/bookings/1/cancel").with(authentication(auth))).andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("Тестирование метода получения броней по мероприятию")
    class BookingsByEventTest{
        @Test
        void bookingsByEvent_NotEventOwner_Returns403()throws Exception{
            User owner=new User("Owner","owner@gmail.com","12345678", UserRole.ORGANIZER);
            owner.setId(1L);
            User user=new User("User","user@gmail.com","12345678", UserRole.USER);
            user.setId(2L);
            UserPrincipal principal=new UserPrincipal(user);
            Authentication auth= new UsernamePasswordAuthenticationToken(principal,null,principal.getAuthorities());
            Event event = new Event();
            event.setOrganizer(owner);
            when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

            mock.perform(get("/api/v1/events/1/bookings").with(authentication(auth))).andExpect(status().isForbidden());
        }
        @Test
        void bookingsByEvent_ValidRequest_Returns200()throws Exception{
            User owner=new User("Owner","owner@gmail.com","12345678", UserRole.ORGANIZER);
            owner.setId(1L);
            UserPrincipal principal=new UserPrincipal(owner);
            Authentication auth= new UsernamePasswordAuthenticationToken(principal,null,principal.getAuthorities());
            Event event = new Event();
            event.setOrganizer(owner);
            when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

            mock.perform(get("/api/v1/events/1/bookings").with(authentication(auth))).andExpect(status().isOk());
        }
    }
}
