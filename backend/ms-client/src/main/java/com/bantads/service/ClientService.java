package com.bantads.service;

import com.bantads.config.RabbitMQConfig;
import com.bantads.dto.RequestDTO;
import com.bantads.entity.*;
import com.bantads.dto.AddressDTO;
import com.bantads.model.RabbitAnswer;
import com.bantads.model.RabbitRequest;
import com.bantads.model.Status;
import com.bantads.repository.AddressRepository;
import com.bantads.repository.ClientRepository;
import com.bantads.repository.RabbitRepository;
import com.bantads.repository.StateRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

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

    public Client createClient(RequestDTO requestDTO) {
        if(stateRepository.findByUf(requestDTO.getEndereco().getUf()) == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "UF não encontrado");
        }

        AddressDTO addressDTO = requestDTO.getEndereco();

        Address addressEntity = Address.builder()
                .additionalInfo(addressDTO.getComplemento())
                .cep(addressDTO.getCep())
                .city(addressDTO.getCidade())
                .state(stateRepository.findByUf(addressDTO.getUf()))
                .city(addressDTO.getCidade())
                .number(addressDTO.getNumero())
                .street(addressDTO.getLogradouro())
                .build();

        Address address = addressRepository.save(addressEntity);

        Client client = Client.builder()
                .address(address)
                .cpf(requestDTO.getCpf())
                .email(requestDTO.getEmail())
                .name(requestDTO.getNome())
                .phone(requestDTO.getTelefone())
                .salary(requestDTO.getSalario())
                .build();

        return clientRepository.save(client);
    }

}
