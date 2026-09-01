package com.example.eventhub.integrity;

import com.example.eventhub.auth.details.CustomUserDetailsService;
import com.example.eventhub.auth.details.UserPrincipal;
import com.example.eventhub.auth.jwt.JwtTokenProvider;
import com.example.eventhub.booking.BookingRepository;
import com.example.eventhub.config.TestSecurityConfig;
import com.example.eventhub.enums.EventCategory;
import com.example.eventhub.enums.EventStatus;
import com.example.eventhub.enums.UserRole;
import com.example.eventhub.event.Event;
import com.example.eventhub.event.EventController;
import com.example.eventhub.event.EventRepository;
import com.example.eventhub.event.EventService;
import com.example.eventhub.event.dto.EventCreateRequest;
import com.example.eventhub.event.dto.EventResponse;
import com.example.eventhub.event.dto.EventUpdateRequest;
import com.example.eventhub.helper.HelpForService;
import com.example.eventhub.review.ReviewRepository;
import com.example.eventhub.unit.HelpForServiceTest;
import com.example.eventhub.user.User;
import jakarta.persistence.EntityNotFoundException;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.postgresql.hostchooser.HostRequirement.any;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EventController.class)
@Import({TestSecurityConfig.class, HelpForService.class})
public class EventControllerTest {

    @Autowired
    MockMvc mock;
    @Autowired
    ObjectMapper json;
    @MockitoBean
    EventRepository eventRepository;
    @MockitoBean
    BookingRepository bookingRepository;
    @MockitoBean
    ReviewRepository reviewRepository;
    @MockitoBean
    EventService eventService;
    @MockitoBean
    CustomUserDetailsService customUserDetailsService;
    @MockitoBean
    JwtTokenProvider jwtTokenProvider;

