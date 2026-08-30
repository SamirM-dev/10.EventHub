package com.example.eventhub.review;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review,Long> {

    Page<Review> findByEvent_Id(Long id, Pageable pageable);
    boolean existsByEvent_IdAndUser_Id(Long enetId,Long userId);
}
