package com.bantads.controller;

import com.bantads.assembler.ClientModelAssembler;
import com.bantads.entity.Request;
import com.bantads.service.ClientService;
import com.bantads.entity.Client;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.ResponseEntity;
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
    CollectionModel<EntityModel<Client>> listClients() {
        List<Client> clients = clientService.listClients();
        return assembler.toCollectionModel(clients);
    }

    @GetMapping("/{id}")
    public 
    EntityModel<Client> getClient(@PathVariable Long id) {
        Client client = clientService.getClient(id);
        return assembler.toModel(client);
    }

    @GetMapping("/{cpf}/cpf")
    public ResponseEntity<String> getName(@PathVariable String cpf) {
        String name = clientService.getName(cpf);
        return ResponseEntity.ok().body(name);
    }

    @GetMapping("/{id}/solicitacoes")
    public
    Request getRequest(@PathVariable Long id) {
        return clientService.getRequestByClient(id);
    }
}