    @Nested
    @DisplayName("Тестирование метода создания события")
    class CreateTest{
        @Test
        void create_SimpleUser_Returns403()throws Exception{
            User user=new User("User","user@gmail.com","12345678", UserRole.USER);
            UserPrincipal principal=new UserPrincipal(user);
            Authentication auth=new UsernamePasswordAuthenticationToken(principal,null,principal.getAuthorities());
            EventCreateRequest request=new EventCreateRequest(
                    "Event1","Event1", EventCategory.CONCERT,"Baku", LocalDateTime.of(2026,12,12,19,0,0)
                    ,LocalDateTime.of(2026,12,15,19,0,0),50, BigDecimal.TWO
            );

            mock.perform(post("/api/v1/events").with(authentication(auth)).contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(request))).andExpect(status().isForbidden());

        }
        @Test
        void create_BlankTitle_Returns400()throws Exception{
            User user=new User("User","user@gmail.com","12345678", UserRole.ORGANIZER);
            UserPrincipal principal=new UserPrincipal(user);
            Authentication auth=new UsernamePasswordAuthenticationToken(principal,null,principal.getAuthorities());
            EventCreateRequest request=new EventCreateRequest(
                    "","", EventCategory.CONCERT,"Baku", LocalDateTime.of(2026,12,12,19,0,0)
                    ,LocalDateTime.of(2026,12,15,19,0,0),50, BigDecimal.TWO
            );

            mock.perform(post("/api/v1/events").with(authentication(auth)).contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(request))).andExpect(status().isBadRequest());        }
        @Test
        void create_ValidData_ReturnsEventResponse()throws Exception{
            User user=new User("User","user@gmail.com","12345678", UserRole.ORGANIZER);
            user.setId(1L);
            UserPrincipal principal=new UserPrincipal(user);
            Authentication auth=new UsernamePasswordAuthenticationToken(principal,null,principal.getAuthorities());
            EventCreateRequest request=new EventCreateRequest(
                    "Event1","Event1", EventCategory.CONCERT,"Baku", LocalDateTime.of(2026,12,12,19,0,0)
                    ,LocalDateTime.of(2026,12,15,19,0,0),50, BigDecimal.TWO
            );
            EventResponse response=new EventResponse(1L,request.title(),request.description(),request.category(),request.venue(),request.startTime(),request.endTime(),request.capacity(),5,request.price(), EventStatus.DRAFT,1L,LocalDateTime.now());
            when(eventService.create(eq(request),any())).thenReturn(response);

            mock.perform(post("/api/v1/events").with(authentication(auth)).contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(request))).andExpect(status().isCreated());
        }
    }

    @Nested
    @DisplayName("Тестирование метода получения всех событий")
    class GetAllTest{

    }

    @Nested
    @DisplayName("Тестирование метода получения 1 события")
    class GetOneTest{
        @Test
        void getOne_NotExistsEvent_Returns404()throws Exception{
            when(eventService.getById(any(),any())).thenThrow(new EntityNotFoundException());
            User user=new User("User","user@gmail.com","12345678", UserRole.ORGANIZER);
            user.setId(1L);
            UserPrincipal principal=new UserPrincipal(user);
            Authentication auth=new UsernamePasswordAuthenticationToken(principal,null,principal.getAuthorities());

            mock.perform(get("/api/v1/events/1").with(authentication(auth))).andExpect(status().isNotFound());
        }
        @Test
        void getOne_ExistsEvent_Returns200()throws Exception{
            EventResponse response=new EventResponse(
                    1L,"Event","Event",EventCategory.CONCERT,"Baku",LocalDateTime.of(2026,12,12,19,0,0)
                    ,LocalDateTime.of(2026,12,15,19,0,0),50,10,BigDecimal.TWO,EventStatus.PUBLISHED,1L,LocalDateTime.now()
            );
            when(eventService.getById(any(),any())).thenReturn(response);

            mock.perform(get("/api/v1/events/1").with(anonymous())).andExpect(status().isOk());

        }
    }

    @Nested
    @DisplayName("Тестирование метода обновления события")
    class UpdateTest{
        @Test
        void update_NotOwner_Returns403() throws Exception{
            User user=new User("User","user@gmail.com","12345678", UserRole.ORGANIZER);
            user.setId(1L);
            User notOwner=new User("User2","user2@gmail.com","12345678", UserRole.ORGANIZER);
            notOwner.setId(11L);
            Event event = new Event();
            event.setId(1L);
            event.setOrganizer(user);
            UserPrincipal principal=new UserPrincipal(notOwner);
            Authentication auth=new UsernamePasswordAuthenticationToken(principal,null,principal.getAuthorities());
            EventUpdateRequest request=new EventUpdateRequest(
                    "Event1","Event1", EventCategory.CONCERT,"Baku", LocalDateTime.of(2026,12,12,19,0,0)
                    ,LocalDateTime.of(2026,12,15,19,0,0),50, BigDecimal.TWO,EventStatus.DRAFT
            );
            EventResponse response=new EventResponse(1L,request.title(),request.description(),request.category(),request.venue(),request.startTime(),request.endTime(),request.capacity(),5,request.price(), EventStatus.DRAFT,1L,LocalDateTime.now());
            when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
            when(eventService.update(1L,request)).thenReturn(response);

            mock.perform(put("/api/v1/events/1").with(authentication(auth)).contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(request))).andExpect(status().isForbidden());

        }
        @Test
        void update_ValidData_Returns200()throws Exception{
            User user=new User("User","user@gmail.com","12345678", UserRole.ORGANIZER);
            user.setId(1L);
            Event event = new Event();
            event.setId(1L);
            event.setOrganizer(user);
            UserPrincipal principal=new UserPrincipal(user);
            Authentication auth=new UsernamePasswordAuthenticationToken(principal,null,principal.getAuthorities());
            EventUpdateRequest request=new EventUpdateRequest(
                    "Event1","Event1", EventCategory.CONCERT,"Baku", LocalDateTime.of(2026,12,12,19,0,0)
                    ,LocalDateTime.of(2026,12,15,19,0,0),50, BigDecimal.TWO,EventStatus.DRAFT
            );
            EventResponse response=new EventResponse(1L,request.title(),request.description(),request.category(),request.venue(),request.startTime(),request.endTime(),request.capacity(),5,request.price(), EventStatus.DRAFT,1L,LocalDateTime.now());
            when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
            when(eventService.update(1L,request)).thenReturn(response);

            mock.perform(put("/api/v1/events/1").with(authentication(auth)).contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(request))).andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("Тестирование метода публикации события")
    class PublishTest{

    }

    @Nested
    @DisplayName("Тестирование метода отмены события")
    class CancelTest{

    }

    @Nested
    @DisplayName("Тестирование метода завершения события")
    class CompleteTest{

    }

    @Nested
    @DisplayName("Тестирование метода удаления события")
    class DeleteTest{

    }
}
