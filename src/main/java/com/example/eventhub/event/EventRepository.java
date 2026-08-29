package com.example.eventhub.event;

import com.example.eventhub.enums.EventCategory;
import com.example.eventhub.enums.EventStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface EventRepository extends JpaRepository<Event,Long> {

    @Query("SELECT e FROM Event e WHERE (e.category=:category OR :category=null) AND e.status=:status")
    Page<Event> getAllWithPaginationAndFilter(@Param("category")EventCategory category, @Param("status") EventStatus status, Pageable pageable);
}
