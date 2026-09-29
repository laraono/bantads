package com.bantads.controller;

import com.bantads.assembler.ClientModelAssembler;
import com.bantads.entity.Request;
import com.bantads.service.ClientService;
import com.bantads.entity.Client;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.CollectionModel;
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
    public
    CollectionModel<EntityModel<Client>> listClients(
        @RequestParam(value = "busca", required = false) String busca) {
        List<Client> clients = clientService.listClients(busca);
        return assembler.toCollectionModel(clients);
    }

    @GetMapping("/{cpf}")
    public 
    EntityModel<Client> getClient(@PathVariable String cpf) {
        Client client = clientService.getClient(cpf);
        return assembler.toModel(client);
    }

    @GetMapping("/{id}/solicitacoes")
    public
    Request getRequest(@PathVariable Long id) {
        return clientService.getRequestByClient(id);
    }
}
