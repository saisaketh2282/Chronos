package com.Prod.Chronos.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import com.fasterxml.jackson.annotation.JsonBackReference;
import java.time.LocalDateTime;

@Entity
@Table(name = "job_schedules")
public class JobSchedule {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    @JsonBackReference
    private Job job;
    
    @NotNull(message = "Schedule type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "schedule_type", nullable = false)
    private ScheduleType scheduleType;
    
    @Column(name = "cron_expression")
    private String cronExpression;
    
    @Column(name = "execution_time")
    private LocalDateTime executionTime;
    
    @Column(name = "timezone")
    private String timezone = "UTC";
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "next_execution")
    private LocalDateTime nextExecution;
    
    @Column(name = "last_execution")
    private LocalDateTime lastExecution;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Constructors
    public JobSchedule() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public JobSchedule(Job job, ScheduleType scheduleType, LocalDateTime executionTime) {
        this();
        this.job = job;
        this.scheduleType = scheduleType;
        this.executionTime = executionTime;
        this.nextExecution = executionTime;
    }
    
    public JobSchedule(Job job, ScheduleType scheduleType, String cronExpression) {
        this();
        this.job = job;
        this.scheduleType = scheduleType;
        this.cronExpression = cronExpression;
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
    
    public ScheduleType getScheduleType() {
        return scheduleType;
    }
    
    public void setScheduleType(ScheduleType scheduleType) {
        this.scheduleType = scheduleType;
    }
    
    public String getCronExpression() {
        return cronExpression;
    }
    
    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }
    
    public LocalDateTime getExecutionTime() {
        return executionTime;
    }
    
    public void setExecutionTime(LocalDateTime executionTime) {
        this.executionTime = executionTime;
    }
    
    public String getTimezone() {
        return timezone;
    }
    
    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    public LocalDateTime getNextExecution() {
        return nextExecution;
    }
    
    public void setNextExecution(LocalDateTime nextExecution) {
        this.nextExecution = nextExecution;
    }
    
    public LocalDateTime getLastExecution() {
        return lastExecution;
    }
    
    public void setLastExecution(LocalDateTime lastExecution) {
        this.lastExecution = lastExecution;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    // Business methods
    public void updateLastExecution() {
        this.lastExecution = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public void deactivate() {
        this.isActive = false;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void activate() {
        this.isActive = true;
        this.updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
