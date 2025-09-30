package com.Prod.Chronos.entity;

public enum JobType {
    ONE_TIME("One-time execution"),
    RECURRING("Recurring execution"),
    BATCH("Batch processing");
    
    private final String description;
    
    JobType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
