package com.bantads.controller;

import com.bantads.dto.event.CreateEventDTO;
import com.bantads.dto.event.ReturnTransferDTO;
import com.bantads.dto.event.TransferDTO;
import com.bantads.entity.event.Event;
import com.bantads.service.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController("/events")
public class EventController {

    @Autowired
    private EventService eventService;

    @PostMapping("/{objectId}/deposit")
    ResponseEntity deposit(@RequestHeader("X-User-CPF") String userCPF, @PathVariable String objectId, @RequestBody CreateEventDTO event) {
        this.eventService.deposit(userCPF, event, objectId);

        return ResponseEntity.status(HttpStatus.CREATED).body("");
    }

    @PostMapping("/{objectId}/withdraw")
    void withdraw(@RequestHeader("X-User-CPF") String userCPF, @PathVariable String objectId, @RequestBody CreateEventDTO event) {
        this.eventService.withdraw(userCPF, event, objectId);
    }

    @PostMapping("/{objectId}/updateManager")
    void changeManager(@PathVariable String objectId, @RequestBody CreateEventDTO event) {
        this.eventService.updateManager(event, objectId);
    }

    @PostMapping("/{objectId}/transfer")
    ResponseEntity<ReturnTransferDTO> transfer(@RequestHeader("X-User-CPF") String userCPF, @PathVariable String objectId, @RequestBody TransferDTO event) {
        ReturnTransferDTO destino = this.eventService.transfer(userCPF, objectId, event);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(destino);
    }

    @PostMapping
    Event createAccount(@RequestHeader("X-User-CPF") String userCPF, @RequestBody CreateEventDTO event) {
        return this.eventService.createAccount(event );
    }
}
