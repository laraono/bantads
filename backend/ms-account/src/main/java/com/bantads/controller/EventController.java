package com.bantads.controller;

import com.bantads.dto.event.CreateEventDTO;
import com.bantads.dto.event.TransferDTO;
import com.bantads.entity.event.Event;
import com.bantads.service.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController("/events")
public class EventController {

    @Autowired
    private EventService eventService;

    @PostMapping("/{objectId}")
    Event createEvent(@PathVariable String objectId, @RequestBody CreateEventDTO event) {
        return this.eventService.createEvent(event, objectId);
    }

    @PostMapping("/{objectId}/transfer")
    List<Event> transfer(@PathVariable String objectId, @RequestBody TransferDTO event) {
        return this.eventService.transfer(objectId, event);
    }

    @PostMapping
    Event createAccount(@RequestBody CreateEventDTO event) {
        return this.eventService.createAccount(event);
    }
}
