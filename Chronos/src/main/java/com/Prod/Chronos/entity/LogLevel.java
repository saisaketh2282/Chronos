package com.Prod.Chronos.entity;

public enum LogLevel {
    INFO("Informational message"),
    WARN("Warning message"),
    ERROR("Error message"),
    DEBUG("Debug message"),
    TRACE("Trace message");
    
    private final String description;
    
    LogLevel(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
