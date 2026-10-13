package com.bantads.service;

import com.bantads.entity.Client;
import com.bantads.entity.Request;
import com.bantads.model.Status;
import com.bantads.repository.ClientRepository;
import com.bantads.entity.*;
import com.bantads.repository.AddressRepository;
import com.bantads.repository.StateRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;


@Service
public class ClientService {
    
    @Autowired
    private ObjectMapper objectMapper;

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

    public List<Client> getClientsByCpf(List<String> cpfs) {
        return clientRepository.findAllByCpfIn(cpfs);
    }

    public Client createClient(Map<String, Object> payload) {

        Request req = objectMapper.convertValue(payload.get("requisicao"), Request.class);

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
                .approvedStatus("SUCESSO")
                .salary(req.getSalary())
                .build();

        return clientRepository.save(client);
    }

    public void deleteClient(Map<String, Object> payload) {
        Object cpf = payload.get("cpfCliente");

        if (cpf == null) {
            return;
        }

        Client client = clientRepository.findByCpf(cpf.toString());

        if (client != null) {
            clientRepository.delete(client);
        }
    }

}
