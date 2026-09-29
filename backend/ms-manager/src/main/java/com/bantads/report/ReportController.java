package com.bantads.report;

import com.bantads.job.JobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/relatorios")
public class ReportController {

    @Autowired
    private JobService jobService;

    @Autowired
    private ClienteReportService clienteReportService;

    @GetMapping("/clientes")
    ResponseEntity<Map<String, String>> relatorioClientes(
            @RequestHeader(value = "x-user-tipo", required = false) String userTipo) {
        if (!"GERENTE".equals(userTipo)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acesso restrito a gerentes");
        }

        String jobId = jobService.createJob();
        clienteReportService.gerarRelatorioClientes(jobId);

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(Map.of("jobId", jobId));
    }
}
