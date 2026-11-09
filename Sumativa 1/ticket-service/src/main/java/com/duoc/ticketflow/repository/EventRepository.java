package com.duoc.ticketflow.repository;

import com.duoc.ticketflow.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    Optional<Event> findByEventCode(String eventCode);
    boolean existsByEventCode(String eventCode);
}
