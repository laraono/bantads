package com.bantads.service;

import com.bantads.entity.Client;
import com.bantads.entity.Request;
import com.bantads.repository.ClientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClientService {
    
    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private RequestService requestService;

    public List<Client> listClients(String busca) {
        if (busca == null || busca.isBlank()) {
            return clientRepository.findAll();
        }

        return clientRepository.search(busca);
    }

    public Client getClient(String cpf) {
        return clientRepository.findByCpf(cpf);
    }

    public Request getRequestByClient(Long id) {
        Client client = clientRepository.getReferenceById(id);

        return requestService.getRequestByClient(client);
    }

}
