package com.example.EventManagementSystem.controller;


import com.example.EventManagementSystem.entity.Event;
import com.example.EventManagementSystem.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class EventController {

    private final EventService eventService;


    @GetMapping
    public Page<Event> getEvents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "eventDate") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {

        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        PageRequest pageRequest = PageRequest.of(page, size, sort);

        boolean hasTitle = title != null && !title.isBlank();
        boolean hasLocation = location != null && !location.isBlank();
        boolean hasStart = startDate != null && !startDate.isBlank();
        boolean hasEnd = endDate != null && !endDate.isBlank();

        // No filters, return all
        if (!hasTitle && !hasLocation && !hasStart && !hasEnd) {
            return eventService.getAllEvents(pageRequest);
        }

        // Parse ISO date-times like "2025-11-20T09:00:00"
        LocalDateTime start = hasStart ? LocalDateTime.parse(startDate) : null;
        LocalDateTime end = hasEnd ? LocalDateTime.parse(endDate) : null;

        return eventService.searchEvents(
                hasTitle ? title : null,
                hasLocation ? location : null,
                start,
                end,
                pageRequest
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<Event> getEventById(@PathVariable Long id) {
        Event event = eventService.getEventById(id);
        return event == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(event);
    }

    @PostMapping
    public ResponseEntity<Event> createEvent(@RequestBody Event event) {
        // Ensure id is not set for creation
        if (event.getId() != null) {
            event.setId(null);
        }
        Event saved = eventService.createEvent(event);
        return ResponseEntity.created(URI.create("/api/events/" + saved.getId())).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Event> updateEvent(@PathVariable Long id, @RequestBody Event eventDetails) {
        Event updatedEvent = eventService.updateEvent(id, eventDetails);
        return updatedEvent == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(updatedEvent);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id) {
        boolean deleted = eventService.deleteEvent(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}