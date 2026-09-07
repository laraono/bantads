package com.bantads.service;

import com.bantads.config.RabbitMQConfig;
import com.bantads.dto.event.CreateEventDTO;
import com.bantads.dto.event.RabbitCommandDTO;
import com.bantads.dto.event.ReturnTransferDTO;
import com.bantads.dto.event.TransferDTO;
import com.bantads.entity.event.Event;
import com.bantads.entity.event.EventType;
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
public class EventService {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public long getVersion(String objectId) {
        Event event = this.eventRepository.findFirstByObjectIdOrderByCreatedAtDesc(objectId);

        return event.getVersion() + 1;
    }

    public void checkUserCPF(String userCPF, String objectId) {
        Event event = this.eventRepository.findByObjectIdAndEventType(objectId, EventType.CREATED);

        String cpf = (String) event.getPayload().get("cpfCliente");

        if(!cpf.equals(userCPF)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your own account");
        }
    }

    public void deposit(String userCPF, CreateEventDTO event, String objectId) {
        if(!this.doesAccountExists(objectId)) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_CONTENT, "Account doesn't exist");
        }

        this.checkUserCPF(userCPF, objectId);

        Map<String, Object> payload = event.getPayload();

        Event newEvent = Event.builder()
                .payload(payload)
                .objectId(objectId)
                .eventType(EventType.DEPOSIT)
                .version(this.getVersion(objectId))
                .createdAt(new Date())
                .build();

        this.eventRepository.save(newEvent);

        RabbitCommandDTO command = RabbitCommandDTO.builder()
                .payload(event.getPayload())
                .accountNumber(objectId)
                .timestamp(new Date())
                .type(EventType.DEPOSIT)
                .build();

        this.sendCommand(command, userCPF);
    }

    public void updateManager(CreateEventDTO event, String objectId) {
        if(!this.doesAccountExists(objectId)) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_CONTENT, "Account doesn't exist");
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

        RabbitCommandDTO command = RabbitCommandDTO.builder()
                .payload(event.getPayload())
                .accountNumber(objectId)
                .timestamp(new Date())
                .type(EventType.UPDATEMANAGER)
                .build();

        this.sendCommand(command, "");
    }

    public void withdraw(String userCPF, CreateEventDTO event, String objectId) {
        if(!this.doesAccountExists(objectId)) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_CONTENT, "Account doesn't exist");
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

        RabbitCommandDTO command = RabbitCommandDTO.builder()
                .payload(event.getPayload())
                .accountNumber(objectId)
                .timestamp(new Date())
                .type(EventType.WITHDRAW)
                .build();

        this.sendCommand(command, userCPF);
    }

    public Event createAccount(CreateEventDTO event) {

        String objectId = this.createObjectId();

        Event newEvent = Event.builder()
                .payload(event.getPayload())
                .objectId(objectId)
                .eventType(EventType.CREATED)
                .version(this.getVersion(objectId))
                .createdAt(new Date())
                .build();

        this.eventRepository.save(newEvent);

        RabbitCommandDTO command = RabbitCommandDTO.builder()
                .payload(event.getPayload())
                .accountNumber(objectId)
                .timestamp(new Date())
                .type(EventType.CREATED)
                .build();

        this.sendCommand(command, "");

        return newEvent;
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
            String textValue = (String) event.getPayload().get("value");
            BigDecimal value = new BigDecimal(textValue);

            if(eventType == EventType.DEPOSIT || eventType == EventType.DESTINATIONTRANSFER) {
                balance = balance.add(value);
            }

            if(eventType == EventType.WITHDRAW || eventType == EventType.ORIGINTRANSFER) {
                balance = balance.subtract(value);
            }
        }

        return balance;
    }

    public ReturnTransferDTO transfer(String userCPF, String originObjectId, TransferDTO transferDto) {
        String destinationObjectId = transferDto.getDestinationObjectId();
        Map<String, Object> payload = transferDto.getPayload();

        if(!this.doesAccountExists(originObjectId) || !this.doesAccountExists(destinationObjectId)) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_CONTENT, "Account doesn't exist");
        }

        this.checkUserCPF(userCPF, originObjectId);

        this.checkBalance(originObjectId, payload);

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

        RabbitCommandDTO command = RabbitCommandDTO.builder()
                .payload(transferDto.getPayload())
                .accountNumber(originObjectId)
                .timestamp(new Date())
                .type(transferDto.getEventType())
                .build();

        this.sendCommand(command, userCPF);

        Map<String, Object> destino = (Map<String, Object>) payload.get("destino");
        String nome = (String) destino.get("nome");

        return ReturnTransferDTO.builder().nome(nome).build();
    }

    public void checkBalance(String objectId, Map<String, Object> payload) {
        BigDecimal balance = this.getBalance(objectId);
        String textValue = (String) payload.get("value");
        BigDecimal value = new BigDecimal(textValue);

        if(value.compareTo(balance) < 0) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_CONTENT, "Not enough balance");
        }
    }

    public boolean doesAccountExists(String objectId) {
        return this.eventRepository.existsByObjectIdAndEventType(objectId, EventType.CREATED);
    }

    public void sendCommand(RabbitCommandDTO command, String cpf) {
        Map<String, Object> payload = command.getPayload();

        payload.put("clienteCpf", cpf);
        command.setPayload(payload);

        rabbitTemplate.convertAndSend(RabbitMQConfig.QUEUE_NAME, command);
    }

}
