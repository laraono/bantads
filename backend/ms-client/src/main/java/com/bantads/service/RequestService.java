package com.bantads.service;

import com.bantads.config.RabbitMQConfig;
import com.bantads.dto.AddressDTO;
import com.bantads.dto.RequestDTO;
import com.bantads.entity.Client;
import com.bantads.entity.Request;
import com.bantads.entity.RequestStatus;
import com.bantads.model.RabbitRequest;
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

    public void conflictThrow(String error) {
        throw new ResponseStatusException(HttpStatus.CONFLICT, error);
    }

    public Request createRequest(RequestDTO requestDTO) {
        try {

            if (requestRepository.existsByCpf(requestDTO.getCpf())) {
                conflictThrow("Já existe uma solicitação com o CPF informado");
            }

            if (requestRepository.existsByEmail(requestDTO.getEmail())) {
                conflictThrow("Já existe uma solicitação com o CPF informado");
            }

            if (clientRepository.existsByCpf(requestDTO.getCpf())) {
                conflictThrow("Já existe um cliente com o CPF informado");
            }

            if (clientRepository.existsByEmail(requestDTO.getEmail())) {
                conflictThrow("Já existe um cliente com o e-mail informado");
            }

            AddressDTO addressDTO = requestDTO.getEndereco(); // ajuda com legibilidade

            BigDecimal salary = new BigDecimal(requestDTO.getSalario()); // sai de string p/ decimal

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
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Erro ao criar solicitação");
        }

    }

    public Request approveRequest(String cpf) {
        Request request = requestRepository.findByCpf(cpf);

        if (request == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Solicitação não encontrada");
        }

        // solicitação rejeitada não volta a ser aprovada: o cliente precisa pedir de
        // novo
        if (request.getStatus() != RequestStatus.PENDENTE) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_CONTENT, "Solicitação não está pendente");
        }

        request.setApprovedAt(new Date());
        request.setStatus(RequestStatus.APROVADA);

        requestRepository.save(request);

        return request;
    }

    // R9, caso especial: aprovação impossível (e-mail/login já cadastrado) vira
    // "Não aprovada" com motivo automático, para não ficar pendente para sempre
    @Transactional
    public void markNotApproved(String cpf, String motivo) {
        Request request = requestRepository.findByCpf(cpf);

        if (request == null) {
            return;
        }

        request.setApprovedAt(null);
        request.setRejectionReason(motivo);
        request.setRejectionAt(new Date());
        request.setStatus(RequestStatus.REJEITADA);

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
        if (rejectReason == null || rejectReason.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Motivo não informado");
        }

        Request request = requestRepository.findByCpf(cpf);

        if (request == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Solicitação não encontrada");
        }

        if (request.getStatus() != RequestStatus.PENDENTE) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_CONTENT, "Solicitação não está pendente");
        }

        request.setRejectionReason(rejectReason);
        request.setRejectionAt(new Date());
        request.setStatus(RequestStatus.REJEITADA);

        requestRepository.save(request);
        this.sendRejectionEmail(request);

        return request;
    }

    private void sendRejectionEmail(Request request) {
        Map<String, Object> email = new HashMap<>();
        email.put("email", request.getEmail());
        email.put("nome", request.getName());

        RabbitRequest command = RabbitRequest.builder()
                .type("email.enviar-rejeicao")
                .payload(Map.of("email", email))
                .error(request.getRejectionReason())
                .build();
        rabbitTemplate.convertAndSend(RabbitMQConfig.EMAIL_QUEUE, command);
    }

    public void sendAlreadyExistsEmail(String cpf) {
        Request request = this.getRequestByCpf(cpf);
        Map<String, Object> email = new HashMap<>();
        email.put("email", request.getEmail());
        email.put("nome", request.getName());
        RabbitRequest command = RabbitRequest.builder()
                .type("email.enviar-ja-existe")
                .payload(Map.of("email", email))
                .error(request.getRejectionReason())
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

        if (request == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Solicitação não encontrada");
        }

        return request;
    }

    public Request getRequestById(Long id) {
        return requestRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Requisição não encontrada"));
    }

}
