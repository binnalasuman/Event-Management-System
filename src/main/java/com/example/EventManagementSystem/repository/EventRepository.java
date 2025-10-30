package com.suman.eventmanagement.repository;

import com.suman.eventmanagement.entity.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface EventRepository extends JpaRepository<Event, Long> {
    Page<Event> findByTitleContainingIgnoreCase(String title, Pageable pageable);
    Page<Event> findByLocationContainingIgnoreCase(String location, Pageable pageable);
    Page<Event> findByTitleContainingIgnoreCaseAndLocationContainingIgnoreCase(
            String title, String location, Pageable pageable);
    Page<Event> findByEventDateBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);
    Page<Event> findByTitleContainingIgnoreCaseAndLocationContainingIgnoreCaseAndEventDateBetween(
            String title, String location, LocalDateTime start, LocalDateTime end, Pageable pageable
    );
}