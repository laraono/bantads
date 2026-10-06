package com.bantads.service;


import com.bantads.dto.AddressDTO;
import com.bantads.dto.RequestDTO;
import com.bantads.entity.Client;
import com.bantads.entity.Request;
import com.bantads.entity.RequestStatus;
import com.bantads.repository.AddressRepository;
import com.bantads.repository.ClientRepository;
import com.bantads.repository.RequestRepository;
import com.bantads.repository.StateRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

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

    public List<Request> listRequests() {
        return this.requestRepository.findAll();
    }

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

        if(requestRepository.existsByEmail(requestDTO.getEmail())) {
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
        request.setStatus(RequestStatus.APROVADO);

        requestRepository.save(request);
    }

    public void rejectRequest(String cpf, String rejectReason) {
        Request request = requestRepository.findByCpf(cpf);

        request.setRejectionReason(rejectReason);
        request.setStatus(RequestStatus.NÃO_APROVADA);

        requestRepository.save(request);
    }

    public void compensateRequestStatus(String cpf) {
        Request request = requestRepository.findByCpf(cpf);

        request.setApprovedAt(null);
        request.setStatus(RequestStatus.PENDENTE);

        requestRepository.save(request);
    }

    public Request getRequestByClient(Client client) {
        return requestRepository.findByClient(client);
    }

    public Request getRequestByCpf(String cpf) {
        Request req = requestRepository.findByCpf(cpf);

        if(req == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Solicitacao não encontrada");
        }

        return req;
    }

}
