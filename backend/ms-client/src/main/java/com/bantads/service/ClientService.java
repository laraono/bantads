package com.bantads.service;

import com.bantads.entity.Client;
import com.bantads.entity.Request;
import com.bantads.repository.ClientRepository;
import com.bantads.config.RabbitMQConfig;
import com.bantads.dto.RequestDTO;
import com.bantads.entity.*;
import com.bantads.dto.AddressDTO;
import com.bantads.model.RabbitAnswer;
import com.bantads.model.RabbitRequest;
import com.bantads.model.Status;
import com.bantads.repository.AddressRepository;
import com.bantads.repository.RabbitRepository;
import com.bantads.repository.StateRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

}
