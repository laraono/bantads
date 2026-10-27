package com.bantads.controller;

import com.bantads.assembler.RequestModelAssembler;
import com.bantads.dto.RejectRequestDTO;
import com.bantads.dto.RequestDTO;
import com.bantads.dto.RequestResponseDTO;
import com.bantads.entity.Request;
import com.bantads.entity.RequestStatus;
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
    public EntityModel<RequestResponseDTO> createRequest(@Valid @RequestBody RequestDTO requestDTO) {
        try {
            Request request = this.requestService.createRequest(requestDTO);
            RequestResponseDTO response = RequestResponseDTO.from(request);
            return assembler.toModel(response);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @PostMapping("/{cpf}/aprovacao")
    @ResponseStatus(HttpStatus.CREATED)
    public EntityModel<RequestResponseDTO> approveRequest(@PathVariable String cpf) {
        Request request = this.requestService.approveRequest(cpf);
        RequestResponseDTO response = RequestResponseDTO.from(request);
            return assembler.toModel(response);
    }

    @PostMapping("/{cpf}/reject")
    @ResponseStatus(HttpStatus.OK)
    public EntityModel<RequestResponseDTO> rejectRequest(@PathVariable String cpf, @Valid @RequestBody RejectRequestDTO dto) {
        Request request = this.requestService.rejectRequest(cpf, dto.getMotivo());
        RequestResponseDTO response = RequestResponseDTO.from(request);
            return assembler.toModel(response);
    }

    @GetMapping()
    @ResponseStatus(HttpStatus.OK)
    public Map<String, List<EntityModel<RequestResponseDTO>>> listRequests() {
        List<EntityModel<RequestResponseDTO>> requests = this.requestService.listRequests().stream()
                .map((r) -> RequestResponseDTO.from(r)).map(assembler::toModel)
                .toList();

        return Map.of("solicitacoes", requests);
    }

    @GetMapping("/{cpf}")
    @ResponseStatus(HttpStatus.OK)
    public EntityModel<RequestResponseDTO> getRequest(@PathVariable String cpf) {
        Request request = this.requestService.getRequestByCpf(cpf);
        RequestResponseDTO response = RequestResponseDTO.from(request);
        return assembler.toModel(response);
    }

}
