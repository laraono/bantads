package com.bantads.controller;

import com.bantads.assembler.RequestModelAssembler;
import com.bantads.dto.RejectRequestDTO;
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
    public
    ResponseEntity<EntityModel<Request>> createRequest(@RequestBody RequestDTO requestDTO) {
        try {
            Request newRequest = this.requestService.createRequest(requestDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(assembler.toModel(newRequest));
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @PostMapping("/{cpf}/aprovacao")
    @ResponseStatus(HttpStatus.CREATED)
    public void approveRequest(@PathVariable String cpf) {
        this.requestService.approveRequest(cpf);
    }

    @PostMapping("/{cpf}/rejeicao")
    @ResponseStatus(HttpStatus.OK)
    public EntityModel<Request> rejectRequest(@PathVariable String cpf, @RequestBody RejectRequestDTO dto) {
        Request request = this.requestService.rejectRequest(cpf, dto.getMotivo());
        return assembler.toModel(request);
    }

    @GetMapping()
    @ResponseStatus(HttpStatus.OK)
    public Map<String, List<EntityModel<Request>>> listRequests() {
        List<EntityModel<Request>> requests = this.requestService.listRequests().stream()
                .map(assembler::toModel)
                .toList();
        return Map.of("solicitacoes", requests);
    }

    @GetMapping("/{cpf}")
    @ResponseStatus(HttpStatus.OK)
    public EntityModel<Request> getRequest(@PathVariable String cpf) {
        Request request = this.requestService.getRequestByCpf(cpf);
        return assembler.toModel(request);
    }

}
