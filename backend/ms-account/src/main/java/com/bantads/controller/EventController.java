package com.bantads.controller;

import com.bantads.dto.event.CreateEventDTO;
import com.bantads.dto.event.ReturnTransferDTO;
import com.bantads.entity.event.Event;
import com.bantads.service.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@CrossOrigin
@RestController
@RequestMapping("/events")
public class EventController {

    @Autowired
    private EventService eventService;

    @PostMapping("/{objectId}/deposit")
    ResponseEntity deposit(@RequestHeader("x-user-cpf") String userCPF, @PathVariable String objectId, @RequestBody CreateEventDTO event) {
        try{
            this.eventService.deposit(userCPF, event, objectId);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @PostMapping("/{objectId}/withdraw")
    ResponseEntity withdraw(@RequestHeader("x-user-cpf") String userCPF, @PathVariable String objectId, @RequestBody CreateEventDTO event) {
        try {
            this.eventService.withdraw(userCPF, event, objectId);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @PostMapping("/{objectId}/updateManager")
    ResponseEntity changeManager(@PathVariable String objectId, @RequestBody CreateEventDTO event) {
        try {
            this.eventService.updateManager(event, objectId);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (Exception e) {
            e.printStackTrace(); 
            throw e;
        }
    }

    @PostMapping("/{objectId}/transfer")
    ResponseEntity<Map<String, ReturnTransferDTO>> transfer(@RequestHeader("x-user-cpf") String userCPF, @PathVariable String objectId, @RequestBody CreateEventDTO event) {
        try {
            ReturnTransferDTO transferDTO = this.eventService.transfer(userCPF, objectId, event);

            Map<String, ReturnTransferDTO> destino = new HashMap<>();
            destino.put("destino", transferDTO);

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(destino);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @PostMapping
    Event createAccount(@RequestHeader("x-user-cpf") String userCPF, @RequestBody CreateEventDTO event) {
        try {
            return this.eventService.createAccount(event );
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}
