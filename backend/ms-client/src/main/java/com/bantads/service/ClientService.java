package com.bantads.service;

import com.bantads.entity.Address;
import com.bantads.entity.Client;
import com.bantads.entity.Request;
import com.bantads.entity.State;
import com.bantads.dto.AddressDTO;
import com.bantads.repository.ClientRepository;
import com.bantads.repository.StateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ClientService {
    
    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private RequestService requestService;

    public List<Client> listClients() {
        return clientRepository.findAll();
    }

    public Client getClient(Long id) {
        return clientRepository.getReferenceById(id);
    }

    public String getName(String cpf) {
        Client c = clientRepository.findByCpf(cpf);

        return c.getName();
    }

    public Request getRequestByClient(Long id) {
        Client client = clientRepository.getReferenceById(id);

        return requestService.getRequestByClient(client);
    }

}
