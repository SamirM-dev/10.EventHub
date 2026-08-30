package com.example.eventhub.booking;

import com.example.eventhub.booking.dto.BookingCreateRequest;
import com.example.eventhub.booking.dto.BookingResponse;
import com.example.eventhub.enums.BookingStatus;
import com.example.eventhub.enums.EventStatus;
import com.example.eventhub.event.Event;
import com.example.eventhub.event.EventRepository;
import com.example.eventhub.helper.HelpForService;
import com.example.eventhub.user.User;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final HelpForService helpForService;
    private final EventRepository eventRepository;

    public List<BookingResponse> getMyBookings(Long userId){
        return bookingRepository.findByUserId(userId).stream().map(this::toResponse).toList();
    }

    @Transactional
    public BookingResponse create(User user, Long eventId, BookingCreateRequest request){
        Event event = helpForService.idCheck(eventId,eventRepository,"Event");
        if (!event.getStatus().equals(EventStatus.PUBLISHED)){
            throw new IllegalStateException("You cannot make a reservation for this event");
        }
        if (request.quantity()>helpForService.calculateAvailableSeats(event)){
            throw new IllegalArgumentException("Not enough available seats for booking");
        }
        if (event.getEndTime().isBefore(LocalDateTime.now())){
            throw new IllegalStateException("The event has already concluded");
        }
        BigDecimal totalPrice = event.getPrice().multiply(BigDecimal.valueOf(request.quantity()));
        Booking booking = new Booking(event,user,request.quantity(), totalPrice);
        return toResponse(bookingRepository.save(booking));

    }

    @Transactional
    public BookingResponse cancel(Long id){
        Booking booking = helpForService.idCheck(id,bookingRepository,"Booking");
        if (booking.getEvent().getStartTime().isBefore(LocalDateTime.now())){
            throw new IllegalStateException("You cannot cancel a reservation for an event that has already started");
        }
        booking.setStatus(BookingStatus.CANCELLED);
        return toResponse(bookingRepository.save(booking));
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getBookingsByEvent(Long eventId){
        Event event = helpForService.idCheck(eventId,eventRepository,"Event");
        return event.getBookings().stream().map(this::toResponse).toList();
    }


    public BookingResponse toResponse(Booking booking){
        return new BookingResponse(
                booking.getId(),booking.getEvent().getId(),booking.getUser().getId(),booking.getQuantity(),
                booking.getTotalPrice(),booking.getStatus(),booking.getBookedAt()
        );
    }
}
