package com.bantads.controller;

import com.bantads.assembler.ClientModelAssembler;
import com.bantads.dto.ClientResponseDTO;
import com.bantads.entity.Request;
import com.bantads.service.ClientService;
import com.bantads.entity.Client;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@RequestMapping("/clients")
public class ClientController {
    
    @Autowired
    private ClientService clientService;

    @Autowired 
    private ClientModelAssembler assembler;

    @GetMapping
    public Map<String, List<EntityModel<ClientResponseDTO>>> listClients(
            @RequestParam(value = "busca", required = false) String busca) {
        List<EntityModel<ClientResponseDTO>> clients = clientService.listClients(busca).stream()
                .map(ClientResponseDTO::from)
                .map(assembler::toModel)
                .toList();
        return Map.of("clientes", clients);
    }

    @GetMapping("/{cpf}")
    public EntityModel<ClientResponseDTO> getClient(@PathVariable String cpf,
            @RequestHeader("x-user-tipo") String tipo) {
        Client client = clientService.getClient(cpf);
        return assembler.toModel(ClientResponseDTO.from(client), tipo);
    }

    @GetMapping("/{id}/requests")
    public
    Request getRequest(@PathVariable Long id) {
        return clientService.getRequestByClient(id);
    }
}
