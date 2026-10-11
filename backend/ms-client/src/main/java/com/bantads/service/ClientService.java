package com.bantads.service;

import com.bantads.entity.Client;
import com.bantads.entity.Request;
import com.bantads.repository.ClientRepository;
import com.bantads.entity.*;
import com.bantads.repository.AddressRepository;
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
    private StateRepository stateRepository;

    @Autowired
    private AddressRepository addressRepository;

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

    public Client createClient(Request req) {
        if(stateRepository.findByUf(req.getState().getUf()) == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "UF não encontrado");
        }

        Address addressEntity = Address.builder()
                .additionalInfo(req.getAdditionalInfo())
                .cep(req.getCep())
                .city(req.getCity())
                .state(req.getState())
                .number(req.getNumber())
                .street(req.getStreet())
                .build();

        Address address = addressRepository.save(addressEntity);

        Client client = Client.builder()
                .address(address)
                .cpf(req.getCpf())
                .email(req.getEmail())
                .name(req.getName())
                .phone(req.getPhone())
                .salary(req.getSalary())
                .build();

        return clientRepository.save(client);
    }

    public void deleteByCpf(String cpf) {
        this.clientRepository.deleteByCpf(cpf);
    }

}
