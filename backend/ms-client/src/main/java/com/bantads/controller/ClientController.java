/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
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

/**
 *
 * @author lenovo
 */
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

    @GetMapping("/{id}/solicitacoes")
    public
    Request getRequest(@PathVariable Long id) {
        return clientService.getRequestByClient(id);
    }
}
