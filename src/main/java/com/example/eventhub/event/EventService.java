package com.example.eventhub.event;

import com.example.eventhub.auth.details.UserPrincipal;
import com.example.eventhub.enums.EventCategory;
import com.example.eventhub.enums.EventStatus;
import com.example.eventhub.enums.UserRole;
import com.example.eventhub.event.dto.EventCreateRequest;
import com.example.eventhub.event.dto.EventResponse;
import com.example.eventhub.event.dto.EventUpdateRequest;
import com.example.eventhub.helper.HelpForService;
import com.example.eventhub.user.User;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final HelpForService helpForService;

    public EventResponse create(EventCreateRequest request, User user){
        return toResponse(eventRepository.save(new Event(
                request.title(), request.description(), request.category(), request.venue(), request.startTime(),
                request.endTime(),request.capacity(),request.price(),user)
        ));
    }

    @Transactional
    public EventResponse update(Long id, EventUpdateRequest request){
        Event event =eventRepository.findById(id).orElseThrow(()->new EntityNotFoundException("Event with id:"+id+" not found"));
        int occupied =event.getCapacity()- helpForService.calculateAvailableSeats(event);
        if (request.capacity()<occupied){
            throw new IllegalArgumentException("The new capacity is insufficient for the places already taken");
        }
        if (request.startTime().isBefore(LocalDateTime.now())||request.endTime().isBefore(LocalDateTime.now())){
            throw new IllegalArgumentException("Incorrect date data");
        }
        if (request.status().equals(EventStatus.COMPLETED)){
            throw new IllegalArgumentException("You do not have permission to complete event");
        }
        event.setTitle(request.title());
        event.setDescription(request.description());
        event.setCategory(request.category());
        event.setVenue(request.venue());
        event.setStartTime(request.startTime());
        event.setEndTime(request.endTime());
        event.setCapacity(request.capacity());
        event.setPrice(request.price());
        event.setStatus(request.status());

        return toResponse(eventRepository.save(event));
    }

    public List<EventResponse> getAll(String categoryFromUrl, Pageable pageable){
        helpForService.isCorrectSort(pageable);
        EventCategory category = categoryFromUrl!=null?EventCategory.valueOf(categoryFromUrl):null;
        List<Event> events=eventRepository.getAllWithPaginationAndFilter(category,EventStatus.PUBLISHED,pageable).getContent();
        return events.stream().map(this::toResponse).toList();
    }

    public EventResponse getById(UserPrincipal principal,Long id){
        Event found=helpForService.idCheck(id,eventRepository,"Event");
        boolean enoughPermission = principal==null?false:List.of(UserRole.ORGANIZER,UserRole.ADMIN).contains(principal.getUser().getRole());
        if (!found.getStatus().equals(EventStatus.PUBLISHED)&&!enoughPermission){
            throw new EntityNotFoundException("Event with id:"+id+" not found");
        }
        return toResponse(found);
    }

    @Transactional
    public EventResponse publish(Long id){
        Event found = helpForService.idCheck(id,eventRepository,"Event");
        if (found.getStartTime().isBefore(LocalDateTime.now())){
            throw new IllegalStateException("An expired event cannot be published");
        }
        if (!found.getStatus().equals(EventStatus.DRAFT)){
            throw new IllegalStateException("Event is already published");
        }
        found.setStatus(EventStatus.PUBLISHED);
        return toResponse(eventRepository.save(found));
    }

    //Доделать !!!
    @Transactional
    public EventResponse cancel(Long id){
        Event found = helpForService.idCheck(id,eventRepository,"Event");
        if (found.getStartTime().isBefore(LocalDateTime.now())){
            throw new IllegalStateException("An expired event cannot be cancelled");
        }
        if (!found.getStatus().equals(EventStatus.PUBLISHED)){
            throw new IllegalStateException("Only published events can be cancelled");
        }

        //ОТМЕНИТЬ У ВСЕХ БРОНИ
        found.setStatus(EventStatus.CANCELLED);
        return toResponse(eventRepository.save(found));
    }

    @Transactional
    public EventResponse complete(Long id){
        Event found = helpForService.idCheck(id,eventRepository,"Event");
        if (!found.getEndTime().isBefore(LocalDateTime.now())){
            throw new IllegalStateException("Events that have not been held cannot be completed");
        }
        if (!found.getStatus().equals(EventStatus.PUBLISHED)){
            throw new IllegalStateException("Only published events can be completed");
        }
        found.setStatus(EventStatus.COMPLETED);
        return toResponse(eventRepository.save(found));
    }

    @Transactional
    public void delete(Long id){
        Event found = helpForService.idCheck(id,eventRepository,"Event");
        if (!List.of(EventStatus.DRAFT,EventStatus.CANCELLED).contains(found.getStatus())){
            throw new IllegalStateException("The event status does not allow it to be deleted");
        }

        eventRepository.delete(found);
    }

    public EventResponse toResponse(Event event){
        return new EventResponse(event.getId(), event.getTitle(), event.getDescription(), event.getCategory(), event.getVenue(),
                event.getStartTime(),event.getEndTime(), event.getCapacity(), helpForService.calculateAvailableSeats(event),event.getPrice(),
                event.getStatus(),event.getOrganizer().getId(),event.getCreatedAt());
    }



}
