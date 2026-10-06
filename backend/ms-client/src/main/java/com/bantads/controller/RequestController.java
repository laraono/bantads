package com.bantads.controller;


import com.bantads.assembler.RequestModelAssembler;
import com.bantads.dto.RequestDTO;
import com.bantads.entity.Request;
import com.bantads.service.RequestService;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin
@RestController
@RequestMapping("/requests")
public class RequestController {

    @Autowired
    private RequestService requestService;

    @Autowired
    RequestModelAssembler assembler;

    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    public
    ResponseEntity<Request> createRequest(@RequestBody RequestDTO requestDTO) {
        try {
            Request newRequest = this.requestService.createRequest(requestDTO);
            return ResponseEntity.ok().body(newRequest);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @GetMapping()
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<List<Request>> listRequests() {
        try {
            List<Request> requests = this.requestService.listRequests();
            return ResponseEntity.ok().body(requests);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @PostMapping("/{id}/aprovacao")
    @ResponseStatus(HttpStatus.CREATED)
    public void approveRequest(@PathVariable String id) {
        this.requestService.approveRequest(id);
    }   

    @PostMapping("/{id}/rejeicao")
    @ResponseStatus(HttpStatus.OK)
    void rejectRequest(@PathVariable Long id, @RequestBody String motivo) {
        this.requestService.rejectRequest(id, motivo);
    }

    @GetMapping ("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public
    ResponseEntity<Request> getRequest(@PathVariable String id) {
        try {
            Request request = this.requestService.getRequestByCpf(id);
            return ResponseEntity.ok().body(request);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

}
