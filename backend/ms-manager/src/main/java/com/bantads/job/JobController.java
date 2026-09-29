package com.bantads.job;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/jobs")
public class JobController {

    @Autowired
    private JobService jobService;

    @GetMapping("/{jobId}/status")
    Map<String, Object> getStatus(@PathVariable String jobId) {
        Job job = jobService.getStatus(jobId);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("jobId", job.getJobId());
        body.put("status", job.getStatus().name());
        body.put("resultType", job.getResultType());

        return body;
    }

    @GetMapping("/{jobId}/result")
    Object getResult(@PathVariable String jobId) {
        return jobService.getResult(jobId);
    }
}
