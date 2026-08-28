package com.example.eventhub.unit;

import com.example.eventhub.enums.EventCategory;
import com.example.eventhub.enums.EventStatus;
import com.example.eventhub.event.Event;
import com.example.eventhub.event.EventRepository;
import com.example.eventhub.event.EventService;
import com.example.eventhub.event.dto.EventCreateRequest;
import com.example.eventhub.event.dto.EventResponse;
import com.example.eventhub.event.dto.EventUpdateRequest;
import com.example.eventhub.helper.HelpForService;
import com.example.eventhub.user.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class EventServiceTest {

    @Mock
    EventRepository eventRepository;
    @Mock
    HelpForService helpForService;
    @InjectMocks
    EventService eventService;

    @Nested
    @DisplayName("Тестирование метода создания события")
    class CreateTest{
        @Test
        void createEvent_ValidData_ReturnsEventResponse(){
            when(helpForService.calculateAvailableSeats(any())).thenReturn(10);
            User user=new User();
            user.setId(1L);
            Event event=new Event();
            event.setId(1L);
            event.setOrganizer(user);
            when(eventRepository.save(any())).thenReturn(event);

            assertThat(eventService.create(
                    new EventCreateRequest("title","description", EventCategory.CONCERT,"venue",
                    LocalDateTime.MIN,LocalDateTime.MAX,20,(BigDecimal.valueOf(10))),user)
            ).isInstanceOf(EventResponse.class).isEqualTo(eventService.toResponse(event));
        }
    }


    @Nested
    @DisplayName("Тестирование метода обновления события")
    class FullUpdateTest{
        @Test
        void update_nonExistsEvent_ThrowsException(){
            when(eventRepository.findById(any())).thenReturn(Optional.empty());

            assertThatThrownBy(()->eventService.update(1L,any()));
        }
        @Test
        void update_InValidCapacity_ThrowsException(){
            User user=new User();
            user.setId(1L);
            Event event=new Event();
            event.setId(1L);
            event.setOrganizer(user);
            event.setCapacity(10);
            when(eventRepository.findById(any())).thenReturn(Optional.of(event));
            EventUpdateRequest request = new EventUpdateRequest("title","description", EventCategory.CONCERT,"venue",
                    LocalDateTime.MIN,LocalDateTime.MAX,1,(BigDecimal.valueOf(10)), EventStatus.COMPLETED);
            when(helpForService.calculateAvailableSeats(event)).thenReturn(0);

            assertThatThrownBy(()->eventService.update(1L,request)).isInstanceOf(IllegalArgumentException.class).hasMessage("The new capacity is insufficient for the places already taken");
        }
        @Test
        void update_InvalidDate_ThrowsException(){
            User user=new User();
            user.setId(1L);
            Event event=new Event();
            event.setId(1L);
            event.setOrganizer(user);
            event.setCapacity(10);
            event.setStartTime(LocalDateTime.now().plusDays(7));
            event.setEndTime(LocalDateTime.now().plusDays(8));
            when(eventRepository.findById(any())).thenReturn(Optional.of(event));
            EventUpdateRequest request = new EventUpdateRequest("title","description", EventCategory.CONCERT,"venue",
                    LocalDateTime.MIN,LocalDateTime.MAX,100,(BigDecimal.valueOf(10)), EventStatus.COMPLETED);
            when(helpForService.calculateAvailableSeats(event)).thenReturn(0);

            assertThatThrownBy(()->eventService.update(1L,request)).isInstanceOf(IllegalArgumentException.class).hasMessage("Incorrect date data");
        }
        @Test
        void update_CompletedStatus_ThrowsException(){
            User user=new User();
            user.setId(1L);
            Event event=new Event();
            event.setId(1L);
            event.setOrganizer(user);
            event.setCapacity(10);
            event.setStartTime(LocalDateTime.now().plusDays(7));
            event.setEndTime(LocalDateTime.now().plusDays(8));
            when(eventRepository.findById(any())).thenReturn(Optional.of(event));
            EventUpdateRequest request = new EventUpdateRequest("title","description", EventCategory.CONCERT,"venue",
                    LocalDateTime.now().plusDays(1),LocalDateTime.now().plusDays(2),100,(BigDecimal.valueOf(10)), EventStatus.COMPLETED);
            when(helpForService.calculateAvailableSeats(event)).thenReturn(0);

            assertThatThrownBy(()->eventService.update(1L,request)).isInstanceOf(IllegalArgumentException.class).hasMessage("You do not have permission to complete event");
        }
        @Test
        void update_ValidData_ReturnsEventResponse(){
            User user=new User();
            user.setId(1L);
            Event event=new Event();
            event.setId(1L);
            event.setOrganizer(user);
            event.setCapacity(10);
            event.setStartTime(LocalDateTime.now().plusDays(7));
            event.setEndTime(LocalDateTime.now().plusDays(8));
            when(eventRepository.findById(any())).thenReturn(Optional.of(event));
            EventUpdateRequest request = new EventUpdateRequest("title","description", EventCategory.CONCERT,"venue",
                    LocalDateTime.now().plusDays(1),LocalDateTime.now().plusDays(2),100,(BigDecimal.valueOf(10)), EventStatus.PUBLISHED);
            when(helpForService.calculateAvailableSeats(event)).thenReturn(0);
            Event updated=new Event("title","description", EventCategory.CONCERT,"venue",
                    LocalDateTime.now().plusDays(1),LocalDateTime.now().plusDays(2),100,(BigDecimal.valueOf(10)),user);
            updated.setId(1L);
            updated.setStatus(EventStatus.PUBLISHED);
            when(eventRepository.save(any())).thenReturn(updated);


            assertThat(eventService.update(1L,request)).isInstanceOf(EventResponse.class).isEqualTo(eventService.toResponse(updated));
            ;
        }
    }
}
