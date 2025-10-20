package com.example.EventManagementSystem.service;

import com.example.EventManagementSystem.entity.Event;
import com.example.EventManagementSystem.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;

    public Page<Event> getAllEvents(Pageable pageable) {
        return eventRepository.findAll(pageable);
    }

    public Page<Event> searchEvents(String title,
                                    String location,
                                    LocalDateTime start,
                                    LocalDateTime end,
                                    Pageable pageable) {

        boolean hasTitle = title != null && !title.isBlank();
        boolean hasLocation = location != null && !location.isBlank();
        boolean hasStart = start != null;
        boolean hasEnd = end != null;

        if (hasTitle && hasLocation && hasStart && hasEnd) {
            return eventRepository
                    .findByTitleContainingIgnoreCaseAndLocationContainingIgnoreCaseAndEventDateBetween(
                            title, location, start, end, pageable);
        }
        if (hasTitle && hasLocation) {
            return eventRepository
                    .findByTitleContainingIgnoreCaseAndLocationContainingIgnoreCase(
                            title, location, pageable);
        }
        if (hasTitle) {
            return eventRepository.findByTitleContainingIgnoreCase(title, pageable);
        }
        if (hasLocation) {
            return eventRepository.findByLocationContainingIgnoreCase(location, pageable);
        }
        if (hasStart && hasEnd) {
            return eventRepository.findByEventDateBetween(start, end, pageable);
        }
        return eventRepository.findAll(pageable);
    }

    public Event getEventById(Long id) {
        return eventRepository.findById(id).orElse(null);
    }

    public Event createEvent(Event event) {
        return eventRepository.save(event);
    }

    public Event updateEvent(Long id, Event details) {
        return eventRepository.findById(id)
                .map(e -> {
                    e.setTitle(details.getTitle());
                    e.setDescription(details.getDescription());
                    e.setLocation(details.getLocation());
                    e.setEventDate(details.getEventDate());
                    e.setAvailableSeats(details.getAvailableSeats());
                    return eventRepository.save(e);
                }).orElse(null);
    }

    public boolean deleteEvent(Long id) {
        return eventRepository.findById(id).map(e -> {
            eventRepository.delete(e);
            return true;
        }).orElse(false);
    }
}
