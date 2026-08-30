package com.example.eventhub.booking;

import com.example.eventhub.event.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking,Long> {

    @Query("SELECT COALESCE(SUM(b.quantity),0) FROM Booking  b WHERE b.event.id=:id AND b.status='CONFIRMED'")
    int countOccupiedSeatByEventId(@Param("id") Long id);
    List<Booking> findByUserId(Long id);
}
