package com.bantads.service;

import com.bantads.config.RabbitMQConfig;
import com.bantads.dto.event.CreateEventDTO;
import com.bantads.dto.event.RabbitCommandDTO;
import com.bantads.dto.event.TransferDTO;
import com.bantads.entity.event.Event;
import com.bantads.entity.event.EventType;
import com.bantads.error.BadRequestError;
import com.bantads.repository.event.EventRepository;
import jakarta.transaction.Transactional;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    public Event createEvent(CreateEventDTO event, String objectId) {
        EventType eventType = event.getEventType();
        Map<String, String> payload = event.getPayload();

        if(!this.doesAccountExists(objectId)) {
            throw new BadRequestError("account doesn't exist");
        }

        if(eventType == EventType.WITHDRAW ) {
            this.checkBalance(objectId, payload);
        }

        Event newEvent = Event.builder()
            .payload(payload)
            .objectId(objectId)
            .eventType(eventType)
            .version(this.getVersion(objectId))
            .createdAt(new Date())
            .build();

        this.eventRepository.save(newEvent);

        RabbitCommandDTO command = RabbitCommandDTO.builder()
                .payload(event.getPayload())
                .accountNumber(objectId)
                .timestamp(new Date())
                .type(event.getEventType())
                .build();

        this.sendCommand(command);

        return newEvent;
    }

    public Event createAccount(CreateEventDTO event) {

        String objectId = this.createObjectId();

        Event newEvent = Event.builder()
                .payload(event.getPayload())
                .objectId(objectId)
                .eventType(event.getEventType())
                .version(this.getVersion(objectId))
                .createdAt(new Date())
                .build();

        this.eventRepository.save(newEvent);

        RabbitCommandDTO command = RabbitCommandDTO.builder()
                .payload(event.getPayload())
                .accountNumber(objectId)
                .timestamp(new Date())
                .type(event.getEventType())
                .build();

        this.sendCommand(command);

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

        throw new BadRequestError("");

    }

    public BigDecimal getBalance(String objectId) {
        List<Event> events = this.eventRepository.findAllByObjectIdOrderByVersionAsc(objectId);

        Iterator<Event> eventIterator = events.iterator();

        BigDecimal balance = new BigDecimal(0);

        while(eventIterator.hasNext()) {
            Event event = eventIterator.next();
            EventType eventType = event.getEventType();
            BigDecimal value = new BigDecimal(event.getPayload().get("value"));

            if(eventType == EventType.DEPOSIT || eventType == EventType.DESTINATIONTRANSFER) {
                balance.add(value);
            }

            if(eventType == EventType.WITHDRAW || eventType == EventType.ORIGINTRANSFER) {
                balance.subtract(value);
            }
        }

        return balance;
    }

    public List<Event> transfer(String originObjectId, TransferDTO transferDto) {
        List<Event> transferEvents = new ArrayList<>();

        String destinationObjectId = transferDto.getDestinationObjectId();
        Map<String, String> payload = transferDto.getPayload();

        if(!this.doesAccountExists(originObjectId) || !this.doesAccountExists(destinationObjectId)) {
            throw new BadRequestError("account doesn't exist");
        }

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

        transferEvents.add(originTransfer);
        transferEvents.add(destinationTransfer);

        RabbitCommandDTO command = RabbitCommandDTO.builder()
                .payload(transferDto.getPayload())
                .accountNumber(originObjectId)
                .timestamp(new Date())
                .type(transferDto.getEventType())
                .build();

        this.sendCommand(command);

        return transferEvents;
    }

    public void checkBalance(String objectId, Map<String, String> payload) {
        BigDecimal balance = this.getBalance(objectId);
        BigDecimal value = new BigDecimal(payload.get("value"));

        if(value.compareTo(balance) < 0) {
            throw new BadRequestError("not enough balance");
        }
    }

    public boolean doesAccountExists(String objectId) {
        return this.eventRepository.existsByObjectIdAndEventType(objectId, EventType.CREATED);
    }

    public void sendCommand(RabbitCommandDTO command) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.QUEUE_NAME, command);
    }

}
