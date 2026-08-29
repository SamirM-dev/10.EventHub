package com.example.eventhub.unit;

import com.example.eventhub.auth.details.UserPrincipal;
import com.example.eventhub.enums.EventCategory;
import com.example.eventhub.enums.EventStatus;
import com.example.eventhub.enums.UserRole;
import com.example.eventhub.event.Event;
import com.example.eventhub.event.EventRepository;
import com.example.eventhub.event.EventService;
import com.example.eventhub.event.dto.EventCreateRequest;
import com.example.eventhub.event.dto.EventResponse;
import com.example.eventhub.event.dto.EventUpdateRequest;
import com.example.eventhub.helper.HelpForService;
import com.example.eventhub.user.User;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

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
        }
    }

    @Nested
    @DisplayName("Тестирование метода получения всех события(с сортировкой и пагинацией)")
    class GetAllTest{
        @Test
        void getAll_NullRrValidCategory_ReturnsListOfEventResponses(){
            User user=new User();
            user.setId(1L);
            Event event=new Event();
            event.setId(1L);
            event.setOrganizer(user);
            Page<Event> page= new PageImpl<>(List.of(event));
            when(eventRepository.getAllWithPaginationAndFilter(any(),eq(EventStatus.PUBLISHED),any())).thenReturn(page);
            Pageable pageable = PageRequest.of(0,2,Sort.by("id").ascending());

            assertThat(eventService.getAll("CONCERT",pageable)).isInstanceOf(List.class).hasSize(1).element(0).isInstanceOf(EventResponse.class);
        }
    }

    @Nested
    @DisplayName("Тестирование метода получения события по айди")
    class GetByIdTest{
        @Test
        void gebById_NotFound_ThrowsException(){
            User user=new User();
            user.setRole(UserRole.USER);
            user.setId(1L);
            when(helpForService.idCheck(any(),any(),any())).thenThrow(new EntityNotFoundException());

            assertThatThrownBy(()->eventService.getById(new UserPrincipal(user),1L)).isInstanceOf(EntityNotFoundException.class);
        }
        @Test
        void getById_NoPermission_ThrowsException(){
            User user=new User();
            user.setRole(UserRole.USER);
            user.setId(1L);
            User organizer=new User();
            organizer.setId(2L);
            organizer.setRole(UserRole.ORGANIZER);
            Event event=new Event();
            event.setId(1L);
            event.setOrganizer(organizer);
            event.setStatus(EventStatus.DRAFT);
            when(helpForService.idCheck(any(),any(),any())).thenReturn(event);

            assertThatThrownBy(()->eventService.getById(new UserPrincipal(user),1L)).isInstanceOf(EntityNotFoundException.class);

        }
        @Test
        void getById_NotPublishedButOrganizer_ReturnsEventResponse(){
            User user=new User();
            user.setRole(UserRole.USER);
            user.setId(1L);
            User organizer=new User();
            organizer.setId(2L);
            organizer.setRole(UserRole.ORGANIZER);
            Event event=new Event();
            event.setId(1L);
            event.setOrganizer(organizer);
            event.setStatus(EventStatus.DRAFT);
            when(helpForService.idCheck(any(),any(),any())).thenReturn(event);

            assertThat(eventService.getById(new UserPrincipal(organizer),1L)).isInstanceOf(EventResponse.class).isEqualTo(eventService.toResponse(event));
        }
        @Test
        void getById_PublishedAndUser_ReturnsEventResponse(){
            User user=new User();
            user.setRole(UserRole.USER);
            user.setId(1L);
            User organizer=new User();
            organizer.setId(2L);
            organizer.setRole(UserRole.ORGANIZER);
            Event event=new Event();
            event.setId(1L);
            event.setOrganizer(organizer);
            event.setStatus(EventStatus.PUBLISHED);
            when(helpForService.idCheck(any(),any(),any())).thenReturn(event);

            assertThat(eventService.getById(new UserPrincipal(user),1L)).isInstanceOf(EventResponse.class).isEqualTo(eventService.toResponse(event));

        }

    }

    @Nested
    @DisplayName("Тестирование метода публикации события")
    class PublishTest{
        @Test
        void publish_NotFoundEvent_ThrowsException(){
            when(helpForService.idCheck(any(),any(),any())).thenThrow(new EntityNotFoundException());

            assertThatThrownBy(()->eventService.publish(1L)).isInstanceOf(EntityNotFoundException.class);
        }
        @Test
        void publish_ExpiredEvent_ThrowsException(){
            User organizer=new User();
            organizer.setId(1L);
            organizer.setRole(UserRole.ORGANIZER);
            Event event=new Event();
            event.setId(1L);
            event.setOrganizer(organizer);
            event.setStartTime(LocalDateTime.now().minusDays(5));
            when(helpForService.idCheck(any(),any(),any())).thenReturn(event);

            assertThatThrownBy(()->eventService.publish(1L)).isInstanceOf(IllegalStateException.class).hasMessage("An expired event cannot be published");
        }
        @Test
        void publish_NoDraftEvent_ThrowsException(){
            User organizer=new User();
            organizer.setId(1L);
            organizer.setRole(UserRole.ORGANIZER);
            Event event=new Event();
            event.setId(1L);
            event.setOrganizer(organizer);
            event.setStartTime(LocalDateTime.now().plusDays(5));
            event.setStatus(EventStatus.PUBLISHED);
            when(helpForService.idCheck(any(),any(),any())).thenReturn(event);

            assertThatThrownBy(()->eventService.publish(1L)).isInstanceOf(IllegalStateException.class).hasMessage("Event is already published");

        }
        @Test
        void publish_ValidRequest_ReturnsEventResponse(){
            User organizer=new User();
            organizer.setId(1L);
            organizer.setRole(UserRole.ORGANIZER);
            Event event=new Event();
            event.setId(1L);
            event.setOrganizer(organizer);
            event.setStartTime(LocalDateTime.now().plusDays(5));
            event.setStatus(EventStatus.DRAFT);
            when(helpForService.idCheck(any(),any(),any())).thenReturn(event);
            when(eventRepository.save(any())).thenReturn(event);

            assertThat(eventService.publish(1L)).isInstanceOf(EventResponse.class).isEqualTo(eventService.toResponse(event));

        }
    }

    @Nested
    @DisplayName("Тестирование метода отмены события")
    class CancelTest{

    }

    @Nested
    @DisplayName("Тестирование метода завершения события")
    class CompleteTest{
        @Test
        void publish_NotFoundEvent_ThrowsException(){
            when(helpForService.idCheck(any(),any(),any())).thenThrow(new EntityNotFoundException());

            assertThatThrownBy(()->eventService.complete(1L)).isInstanceOf(EntityNotFoundException.class);
        }
        @Test
        void publish_NotEnded_ThrowsException(){
            User organizer=new User();
            organizer.setId(1L);
            organizer.setRole(UserRole.ORGANIZER);
            Event event=new Event();
            event.setId(1L);
            event.setOrganizer(organizer);
            event.setEndTime(LocalDateTime.now().plusDays(5));
            when(helpForService.idCheck(any(),any(),any())).thenReturn(event);

            assertThatThrownBy(()->eventService.complete(1L)).isInstanceOf(IllegalStateException.class).hasMessage("Events that have not been held cannot be completed");
        }
        @Test
        void publish_NoPublishedEvent_ThrowsException(){
            User organizer=new User();
            organizer.setId(1L);
            organizer.setRole(UserRole.ORGANIZER);
            Event event=new Event();
            event.setId(1L);
            event.setOrganizer(organizer);
            event.setEndTime(LocalDateTime.now().minusDays(5));
            event.setStatus(EventStatus.DRAFT);
            when(helpForService.idCheck(any(),any(),any())).thenReturn(event);

            assertThatThrownBy(()->eventService.complete(1L)).isInstanceOf(IllegalStateException.class).hasMessage("Only published events can be completed");

        }
        @Test
        void publish_ValidRequest_ReturnsEventResponse(){
            User organizer=new User();
            organizer.setId(1L);
            organizer.setRole(UserRole.ORGANIZER);
            Event event=new Event();
            event.setId(1L);
            event.setOrganizer(organizer);
            event.setEndTime(LocalDateTime.now().minusDays(5));
            event.setStatus(EventStatus.PUBLISHED);
            when(helpForService.idCheck(any(),any(),any())).thenReturn(event);
            when(eventRepository.save(any())).thenReturn(event);

            assertThat(eventService.complete(1L)).isInstanceOf(EventResponse.class).isEqualTo(eventService.toResponse(event));

        }

    }

    @Nested
    @DisplayName("Тестирование метода удаления события")
    class DeleteTest{
        @Test
        void delete_NotFoundEvent_ThrowsException(){
            when(helpForService.idCheck(any(),any(),any())).thenThrow(new EntityNotFoundException());

            assertThatThrownBy(()->eventService.delete(1L)).isInstanceOf(EntityNotFoundException.class);
        }
        @Test
        void delete_NoValidStatus_ThrowsException(){
            User organizer=new User();
            organizer.setId(1L);
            organizer.setRole(UserRole.ORGANIZER);
            Event event=new Event();
            event.setId(1L);
            event.setOrganizer(organizer);
            event.setStatus(EventStatus.PUBLISHED);
            when(helpForService.idCheck(any(),any(),any())).thenReturn(event);

            assertThatThrownBy(()->eventService.delete(1L)).isInstanceOf(IllegalStateException.class).hasMessage("The event status does not allow it to be deleted");

        }
        @Test
        void delete_ValidData_SuccessfullyDeleted(){
            User organizer=new User();
            organizer.setId(1L);
            organizer.setRole(UserRole.ORGANIZER);
            Event event=new Event();
            event.setId(1L);
            event.setOrganizer(organizer);
            event.setStatus(EventStatus.CANCELLED);
            when(helpForService.idCheck(any(),any(),any())).thenReturn(event);

            eventService.delete(1L);

            verify(eventRepository).delete(any());
        }
    }
}
