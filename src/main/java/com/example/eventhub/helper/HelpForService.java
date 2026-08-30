package com.example.eventhub.helper;

import com.example.eventhub.booking.BookingRepository;
import com.example.eventhub.event.Event;
import com.example.eventhub.event.EventRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component(value = "helpForService")
@RequiredArgsConstructor
public class HelpForService {

    private static final List<String> ALLOWED_FIELDS_FOR_SORT=List.of("id","title","category","venue","startTime","endTime","capacity","price","status","organizer.id");


    private final BookingRepository bookingRepository;
    private final EventRepository eventRepository;

    @Transactional
    public <T>T idCheck(Long id, JpaRepository<T,Long> repository, String entity){
        if (id<=0){
            throw new IllegalArgumentException("Id can not be <=0");
        }
        return repository.findById(id).orElseThrow(()-> new EntityNotFoundException(entity+" with id: "+id+" not found"));
    }

    @Transactional
    public int calculateAvailableSeats(Event event){
        int capacity=event.getCapacity();
        int occupied=bookingRepository.countOccupiedSeatByEventId(event.getId());
        return capacity-occupied;

    }

    public void isCorrectSort(Pageable pageable){
        for (Sort.Order order:pageable.getSort() ){
            if (!ALLOWED_FIELDS_FOR_SORT.contains(order.getProperty())){
                throw new IllegalArgumentException("Invalid sort field");
            }
        }
    }


    public boolean isEventOwner(Long eventId, Long userId){
        return eventRepository.findById(eventId).orElseThrow(()->new EntityNotFoundException("Event not found")).getOrganizer().getId().equals(userId);
    }

    public boolean isBookingOwner(Long bookingId,Long userId){
        return bookingRepository.findById(bookingId).orElseThrow(()->new EntityNotFoundException("Event not found")).getUser().getId().equals(userId);

    }
}
