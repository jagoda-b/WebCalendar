package webCalendarSpring.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import webCalendarSpring.db.Event;
import webCalendarSpring.db.EventRepository;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
public class EventController {

    private final EventRepository eventRepository;

    public EventController(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @GetMapping("/event/today")
    public ResponseEntity<List<Event>> getTodayEvents() {
        LocalDate today = LocalDate.now();
        List<Event> events = eventRepository.findAll().stream()
                .filter(event -> event.getDate().isEqual(today))
                .collect(Collectors.toList());
        return new ResponseEntity<>(events, HttpStatus.OK);
    }

    @PostMapping("/event")
    public ResponseEntity<Object> addEvent(@RequestBody Map<String, Object> payload) {
        Optional<String> eventOpt = Optional.ofNullable(payload.get("event")).map(Object::toString);
        Optional<String> dateOpt = Optional.ofNullable(payload.get("date")).map(Object::toString);

        if (payload.size() != 2 || eventOpt.isEmpty() || dateOpt.isEmpty() || eventOpt.get().isBlank() || dateOpt.get().isBlank()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        LocalDate date;
        try {
            date = LocalDate.parse(dateOpt.get());
        } catch (DateTimeParseException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        Event event = new Event();
        event.setEvent(eventOpt.get());
        event.setDate(date);
        eventRepository.save(event);

        Map<String, Object> response = Map.of(
                "message", "The event has been added!",
                "event", eventOpt.get(),
                "date", date.toString()
        );

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/event/{id}")
    public ResponseEntity<Object> getEvent(@PathVariable Integer id) {
        Optional<Event> eventOpt = eventRepository.findById(id);
        if (eventOpt.isEmpty()) {
            return new ResponseEntity<>(Map.of("message", "The event doesn't exist!"), HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(eventOpt.get(), HttpStatus.OK);
    }

    @DeleteMapping("/event/{id}")
    public ResponseEntity<Object> deleteEvent(@PathVariable Integer id) {
        Optional<Event> eventOpt = eventRepository.findById(id);
        if (eventOpt.isEmpty()) {
            return new ResponseEntity<>(Map.of("message", "The event doesn't exist!"), HttpStatus.NOT_FOUND);
        }
        eventRepository.delete(eventOpt.get());
        return new ResponseEntity<>(eventOpt.get(), HttpStatus.OK);
    }

    @GetMapping("/event")
    public ResponseEntity<List<Event>> getEventsInRange(@RequestParam Optional<String> start_time, @RequestParam Optional<String> end_time) {
        LocalDate start = start_time.map(LocalDate::parse).orElse(LocalDate.MIN);
        LocalDate end = end_time.map(LocalDate::parse).orElse(LocalDate.MAX);
        List<Event> events = eventRepository.findAll().stream()
                .filter(event -> !event.getDate().isBefore(start) && !event.getDate().isAfter(end))
                .collect(Collectors.toList());
        if (events.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(events, HttpStatus.OK);
    }


}

