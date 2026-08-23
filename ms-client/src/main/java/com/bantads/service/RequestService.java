package com.bantads.service;


import com.bantads.entity.Client;
import com.bantads.entity.Request;
import com.bantads.entity.RequestStatus;
import com.bantads.repository.RequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class RequestService {

    @Autowired
    private RequestRepository requestRepository;


    public void createRequest(Client client) {
        Request request = Request.builder()
            .client(client)
            .status(RequestStatus.PENDING.getLabel())
            .build();
        requestRepository.save(request);
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

        requestRepository.save(request);
    }

    public Request getRequestByClient(Client client) {
        return requestRepository.findByClient(client);
    }
}
