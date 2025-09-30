package com.Prod.Chronos.entity;

public enum JobStatus {
    SCHEDULED("Job is scheduled for execution"),
    RUNNING("Job is currently running"),
    COMPLETED("Job completed successfully"),
    FAILED("Job failed"),
    CANCELLED("Job was cancelled"),
    RETRYING("Job is being retried after failure");
    
    private final String description;
    
    JobStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
