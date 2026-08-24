package com.bantads.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin
@RestController()
public class HealthController {

    @GetMapping("/health")
    ResponseEntity getHealth() {
        return ResponseEntity.ok().build();
    }
}
