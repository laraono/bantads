/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.bantads.controller;

import com.bantads.dto.ClientDTO;
import com.bantads.entity.Request;
import com.bantads.service.ClientService;
import com.bantads.entity.Client;
import java.util.List;

import com.bantads.service.RequestService;
import org.springframework.beans.factory.annotation.Autowired;
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
    private RequestService requestService;

    @PostMapping
    Client selfRegister(@RequestBody ClientDTO client) {
        return clientService.selfRegister(client);
    }
    
    @GetMapping
    List<Client> listClients() {
        return clientService.listClients();
    }

    @GetMapping("/{id}")
    Client getClient(@PathVariable Long id) {
        return clientService.getClient(id);
    }

    @GetMapping("/{id}/request")
    Request getRequest(@PathVariable Long id) {
        return clientService.getRequestByClient(id);
    }
}
