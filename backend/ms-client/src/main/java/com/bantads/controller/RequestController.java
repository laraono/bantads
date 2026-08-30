package com.bantads.controller;


import com.bantads.entity.Request;
import com.bantads.service.RequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController("/requests")
public class RequestController {

    @Autowired
    private RequestService requestService;

    @PostMapping()
    Request createRequest() {
        return requestService.createRequest();
    }

    @PostMapping("/{id}/approve")
    void approveRequest(@PathVariable Long id) {
        requestService.approveRequest(id);
    }

    @PostMapping("/{id}/cancel")
    void getClient(@PathVariable Long id, @RequestBody String rejectionReason) {
        requestService.rejectRequest(id, rejectionReason);
    }
}
