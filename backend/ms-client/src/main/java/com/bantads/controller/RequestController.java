package com.bantads.controller;


import com.bantads.assembler.RequestModelAssembler;
import com.bantads.dto.RequestDTO;
import com.bantads.entity.Request;
import com.bantads.service.RequestService;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

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
    EntityModel<Request>  createRequest(@Valid @RequestBody RequestDTO requestDTO) {
        Request newRequest = this.requestService.createRequest(requestDTO);
        return assembler.toModel(newRequest);
    }

    @PostMapping("/{id}/aprovacao")
    @ResponseStatus(HttpStatus.CREATED)
    public
    void approveRequest(@PathVariable Long id) {
        this.requestService.approveRequest(id);
    }   

    @PostMapping("/{id}/rejeicao")
    @ResponseStatus(HttpStatus.CREATED)
    void rejectRequest(@PathVariable Long id) {
        this.requestService.rejectRequest(id, null);
    }

    @GetMapping ("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public
    EntityModel<Request> getRequest(@PathVariable Long id) {
        Request request = this.requestService.getRequestById(id);
        return assembler.toModel(request);
    }

}
