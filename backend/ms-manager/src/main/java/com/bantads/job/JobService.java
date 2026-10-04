package com.bantads.job;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class JobService {

    private final Map<String, Job> jobs = new ConcurrentHashMap<>();

    public String createJob() {
        String jobId = UUID.randomUUID().toString();

        jobs.put(jobId, Job.builder()
                .jobId(jobId)
                .status(JobStatus.PENDENTE)
                .resultType("inline")
                .build());

        return jobId;
    }

    public void complete(String jobId, Object result) {
        Job job = getJob(jobId);
        job.setStatus(JobStatus.CONCLUIDO);
        job.setResult(result);
    }

    public void fail(String jobId, String errorMessage) {
        Job job = getJob(jobId);
        job.setStatus(JobStatus.FALHA);
        job.setErrorMessage(errorMessage);
    }

    public Job getStatus(String jobId) {
        return getJob(jobId);
    }

    public Object getResult(String jobId) {
        Job job = getJob(jobId);

        if (job.getStatus() == JobStatus.PENDENTE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Job ainda não foi concluído");
        }

        if (job.getStatus() == JobStatus.FALHA) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, job.getErrorMessage());
        }

        return job.getResult();
    }

    private Job getJob(String jobId) {
        Job job = jobs.get(jobId);

        if (job == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Job não encontrado");
        }

        return job;
    }
}
