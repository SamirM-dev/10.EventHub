package com.example.eventhub.event;

import com.example.eventhub.booking.BookingRepository;
import com.example.eventhub.enums.EventStatus;
import com.example.eventhub.event.dto.EventCreateRequest;
import com.example.eventhub.event.dto.EventResponse;
import com.example.eventhub.event.dto.EventUpdateRequest;
import com.example.eventhub.helper.HelpForService;
import com.example.eventhub.user.User;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

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

    public EventResponse update(Long id,EventUpdateRequest request){
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
        event.setStartTime(request.startTime());
        event.setEndTime(request.endTime());
        event.setCapacity(request.capacity());
        event.setPrice(request.price());
        event.setStatus(request.status());

        return toResponse(eventRepository.save(event));
    }

    public EventResponse toResponse(Event event){
        return new EventResponse(event.getId(), event.getTitle(), event.getDescription(), event.getCategory(), event.getVenue(),
                event.getStartTime(),event.getEndTime(), event.getCapacity(), helpForService.calculateAvailableSeats(event),event.getPrice(),
                event.getStatus(),event.getOrganizer().getId(),event.getCreatedAt());
    }

}
