package com.bantads.orchestration.controller;


import com.bantads.orchestration.dto.StartSagaDTO;
import com.bantads.orchestration.model.SagaSteps;
import com.bantads.orchestration.services.OrchestrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@CrossOrigin
@RestController
@RequestMapping("/orchestration")
class OrchestrationController {

    @Autowired
    private OrchestrationService orchestrationService;

    @PostMapping("/create-account")
    ResponseEntity startSaga(@RequestBody Map<String, Object> payload) {
        try {
            orchestrationService.startSaga(SagaSteps.CRIACAO_CONTA, payload);
            return ResponseEntity.accepted().build();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}
