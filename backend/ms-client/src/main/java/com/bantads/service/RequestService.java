package com.bantads.service;


import com.bantads.config.RabbitMQConfig;
import com.bantads.dto.AddressDTO;
import com.bantads.dto.RequestDTO;
import com.bantads.entity.Client;
import com.bantads.entity.Request;
import com.bantads.entity.RequestStatus;
import com.bantads.model.RabbitRequest;
import com.bantads.repository.AddressRepository;
import com.bantads.repository.ClientRepository;
import com.bantads.repository.RequestRepository;
import com.bantads.repository.StateRepository;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.List;

@Service
public class RequestService {

    @Autowired
    private RequestRepository requestRepository;

    @Autowired 
    private StateRepository stateRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ClientRepository clientRepository;

    public Request createRequest(RequestDTO requestDTO) {

        if(requestDTO.getCpf() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "CPF não informado");
        }

        if(requestRepository.existsByCpf(requestDTO.getCpf())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe um cliente com o CPF informado");
        }

        if(requestDTO.getEmail() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "E-mail não informado");
        }

        if(stateRepository.findByUf(requestDTO.getEndereco().getUf()) == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "UF não encontrado");
        }

        if(requestRepository.findByCpf(requestDTO.getCpf()) != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe uma solicitação com o CPF informado");
        }

        if(clientRepository.existsByCpf(requestDTO.getCpf())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe um cliente com o CPF informado");
        }

        if(clientRepository.existsByEmail(requestDTO.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe um cliente com o e-mail informado");
        }

        if(requestDTO.getCpf().length() < 11) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "CPF inválido");
        }

        AddressDTO addressDTO = requestDTO.getEndereco();
        BigDecimal salary = new BigDecimal(requestDTO.getSalario());

        Request requestEntity = Request.builder()
            .status(RequestStatus.PENDENTE)
            .additionalInfo(addressDTO.getComplemento())
            .cep(addressDTO.getCep())
            .city(addressDTO.getCidade())
            .state(stateRepository.findByUf(addressDTO.getUf()))
            .number(addressDTO.getNumero())
            .street(addressDTO.getLogradouro())
            .cpf(requestDTO.getCpf())
            .email(requestDTO.getEmail())
            .name(requestDTO.getNome())
            .phone(requestDTO.getTelefone())
            .salary(salary)
            .build();

        requestRepository.save(requestEntity);

        return requestEntity;
    }

    public void approveRequest(String cpf) {
        Request request = requestRepository.findByCpf(cpf);

        request.setApprovedAt(new Date());
        request.setStatus(RequestStatus.APROVADA);

        requestRepository.save(request);
    }

    @Transactional
    public void compensateRequestStatus(String cpf) {
        Request request = requestRepository.findByCpf(cpf);

        if (request == null) {
            return;
        }

        request.setApprovedAt(null);
        request.setStatus(RequestStatus.PENDENTE);

        requestRepository.save(request);
    }

    public Request rejectRequest(String cpf, String rejectReason) {
        if(rejectReason == null || rejectReason.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Motivo não informado");
        }

        Request request = requestRepository.findByCpf(cpf);

        if(request == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Solicitação não encontrada");
        }

        request.setRejectionReason(rejectReason);
        request.setStatus(RequestStatus.REJEITADA);

        requestRepository.save(request);

        return request;
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

    public List<Request> listRequests() {
        return requestRepository.findAll();
    }

    public Request getRequestByClient(Client client) {
        return requestRepository.findByClient(client);
    }

    public Request getRequestByCpf(String cpf) {
        Request request = requestRepository.findByCpf(cpf);

        if(request == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Solicitação não encontrada");
        }

        return request;
    }

    public Request getRequestById(Long id) {
        return requestRepository.getReferenceById(id);
    }

}
