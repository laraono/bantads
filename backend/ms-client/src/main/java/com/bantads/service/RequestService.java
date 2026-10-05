package com.bantads.service;


import com.bantads.config.RabbitMQConfig;
import com.bantads.dto.AddressDTO;
import com.bantads.dto.RequestDTO;
import com.bantads.entity.Address;
import com.bantads.entity.Client;
import com.bantads.entity.Request;
import com.bantads.entity.RequestStatus;
import com.bantads.model.RabbitRequest;
import com.bantads.repository.AddressRepository;
import com.bantads.repository.ClientRepository;
import com.bantads.repository.RequestRepository;
import com.bantads.repository.StateRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.amqp.autoconfigure.RabbitConnectionDetails;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class RequestService {

    @Autowired
    private RequestRepository requestRepository;

    @Autowired 
    private AddressRepository addressRepository;

    @Autowired 
    private ClientRepository clientRepository;

    @Autowired 
    private StateRepository stateRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public Request createRequest(RequestDTO requestDTO) {

        if(requestDTO.getCpf() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "CPF não informado");
        }

        if(requestDTO.getEmail() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "E-mail não informado");
        }

        if(stateRepository.findByUf(requestDTO.getEndereco().getUf()) == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "UF não encontrado");
        }

        if(clientRepository.existsByCpf(requestDTO.getCpf())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe um cliente com o CPF informado");
        }

        if(clientRepository.existsByEmail(requestDTO.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe um cliente com o e-mail informado");
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

        Request requestEntity = Request.builder()
            .status(RequestStatus.PENDING.getLabel())
            .additionalInfo(addressDTO.getComplemento())
            .cep(addressDTO.getCep())
            .city(addressDTO.getCidade())
            .state(stateRepository.findByUf(addressDTO.getUf()))
            .city(addressDTO.getCidade())
            .number(addressDTO.getNumero())
            .street(addressDTO.getLogradouro())
            .cpf(requestDTO.getCpf())
            .email(requestDTO.getEmail())
            .name(requestDTO.getNome())
            .phone(requestDTO.getTelefone())
            .salary(requestDTO.getSalario())
            .build();

        requestRepository.save(requestEntity);

        return requestEntity;
    }

    public void approveRequest(Long id) {
        Request request = requestRepository.getReferenceById(id);

        request.setApprovedAt(new Date());
        request.setStatus(RequestStatus.APPROVED.getLabel());

        requestRepository.save(request);
    }

    public void rejectRequest(Long id, String rejectReason) {
        Request request = requestRepository.getReferenceById(id);

        request.setRejectionReason(rejectReason);
        request.setStatus(RequestStatus.REJECTED.getLabel());
        request.setRejectionAt(new Date());

        requestRepository.save(request);
        this.sendRejectionEmail(request);
    }

    private void sendRejectionEmail(Request request) {
        Map<String, Object> email = new HashMap<>();
        email.put("email", request.getEmail());
        email.put("nome", request.getName());
        email.put("motivo", request.getRejectionReason());

        RabbitRequest command = RabbitRequest.builder()
                .type("email.enviar-rejeicao")
                .payload(Map.of("email", email))
                .build();
        rabbitTemplate.convertAndSend(RabbitMQConfig.EMAIL_QUEUE, command);
    }

    public Request getRequestByClient(Client client) {
        return requestRepository.findByClient(client);
    }

    public Request getRequestById(Long id) {
        return requestRepository.getReferenceById(id);
    }

    public void associateRequestToClient(Long id, Client client) {
        Request request = requestRepository.getReferenceById(id);

        request.setClient(client);

        requestRepository.save(request);
    }
}
