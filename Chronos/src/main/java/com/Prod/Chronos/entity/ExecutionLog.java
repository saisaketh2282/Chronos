package com.Prod.Chronos.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import com.fasterxml.jackson.annotation.JsonBackReference;
import java.time.LocalDateTime;

@Entity
@Table(name = "execution_logs")
public class ExecutionLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    @JsonBackReference
    private Job job;
    
    @NotNull(message = "Log level is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "log_level", nullable = false)
    private LogLevel logLevel;
    
    @Column(name = "message", columnDefinition = "TEXT")
    private String message;
    
    @Column(name = "details", columnDefinition = "TEXT")
    private String details;
    
    @Column(name = "execution_time")
    private LocalDateTime executionTime;
    
    @Column(name = "duration_ms")
    private Long durationMs;
    
    @Column(name = "thread_name")
    private String threadName;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    // Constructors
    public ExecutionLog() {
        this.createdAt = LocalDateTime.now();
        this.executionTime = LocalDateTime.now();
    }
    
    public ExecutionLog(Job job, LogLevel logLevel, String message) {
        this();
        this.job = job;
        this.logLevel = logLevel;
        this.message = message;
    }
    
    public ExecutionLog(Job job, LogLevel logLevel, String message, String details) {
        this(job, logLevel, message);
        this.details = details;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Job getJob() {
        return job;
    }
    
    public void setJob(Job job) {
        this.job = job;
    }
    
    public LogLevel getLogLevel() {
        return logLevel;
    }
    
    public void setLogLevel(LogLevel logLevel) {
        this.logLevel = logLevel;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getDetails() {
        return details;
    }
    
    public void setDetails(String details) {
        this.details = details;
    }
    
    public LocalDateTime getExecutionTime() {
        return executionTime;
    }
    
    public void setExecutionTime(LocalDateTime executionTime) {
        this.executionTime = executionTime;
    }
    
    public Long getDurationMs() {
        return durationMs;
    }
    
    public void setDurationMs(Long durationMs) {
        this.durationMs = durationMs;
    }
    
    public String getThreadName() {
        return threadName;
    }
    
    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    // Business methods
    public void setThreadNameFromCurrentThread() {
        this.threadName = Thread.currentThread().getName();
    }
    
    public void calculateDuration(LocalDateTime startTime) {
        if (startTime != null) {
            this.durationMs = java.time.Duration.between(startTime, this.executionTime).toMillis();
        }
    }
}
