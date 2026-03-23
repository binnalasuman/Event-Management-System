package com.suman.eventmanagement.controller;

import com.suman.eventmanagement.entity.Booking;
import com.suman.eventmanagement.entity.Event;
import com.suman.eventmanagement.repository.BookingRepository;
import com.suman.eventmanagement.repository.EventRepository;
import com.suman.eventmanagement.service.EmailService;
import com.suman.eventmanagement.service.EventService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.nio.file.*;
import java.security.Principal;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;

@RestController
@RequestMapping("/api/events")
@CrossOrigin(origins = "http://localhost:4200")

public class EventController {

    private final EventService eventService;
    private final BookingRepository bookingRepository;
    private final EventRepository eventRepository;
    private final EmailService emailService;
    private final String uploadDir = "uploads";
    public EventController(EventService eventService,
                           BookingRepository bookingRepository,
                           EventRepository eventRepository,
                           EmailService emailService) {

        this.eventService = eventService;
        this.bookingRepository = bookingRepository;
        this.eventRepository = eventRepository;
        this.emailService = emailService;
    }

    // ================= GET EVENTS =================

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

        LocalDateTime start = null;
        LocalDateTime end = null;

        try {
            if (startDate != null && !startDate.isBlank())
                start = LocalDateTime.parse(startDate);

            if (endDate != null && !endDate.isBlank())
                end = LocalDateTime.parse(endDate);

        } catch (DateTimeParseException ex) {
            return ResponseEntity.badRequest()
                    .body("Invalid date format. Use yyyy-MM-ddTHH:mm:ss");
        }

        return ResponseEntity.ok(
                eventService.searchEvents(title, location, start, end, pageRequest)
        );
    }

    // ================= GET EVENT =================

    @GetMapping("/{id}")
    public ResponseEntity<Event> getEventById(@PathVariable Long id) {

        Event event = eventService.getEventById(id);

        if (event == null)
            return ResponseEntity.notFound().build();

        return ResponseEntity.ok(event);
    }

    // ================= CREATE EVENT =================

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<?> createEvent(
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam String location,
            @RequestParam String eventDate,
            @RequestParam int availableSeats,
            @RequestParam(required = false) MultipartFile image) throws Exception {

        Event event = new Event();

        event.setTitle(title);
        event.setDescription(description);
        event.setLocation(location);
        event.setEventDate(LocalDateTime.parse(eventDate));
        event.setAvailableSeats(availableSeats);

        if (image != null && !image.isEmpty()) {

            Path uploadPath = Paths.get(uploadDir).toAbsolutePath();

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String fileName = UUID.randomUUID() + "_" + image.getOriginalFilename();

            Path filePath = uploadPath.resolve(fileName);

            Files.copy(image.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            event.setImageUrl("/uploads/" + fileName);
        }

        Event saved = eventService.createEvent(event);

        return ResponseEntity
                .created(URI.create("/api/events/" + saved.getId()))
                .body(saved);
    }

    // ================= BOOK SEATS =================

    @Transactional
    @PostMapping("/{eventId}/book")
    public ResponseEntity<?> bookSeats(
            @PathVariable Long eventId,
            @RequestBody Map<String, Integer> payload,
            Principal principal) {

        Integer seats = payload.get("seats");

        if (seats == null || seats <= 0) {
            return ResponseEntity.badRequest().body("Invalid seat count");
        }

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        if (event.getEventDate().isBefore(LocalDateTime.now())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Event has completed"));
        }

        if (event.getAvailableSeats() < seats) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Seats are sold out"));
        }

        event.setAvailableSeats(event.getAvailableSeats() - seats);
        eventRepository.save(event);

        List<String> passes = new ArrayList<>();

        for (int i = 0; i < seats; i++) {

            String passCode = "PASS-" + UUID.randomUUID();

            Booking booking = new Booking();
            booking.setEventId(event.getId());
            booking.setUser(principal.getName());
            booking.setPass(passCode);

            bookingRepository.save(booking);

            passes.add(passCode);

        }  emailService.sendBookingConfirmation(principal.getName(), event.getTitle(), passes);

        return ResponseEntity.ok(Map.of("passes", passes));
    }
    //verify
    @GetMapping("/verify/{pass}")
    public ResponseEntity<?> verifyTicket(@PathVariable String pass) {

        Booking booking = bookingRepository.findByPass(pass);

        Map<String, Object> response = new HashMap<>();

        // Ticket not found
        if (booking == null) {
            response.put("status", "INVALID");
            response.put("message", "Ticket not found");
            return ResponseEntity.badRequest().body(response);
        }

        // Ticket already used
        if (booking.isUsed()) {
            response.put("status", "USED");
            response.put("message", "Ticket already used");
            response.put("pass", booking.getPass());
            response.put("user", booking.getUser());
            return ResponseEntity.ok(response);
        }



        // Get event
        Event event = eventRepository.findById(booking.getEventId()).orElse(null);

        // Mark ticket as used
        booking.setUsed(true);
        bookingRepository.save(booking);

        response.put("status", "VALID");
        response.put("event", event != null ? event.getTitle() : "Unknown");
        response.put("user", booking.getUser());
        response.put("pass", booking.getPass());
        response.put("message", "Entry Allowed");

        return ResponseEntity.ok(response);
    }
    // ================= USER BOOKINGS =================

    @GetMapping("/bookings/my")
    public List<Map<String, Object>> getMyBookings(Principal principal) {

        String username = principal.getName();

        return bookingRepository.findByUser(username).stream()
                .map(b -> {

                    Event event = eventRepository.findById(b.getEventId()).orElse(null);

                    Map<String, Object> map = new HashMap<>();

                    map.put("id", b.getId());
                    map.put("pass", b.getPass());
                    map.put("eventId", b.getEventId());
                    map.put("title", event != null ? event.getTitle() : "Unknown");
                    map.put("location", event != null ? event.getLocation() : "Unknown");
                    map.put("eventDate", event != null ? event.getEventDate().toString() : "");

                    return map;
                })
                .toList();
    }

    // ================= EVENT BOOKINGS =================

    @GetMapping("/{eventId}/bookings")
    public List<Map<String, String>> getBookings(@PathVariable Long eventId) {

        return bookingRepository.findByEventId(eventId).stream()
                .map(b -> Map.of(
                        "user", b.getUser(),
                        "pass", b.getPass()
                ))
                .toList();
    }



    @PutMapping(value = "/{id}", consumes = "multipart/form-data")
    public ResponseEntity<?> updateEvent(
            @PathVariable Long id,
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam String location,
            @RequestParam String eventDate,
            @RequestParam int availableSeats,
            @RequestParam(required = false) MultipartFile image) throws Exception {

        Event event = eventService.getEventById(id);

        if (event == null)
            return ResponseEntity.notFound().build();

        event.setTitle(title);
        event.setDescription(description);
        event.setLocation(location);
        event.setEventDate(LocalDateTime.parse(eventDate));
        event.setAvailableSeats(availableSeats);

        if (image != null && !image.isEmpty()) {

            Path uploadPath = Paths.get(uploadDir).toAbsolutePath();

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String fileName = UUID.randomUUID() + "_" + image.getOriginalFilename();

            Path filePath = uploadPath.resolve(fileName);

            Files.copy(image.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            event.setImageUrl("/uploads/" + fileName);
        }

        Event updated = eventService.updateEvent(id, event);

        return ResponseEntity.ok(updated);
    }



    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id) {

        boolean deleted = eventService.deleteEvent(id);

        if (deleted)
            return ResponseEntity.noContent().build();

        return ResponseEntity.notFound().build();
    }
}
