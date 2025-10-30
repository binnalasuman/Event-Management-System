package com.suman.eventmanagement.controller;

import com.suman.eventmanagement.entity.Booking;
import com.suman.eventmanagement.entity.Event;
import com.suman.eventmanagement.repository.BookingRepository;
import com.suman.eventmanagement.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.security.Principal;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/events")
@CrossOrigin(origins = "http://localhost:59704")
public class EventController {

    private final EventService eventService;
    private final BookingRepository bookingRepository ;

    public EventController(EventService eventService, BookingRepository bookingRepository) {
        this.eventService = eventService;
        this.bookingRepository = bookingRepository;
    }


    @GetMapping
    public ResponseEntity<?> getEvents(
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

        LocalDateTime start = null;
        LocalDateTime end = null;

        try {
            start = hasStart ? LocalDateTime.parse(startDate) : null;
            end = hasEnd ? LocalDateTime.parse(endDate) : null;
        } catch (DateTimeParseException ex) {
            return ResponseEntity.badRequest().body("Invalid date format. Please use ISO_LOCAL_DATE_TIME format: yyyy-MM-ddTHH:mm:ss");
        }

        if (!hasTitle && !hasLocation && !hasStart && !hasEnd) {
            return ResponseEntity.ok(eventService.getAllEvents(pageRequest));
        }

        return ResponseEntity.ok(eventService.searchEvents(
                hasTitle ? title : null,
                hasLocation ? location : null,
                start,
                end,
                pageRequest
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Event> getEventById(@PathVariable Long id) {
        Event event = eventService.getEventById(id);
        return event == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(event);
    }

    @PostMapping
    public ResponseEntity<Event> createEvent(@Valid @RequestBody Event event) {
        event.setId(null);
        Event saved = eventService.createEvent(event);
        return ResponseEntity.created(URI.create("/api/events/" + saved.getId())).body(saved);
    }
    @PostMapping("/{eventId}/book")
    public ResponseEntity<?> bookSeats(
            @PathVariable Long eventId,
            @RequestBody Map<String, Integer> payload,
            Principal principal) {
        Integer seats = payload.get("seats");
        List<String> passes = new ArrayList<>();
        for (int i = 0; i < seats; i++) {
            String passCode = "PASS-" + UUID.randomUUID();
            Booking booking = new Booking();
            booking.setEventId(eventId);
            booking.setUser(principal.getName());
            booking.setPass(passCode);
            bookingRepository.save(booking);
            passes.add(passCode);
        }
        return ResponseEntity.ok(Map.of("passes", passes));
    }


    @GetMapping("/{eventId}/bookings")
    public List<Map<String, String>> getBookings(@PathVariable Long eventId) {
        return bookingRepository.findByEventId(eventId).stream()
                .map(b -> Map.of("user", b.getUser(), "pass", b.getPass()))
                .collect(Collectors.toList());
    }




    @PutMapping("/{id}")
    public ResponseEntity<Event> updateEvent(@PathVariable Long id, @Valid @RequestBody Event eventDetails) {
        Event updatedEvent = eventService.updateEvent(id, eventDetails);
        return updatedEvent == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(updatedEvent);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id) {
        boolean deleted = eventService.deleteEvent(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
