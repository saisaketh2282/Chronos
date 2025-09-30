package com.Prod.Chronos.service;

import com.Prod.Chronos.entity.*;
import com.Prod.Chronos.repository.JobRepository;
import com.Prod.Chronos.repository.ExecutionLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Service
public class JobExecutorService {

    private static final Logger logger = LoggerFactory.getLogger(JobExecutorService.class);

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private ExecutionLogRepository executionLogRepository;

    @Autowired
    @Qualifier("jobExecutor")
    private Executor jobExecutor;

    @Autowired
    @Qualifier("retryExecutor")
    private Executor retryExecutor;

    // @Autowired
    // private SplunkService splunkService; // Commented out due to dependency issues

    @Autowired
    private KafkaService kafkaService;

    @Async("jobExecutor")
    @Transactional
    public CompletableFuture<Void> executeJob(Long jobId) {
        return CompletableFuture.runAsync(() -> {
            try {
                Job job = jobRepository.findById(jobId).orElse(null);
                if (job == null) {
                    logger.error("Job not found: {}", jobId);
                    return;
                }

                logger.info("Starting execution of job: {} (ID: {})", job.getName(), jobId);
                
                // Mark job as running
                job.markAsRunning();
                job = jobRepository.save(job);

                // Log job start
                ExecutionLog startLog = new ExecutionLog(job, LogLevel.INFO, "Job execution started");
                startLog.setThreadNameFromCurrentThread();
                executionLogRepository.save(startLog);

                // Send to Splunk
                // splunkService.logJobEvent(job, "JOB_STARTED", "Job execution started"); // Commented out

                // Simulate job execution (in real implementation, this would be actual job logic)
                boolean success = executeJobLogic(job);

                if (success) {
                    // Job completed successfully
                    job.markAsCompleted();
                    jobRepository.save(job);

                    ExecutionLog successLog = new ExecutionLog(job, LogLevel.INFO, "Job completed successfully");
                    successLog.setThreadNameFromCurrentThread();
                    executionLogRepository.save(successLog);

                    // splunkService.logJobEvent(job, "JOB_COMPLETED", "Job completed successfully"); // Commented out

                    logger.info("Job completed successfully: {} (ID: {})", job.getName(), jobId);
                } else {
                    // Job failed
                    String errorMessage = "Job execution failed";
                    job.markAsFailed(errorMessage);
                    jobRepository.save(job);

                    ExecutionLog errorLog = new ExecutionLog(job, LogLevel.ERROR, "Job execution failed", errorMessage);
                    errorLog.setThreadNameFromCurrentThread();
                    executionLogRepository.save(errorLog);

                    // splunkService.logJobEvent(job, "JOB_FAILED", "Job execution failed: " + errorMessage); // Commented out

                    // Send failure event to Kafka for retry processing
                    kafkaService.sendFailureEvent(job, errorMessage);

                    logger.error("Job failed: {} (ID: {})", job.getName(), jobId);
                }

            } catch (Exception e) {
                logger.error("Error executing job: {}", jobId, e);
                handleJobExecutionError(jobId, e);
            }
        }, jobExecutor);
    }

    @Async("retryExecutor")
    @Transactional
    public CompletableFuture<Void> retryJob(Long jobId) {
        return CompletableFuture.runAsync(() -> {
            try {
                Job job = jobRepository.findById(jobId).orElse(null);
                if (job == null) {
                    logger.error("Job not found for retry: {}", jobId);
                    return;
                }

                if (!job.canRetry()) {
                    logger.warn("Job {} has exceeded max retries ({}), moving to dead letter queue", 
                               jobId, job.getMaxRetries());
                    
                    // Send to dead letter queue
                    kafkaService.sendToDeadLetterQueue(job, "Max retries exceeded");
                    return;
                }

                logger.info("Retrying job: {} (ID: {}, attempt: {})", 
                           job.getName(), jobId, job.getCurrentRetryCount() + 1);

                // Increment retry count and set status to retrying
                job.incrementRetryCount();
                job.setStatus(JobStatus.RETRYING);
                job = jobRepository.save(job);

                // Log retry attempt
                ExecutionLog retryLog = new ExecutionLog(job, LogLevel.WARN, 
                    "Retrying job execution (attempt " + job.getCurrentRetryCount() + ")");
                retryLog.setThreadNameFromCurrentThread();
                executionLogRepository.save(retryLog);

                // splunkService.logJobEvent(job, "JOB_RETRY", "Retrying job execution"); // Commented out

                // Execute the job again
                executeJob(jobId).join();

            } catch (Exception e) {
                logger.error("Error retrying job: {}", jobId, e);
                handleJobExecutionError(jobId, e);
            }
        }, retryExecutor);
    }

    private boolean executeJobLogic(Job job) {
        try {
            // Simulate job execution time
            Thread.sleep(1000 + (long) (Math.random() * 2000));

            // Simulate success/failure (90% success rate for demo)
            return Math.random() > 0.1;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    private void handleJobExecutionError(Long jobId, Exception e) {
        try {
            Job job = jobRepository.findById(jobId).orElse(null);
            if (job != null) {
                String errorMessage = "Unexpected error during job execution: " + e.getMessage();
                job.markAsFailed(errorMessage);
                jobRepository.save(job);

                ExecutionLog errorLog = new ExecutionLog(job, LogLevel.ERROR, 
                    "Unexpected error during job execution", e.getMessage());
                errorLog.setThreadNameFromCurrentThread();
                executionLogRepository.save(errorLog);

                // splunkService.logJobEvent(job, "JOB_ERROR", errorMessage); // Commented out

                // Send failure event to Kafka
                kafkaService.sendFailureEvent(job, errorMessage);
            }
        } catch (Exception ex) {
            logger.error("Error handling job execution error for job: {}", jobId, ex);
        }
    }

    public void executeJobSync(Long jobId) {
        executeJob(jobId).join();
    }

    public void retryJobSync(Long jobId) {
        retryJob(jobId).join();
    }
}
