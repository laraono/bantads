/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.bantads.service;

import com.bantads.entity.Address;
import com.bantads.entity.Client;
import com.bantads.entity.Request;
import com.bantads.entity.State;
import com.bantads.dto.AddressDTO;
import com.bantads.dto.ClientDTO;
import com.bantads.repository.ClientRepository;
import com.bantads.repository.StateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 *
 * @author lenovo
 */
@Service
public class ClientService {
    
    @Autowired
    private ClientRepository clientRepository;
     
    @Autowired
    private StateRepository stateRepository;

    @Autowired
    private RequestService requestService;

    public Client selfRegister(ClientDTO clientDTO) {
        State state = stateRepository.getReferenceById(clientDTO.getAddress().getStateId());

        AddressDTO addressDTO = clientDTO.getAddress();

        Address address = Address.builder()
            .additionalInfo(addressDTO.getAdditionalInfo())
            .cep(addressDTO.getCep())
            .city(addressDTO.getCity())
            .state(state)
            .street(addressDTO.getStreet())
            .number(addressDTO.getNumber())
            .build();

        Client clientEntity = Client.builder()
            .address(address)
            .cpf(clientDTO.getCpf())
            .email(clientDTO.getEmail())
            .name(clientDTO.getName())
            .phone(clientDTO.getPhone())
            .salary(clientDTO.getSalary())
            .build();

        Client newClient = clientRepository.save(clientEntity);

        requestService.associateRequestToClient(clientDTO.getRequestId(), newClient);

        return newClient;
    }

    public List<Client> listClients() {
        return clientRepository.findAll();
    }

    public Client getClient(Long id) {
        return clientRepository.getReferenceById(id);
    }

    public Request getRequestByClient(Long id) {
        Client client = clientRepository.getReferenceById(id);

        return requestService.getRequestByClient(client);
    }

}
