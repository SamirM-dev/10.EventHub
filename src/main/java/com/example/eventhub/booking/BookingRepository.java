package com.example.eventhub.booking;

import com.example.eventhub.event.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BookingRepository extends JpaRepository<Event,Long> {

    @Query(nativeQuery = true,value = "SELECT COALESCE(SUM(quantity),0) FROM bookings WHERE event_id=:id")
    int countOccupiedSeatByEventId(@Param("id") Long id);
}
