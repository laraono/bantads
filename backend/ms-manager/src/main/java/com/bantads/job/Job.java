package com.bantads.job;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class Job {
    private String jobId;
    private JobStatus status;
    private String resultType;
    private Object result;
    private String errorMessage;
}
