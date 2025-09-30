package com.Prod.Chronos.service;

import com.Prod.Chronos.entity.*;
import com.Prod.Chronos.repository.JobRepository;
import com.Prod.Chronos.repository.JobScheduleRepository;
import com.Prod.Chronos.repository.ExecutionLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class JobService {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private JobScheduleRepository jobScheduleRepository;

    @Autowired
    private ExecutionLogRepository executionLogRepository;

    public Job createJob(Job job) {
        job.setCreatedAt(LocalDateTime.now());
        job.setUpdatedAt(LocalDateTime.now());
        return jobRepository.save(job);
    }

    public Job createOneTimeJob(String name, String description, String payload, 
                               LocalDateTime scheduledAt, String createdBy) {
        Job job = new Job(name, description, JobType.ONE_TIME, payload, createdBy);
        job.setScheduledAt(scheduledAt);
        job = createJob(job);

        // Create schedule for one-time job
        JobSchedule schedule = new JobSchedule(job, ScheduleType.ONE_TIME, scheduledAt);
        jobScheduleRepository.save(schedule);
        job.setJobSchedule(schedule);

        return job;
    }

    public Job createRecurringJob(String name, String description, String payload, 
                                 String cronExpression, String createdBy) {
        Job job = new Job(name, description, JobType.RECURRING, payload, createdBy);
        job = createJob(job);

        // Create schedule for recurring job
        JobSchedule schedule = new JobSchedule(job, ScheduleType.CRON, cronExpression);
        jobScheduleRepository.save(schedule);
        job.setJobSchedule(schedule);

        return job;
    }

    public Optional<Job> findById(Long id) {
        return jobRepository.findById(id);
    }

    public Optional<Job> findByIdWithLogs(Long id) {
        return jobRepository.findByIdWithExecutionLogs(id);
    }

    public Optional<Job> findByIdWithSchedule(Long id) {
        return jobRepository.findByIdWithSchedule(id);
    }

    public Optional<Job> findByIdWithScheduleAndLogs(Long id) {
        return jobRepository.findByIdWithScheduleAndLogs(id);
    }

    public List<Job> findAll() {
        return jobRepository.findAll();
    }

    public Page<Job> findAll(Pageable pageable) {
        return jobRepository.findAll(pageable);
    }

    public List<Job> findByStatus(JobStatus status) {
        return jobRepository.findByStatus(status);
    }

    public Page<Job> findByStatus(JobStatus status, Pageable pageable) {
        return jobRepository.findByStatus(status, pageable);
    }

    public List<Job> findByCreatedBy(String createdBy) {
        return jobRepository.findByCreatedBy(createdBy);
    }

    public Page<Job> findByCreatedBy(String createdBy, Pageable pageable) {
        return jobRepository.findByCreatedBy(createdBy, pageable);
    }

    public List<Job> findJobsReadyForExecution() {
        return jobRepository.findJobsReadyForExecution(LocalDateTime.now());
    }

    public List<Job> findJobsNeedingRetry() {
        return jobRepository.findJobsNeedingRetry();
    }

    public Job updateJob(Job job) {
        job.setUpdatedAt(LocalDateTime.now());
        return jobRepository.save(job);
    }

    public void deleteJob(Long id) {
        jobRepository.deleteById(id);
    }

    public Job cancelJob(Long id) {
        Optional<Job> jobOpt = jobRepository.findById(id);
        if (jobOpt.isPresent()) {
            Job job = jobOpt.get();
            if (job.getStatus() == JobStatus.SCHEDULED || job.getStatus() == JobStatus.RETRYING) {
                job.setStatus(JobStatus.CANCELLED);
                job.setUpdatedAt(LocalDateTime.now());
                
                // Deactivate schedule if exists
                if (job.getJobSchedule() != null) {
                    job.getJobSchedule().deactivate();
                }
                
                return jobRepository.save(job);
            }
        }
        return jobOpt.orElse(null);
    }

    public Job markJobAsRunning(Long id) {
        Optional<Job> jobOpt = jobRepository.findById(id);
        if (jobOpt.isPresent()) {
            Job job = jobOpt.get();
            job.markAsRunning();
            return jobRepository.save(job);
        }
        return null;
    }

    public Job markJobAsCompleted(Long id) {
        Optional<Job> jobOpt = jobRepository.findById(id);
        if (jobOpt.isPresent()) {
            Job job = jobOpt.get();
            job.markAsCompleted();
            return jobRepository.save(job);
        }
        return null;
    }

    public Job markJobAsFailed(Long id, String errorMessage) {
        Optional<Job> jobOpt = jobRepository.findById(id);
        if (jobOpt.isPresent()) {
            Job job = jobOpt.get();
            job.markAsFailed(errorMessage);
            return jobRepository.save(job);
        }
        return null;
    }

    public Job incrementRetryCount(Long id) {
        Optional<Job> jobOpt = jobRepository.findById(id);
        if (jobOpt.isPresent()) {
            Job job = jobOpt.get();
            job.incrementRetryCount();
            job.setStatus(JobStatus.RETRYING);
            return jobRepository.save(job);
        }
        return null;
    }

    public ExecutionLog addExecutionLog(Long jobId, LogLevel logLevel, String message) {
        Optional<Job> jobOpt = jobRepository.findById(jobId);
        if (jobOpt.isPresent()) {
            ExecutionLog log = new ExecutionLog(jobOpt.get(), logLevel, message);
            log.setThreadNameFromCurrentThread();
            return executionLogRepository.save(log);
        }
        return null;
    }

    public ExecutionLog addExecutionLog(Long jobId, LogLevel logLevel, String message, String details) {
        Optional<Job> jobOpt = jobRepository.findById(jobId);
        if (jobOpt.isPresent()) {
            ExecutionLog log = new ExecutionLog(jobOpt.get(), logLevel, message, details);
            log.setThreadNameFromCurrentThread();
            return executionLogRepository.save(log);
        }
        return null;
    }

    public List<ExecutionLog> getExecutionLogs(Long jobId) {
        return executionLogRepository.findByJobIdOrderByCreatedAtDesc(jobId);
    }

    public Page<ExecutionLog> getExecutionLogs(Long jobId, Pageable pageable) {
        return executionLogRepository.findByJobIdOrderByCreatedAtDesc(jobId, pageable);
    }

    public long countJobsByStatus(JobStatus status) {
        return jobRepository.countByStatus(status);
    }

    public long countJobsByType(JobType jobType) {
        return jobRepository.countByJobType(jobType);
    }

    public List<Job> findStuckRunningJobs(int timeoutMinutes) {
        LocalDateTime timeoutThreshold = LocalDateTime.now().minusMinutes(timeoutMinutes);
        return jobRepository.findStuckRunningJobs(timeoutThreshold);
    }

    public List<Job> findJobsForCleanup(int daysOld) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysOld);
        return jobRepository.findJobsForCleanup(cutoffDate);
    }
}
