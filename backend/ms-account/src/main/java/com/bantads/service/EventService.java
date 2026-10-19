package com.bantads.service;

import com.bantads.config.RabbitMQConfig;
import com.bantads.dto.event.CreateEventDTO;
import com.bantads.dto.event.RabbitCommandDTO;
import com.bantads.dto.event.ReturnTransferDTO;
import com.bantads.entity.event.Event;
import com.bantads.entity.event.EventType;
import com.bantads.entity.read.AccountData;
import com.bantads.repository.event.EventRepository;
import jakarta.transaction.Transactional;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.*;

@Service
@Transactional

// Serviço de alterações (eventos)
public class EventService {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private AccountDataService accountDataService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public long getVersion(String objectId) {
        Event event = this.eventRepository.findFirstByObjectIdOrderByCreatedAtDesc(objectId);

        return event.getVersion() + 1;
    }

    public void checkUserCPF(String userCPF, String objectId) {
        Event event = this.eventRepository.findByObjectIdAndEventType(objectId, EventType.CREATED);

        String cpf = String.valueOf(event.getPayload().get("cpfCliente"));

        if(!cpf.equals(userCPF)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Não é a sua conta");
        }
    }

    public Event deposit(String userCPF, CreateEventDTO event, String objectId) {

        if(event.getPayload().get("valor") == null || event.getPayload().get("valor").toString().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Valor não informado");
        }
        if(!this.doesAccountExists(objectId)) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_CONTENT, "Conta não existe");
        }

        this.checkUserCPF(userCPF, objectId);

        Map<String, Object> payload = event.getPayload();

        String nome = payload.get("nome").toString();
        String cpf = payload.get("cpfCliente").toString();
        String valor = payload.get("valor").toString();

        if(nome.isEmpty() || cpf.isEmpty() || valor.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_CONTENT, "Campos obrigatórios");
        }
        
        Event newEvent = Event.builder()
                .payload(payload)
                .objectId(objectId)
                .eventType(EventType.DEPOSIT)
                .version(this.getVersion(objectId))
                .createdAt(new Date())
                .build();

        this.eventRepository.save(newEvent);

        Map<String, Object> newPayload = newEvent.getPayload();
        
        newPayload.putIfAbsent("eventId", newEvent.getEventId());
        newPayload.putIfAbsent("version", newEvent.getVersion());

        RabbitCommandDTO command = RabbitCommandDTO.builder()
                .payload(newPayload)
                .accountNumber(objectId)
                .timestamp(new Date())
                .type(EventType.DEPOSIT)
                .build();

        this.sendReadModelCommand(command, userCPF);
        
        return newEvent;
    }

    public void updateManager(CreateEventDTO event, String objectId) {
        if(!this.doesAccountExists(objectId)) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_CONTENT, "Conta não existe");
        }

        Map<String, Object> payload = event.getPayload();

        Event newEvent = Event.builder()
                .payload(payload)
                .objectId(objectId)
                .eventType(EventType.UPDATEMANAGER)
                .version(this.getVersion(objectId))
                .createdAt(new Date())
                .build();

        this.eventRepository.save(newEvent);

        Map<String, Object> newPayload = newEvent.getPayload();

        newPayload.putIfAbsent("eventId", newEvent.getEventId());
        newPayload.putIfAbsent("version", newEvent.getVersion());

        RabbitCommandDTO command = RabbitCommandDTO.builder()
                .payload(newPayload)
                .accountNumber(objectId)
                .timestamp(new Date())
                .type(EventType.UPDATEMANAGER)
                .build();

        this.sendReadModelCommand(command, "");
    }

    public Event withdraw(String userCPF, CreateEventDTO event, String objectId) {
        if(!this.doesAccountExists(objectId)) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_CONTENT, "Conta não existe");
        }

        this.checkUserCPF(userCPF, objectId);

        Map<String, Object> payload = event.getPayload();

        this.checkBalance(objectId, payload);

        Event newEvent = Event.builder()
                .payload(payload)
                .objectId(objectId)
                .eventType(EventType.WITHDRAW)
                .version(this.getVersion(objectId))
                .createdAt(new Date())
                .build();

        this.eventRepository.save(newEvent);

        Map<String, Object> newPayload = newEvent.getPayload();

        newPayload.putIfAbsent("eventId", newEvent.getEventId());
        newPayload.putIfAbsent("version", newEvent.getVersion());

        RabbitCommandDTO command = RabbitCommandDTO.builder()
                .payload(newPayload)
                .accountNumber(objectId)
                .timestamp(new Date())
                .type(EventType.WITHDRAW)
                .build();

        this.sendReadModelCommand(command, userCPF);

        return newEvent;
}

    public Event createAccount(Map<String, Object> payload) {

        String objectId = this.createObjectId();

        Event newEvent = Event.builder()
                .payload(payload)
                .objectId(objectId)
                .eventType(EventType.CREATED)
                .version(0)
                .createdAt(new Date())
                .build();

        this.eventRepository.save(newEvent);

        payload.putIfAbsent("eventId", newEvent.getEventId());
        payload.putIfAbsent("version", newEvent.getVersion());

        RabbitCommandDTO command = RabbitCommandDTO.builder()
                .payload(payload)
                .accountNumber(objectId)
                .timestamp(new Date())
                .type(EventType.CREATED)
                .build();

        this.sendReadModelCommand(command, "");

        return newEvent;
    }

    public List<String> deleteManager(Map<String, Object> payload) {
        String cpfGerente = String.valueOf(payload.get("cpfGerente"));

        List<String> gerentes = new ArrayList<>(this.accountDataService.getAllManagersCpf());

        if (gerentes.size() <= 1) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_CONTENT, "Não é possível remover o último gerente");
        }

        gerentes.remove(cpfGerente);

        String newManager = this.accountDataService.getManagerWithLowestAccount(gerentes);

        List<String> movedAccounts = new ArrayList<>();

        for (AccountData conta : this.accountDataService.getAccountsByManager(cpfGerente)) {
            this.moveAccountToManager(conta.getAccountNumber(), newManager);
            movedAccounts.add(conta.getAccountNumber());
        }

        return movedAccounts;
    }

    public void moveAccountToManager(String accountNumber, String managerCpf) {
        Map<String, Object> eventPayload = new HashMap<>();
        eventPayload.put("cpfGerente", managerCpf);

        this.updateManager(new CreateEventDTO(eventPayload), accountNumber);
    }

    public String createObjectId() {

        boolean isNumberUnique = false;

        SecureRandom rand = new SecureRandom();

        int maxTries = 100;
        int tries = 0;

        while(tries < maxTries) {
            int accountNumber = rand.nextInt(10000);

            String objectId = String.format("%04d" , accountNumber);

            isNumberUnique = !doesAccountExists(objectId);

            if(isNumberUnique) {
                return objectId;
            }

            tries++;
        }

        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public BigDecimal getBalance(String objectId) {
        List<Event> events = this.eventRepository.findAllByObjectIdOrderByVersionAsc(objectId);

        Iterator<Event> eventIterator = events.iterator();

        BigDecimal balance = new BigDecimal(0);

        while(eventIterator.hasNext()) {
            Event event = eventIterator.next();
            EventType eventType = event.getEventType();
            if(eventType == EventType.DEPOSIT || eventType == EventType.WITHDRAW) {
                String textValue = String.valueOf(event.getPayload().get("valor"));
                BigDecimal value = new BigDecimal(textValue);

                if(eventType == EventType.DEPOSIT) {
                    balance = balance.add(value);
                }

                if(eventType == EventType.WITHDRAW) {
                    balance = balance.subtract(value);
                }
            }
            if(eventType == EventType.DESTINATIONTRANSFER || eventType == EventType.ORIGINTRANSFER) {
                Map<String, Object> origem = (Map<String, Object>) event.getPayload().get("origem");
                String textValue = String.valueOf(origem.get("valor"));
                BigDecimal value = new BigDecimal(textValue);

                if(eventType == EventType.DESTINATIONTRANSFER) {
                    balance = balance.add(value);
                }

                if(eventType == EventType.ORIGINTRANSFER) {
                    balance = balance.subtract(value);
                }
            }

        }

        return balance;
    }

    public ReturnTransferDTO transfer(String userCPF, String originObjectId, CreateEventDTO transferDto) {
        Map<String, Object> payload = transferDto.getPayload();

        Map<String, Object> destination = (Map<String, Object>) payload.get("destino");
        Map<String, Object> origin = (Map<String, Object>) payload.get("origem");
        origin.put("cpf", userCPF);

        String destinationCPF = String.valueOf(destination.get("cpf"));
        String destinationObjectId = String.valueOf(destination.get("conta"));

        if(userCPF.equals(destinationCPF)) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_CONTENT, "Não pode transferir para sua própria conta");
        }

        if(!this.doesAccountExists(originObjectId) || !this.doesAccountExists(destinationObjectId)) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_CONTENT, "Conta não existe");
        }

        this.checkUserCPF(userCPF, originObjectId);

        this.checkBalance(originObjectId, origin);

        Event originTransfer = Event.builder()
            .payload(payload)
            .objectId(originObjectId)
            .eventType(EventType.ORIGINTRANSFER)
            .version(this.getVersion(originObjectId))
            .createdAt(new Date())
            .build();

        this.eventRepository.save(originTransfer);

        Event destinationTransfer = Event.builder()
            .payload(payload)
            .objectId(destinationObjectId)
            .eventType(EventType.DESTINATIONTRANSFER)
            .version(this.getVersion(destinationObjectId))
            .createdAt(new Date())
            .build();

        this.eventRepository.save(destinationTransfer);

        // cópia defensiva: os comandos do read model levam o payload completo
        // (origem + destino) pra cada lado conseguir montar o histórico com o
        // nome/cpf da outra ponta, sem contaminar o payload do evento (write side)
        Map<String, Object> originCommandPayload = new HashMap<>(payload);
        originCommandPayload.put("eventId", originTransfer.getEventId());
        originCommandPayload.put("version", originTransfer.getVersion());

        RabbitCommandDTO command = RabbitCommandDTO.builder()
                .payload(originCommandPayload)
                .accountNumber(originObjectId)
                .timestamp(new Date())
                .type(EventType.ORIGINTRANSFER)
                .build();

        Map<String, Object> destCommandPayload = new HashMap<>(payload);
        destCommandPayload.put("eventId", destinationTransfer.getEventId());
        destCommandPayload.put("version", destinationTransfer.getVersion());

        RabbitCommandDTO destCommand = RabbitCommandDTO.builder()
                .payload(destCommandPayload)
                .accountNumber(destinationObjectId)
                .timestamp(new Date())
                .type(EventType.DESTINATIONTRANSFER)
                .build();

        this.sendReadModelCommand(command, userCPF);
        this.sendReadModelCommand(destCommand, destinationCPF);

        String nome = String.valueOf(destination.get("nome"));

        return ReturnTransferDTO.builder().nome(nome).build();
        
    }

    public void checkBalance(String objectId, Map<String, Object> payload) {
        BigDecimal balance = this.getBalance(objectId);
        Object objectValue = payload.get("valor");
        BigDecimal value = new BigDecimal(objectValue.toString());

        if(value.compareTo(balance) > 0) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_CONTENT, "Sem saldo suficiente");
        }
    }

    public boolean doesAccountExists(String objectId) {
        return this.eventRepository.existsByObjectIdAndEventType(objectId, EventType.CREATED)
                && !this.eventRepository.existsByObjectIdAndEventType(objectId, EventType.DELETED);
    }

    public void sendReadModelCommand(RabbitCommandDTO command, String cpf) {
        Map<String, Object> payload = command.getPayload();

        payload.put("clienteCpf", cpf);
        command.setPayload(payload);

        rabbitTemplate.convertAndSend(RabbitMQConfig.EVENT_QUEUE, command);
    }

    public void deleteByObjectId(String objectId) {
        this.eventRepository.deleteByObjectId(objectId);
    }

}
