package com.Prod.Chronos.entity;

public enum ScheduleType {
    ONE_TIME("Execute once at specified time"),
    CRON("Execute based on cron expression"),
    INTERVAL("Execute at regular intervals");
    
    private final String description;
    
    ScheduleType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
