package com.example.eventhub.booking;

import com.example.eventhub.enums.BookingStatus;
import com.example.eventhub.enums.EventStatus;
import com.example.eventhub.event.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking,Long> {

    @Query("SELECT COALESCE(SUM(b.quantity),0) FROM Booking  b WHERE b.event.id=:id AND b.status='CONFIRMED'")
    int countOccupiedSeatByEventId(@Param("id") Long id);
    boolean existsByEvent_IdAndUser_IdAndStatus(Long eventId, Long userId, BookingStatus status);
    List<Booking> findByUserId(Long id);
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Booking b SET b.status='CANCELLED' WHERE b.event.id=:eventId AND b.status='CONFIRMED'")
    void updateBookingsToCancel(@Param("eventId") Long eventId);
}
