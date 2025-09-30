package com.Prod.Chronos.service;

import com.Prod.Chronos.entity.Job;
import com.Prod.Chronos.entity.JobSchedule;
import com.Prod.Chronos.entity.ScheduleType;
import com.Prod.Chronos.repository.JobRepository;
import com.Prod.Chronos.repository.JobScheduleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class JobSchedulerService {

    private static final Logger logger = LoggerFactory.getLogger(JobSchedulerService.class);

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private JobScheduleRepository jobScheduleRepository;

    @Autowired
    private JobExecutorService jobExecutorService;

    // @Autowired
    // private SplunkService splunkService; // Commented out due to dependency issues

    @Autowired
    private EmailNotificationService emailNotificationService;

    // Run every 30 seconds to check for jobs ready for execution
    @Scheduled(fixedRate = 30000)
    @Transactional
    public void scheduleOneTimeJobs() {
        try {
            List<Job> readyJobs = jobRepository.findJobsReadyForExecution(LocalDateTime.now());
            
            if (!readyJobs.isEmpty()) {
                logger.info("Found {} jobs ready for execution", readyJobs.size());
                
                for (Job job : readyJobs) {
                    if (job.getStatus() == com.Prod.Chronos.entity.JobStatus.SCHEDULED) {
                        // Execute job asynchronously
                        CompletableFuture<Void> future = jobExecutorService.executeJob(job.getId());
                        
                        // Log scheduling event
                        // splunkService.logJobEvent(job, "JOB_SCHEDULED", // Commented out
                        //     "Job scheduled for execution at " + LocalDateTime.now());
                        
                        logger.info("Scheduled job for execution: {} (ID: {})", job.getName(), job.getId());
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error in one-time job scheduler", e);
            // splunkService.logSystemEvent("SCHEDULER_ERROR", "Error in one-time job scheduler: " + e.getMessage(), null); // Commented out
        }
    }

    // Run every minute to check for recurring jobs
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void scheduleRecurringJobs() {
        try {
            List<JobSchedule> readySchedules = jobScheduleRepository.findSchedulesReadyForExecution(LocalDateTime.now());
            
            if (!readySchedules.isEmpty()) {
                logger.info("Found {} recurring schedules ready for execution", readySchedules.size());
                
                for (JobSchedule schedule : readySchedules) {
                    Job job = schedule.getJob();
                    
                    if (job.getStatus() == com.Prod.Chronos.entity.JobStatus.SCHEDULED && schedule.getIsActive()) {
                        // Execute job asynchronously
                        CompletableFuture<Void> future = jobExecutorService.executeJob(job.getId());
                        
                        // Update schedule for next execution
                        updateScheduleForNextExecution(schedule);
                        
                        // Log scheduling event
                        // splunkService.logJobEvent(job, "RECURRING_JOB_SCHEDULED", // Commented out 
                        //     "Recurring job scheduled for execution");
                        
                        logger.info("Scheduled recurring job for execution: {} (ID: {})", job.getName(), job.getId());
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error in recurring job scheduler", e);
            // splunkService.logSystemEvent("SCHEDULER_ERROR", "Error in recurring job scheduler: " + e.getMessage(), null); // Commented out
        }
    }

    // Run every 5 minutes to check for jobs needing retry
    @Scheduled(fixedRate = 300000)
    @Transactional
    public void scheduleRetryJobs() {
        try {
            List<Job> retryJobs = jobRepository.findJobsNeedingRetry();
            
            if (!retryJobs.isEmpty()) {
                logger.info("Found {} jobs needing retry", retryJobs.size());
                
                for (Job job : retryJobs) {
                    if (job.canRetry()) {
                        // Execute retry asynchronously
                        CompletableFuture<Void> future = jobExecutorService.retryJob(job.getId());
                        
                        // Log retry scheduling event
                        // splunkService.logRetryEvent(job, job.getCurrentRetryCount() + 1, // Commented out 
                        //     "Job scheduled for retry");
                        
                        logger.info("Scheduled job for retry: {} (ID: {}, attempt: {})", 
                                   job.getName(), job.getId(), job.getCurrentRetryCount() + 1);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error in retry job scheduler", e);
            // splunkService.logSystemEvent("SCHEDULER_ERROR", "Error in retry job scheduler: " + e.getMessage(), null); // Commented out
        }
    }

    // Run every hour to check for stuck jobs
    @Scheduled(fixedRate = 3600000)
    @Transactional
    public void checkStuckJobs() {
        try {
            List<Job> stuckJobs = jobRepository.findStuckRunningJobs(LocalDateTime.now().minusMinutes(30));
            
            if (!stuckJobs.isEmpty()) {
                logger.warn("Found {} stuck jobs", stuckJobs.size());
                
                for (Job job : stuckJobs) {
                    // Mark as failed due to timeout
                    job.setStatus(com.Prod.Chronos.entity.JobStatus.FAILED);
                    job.setErrorMessage("Job was marked as failed due to timeout (stuck for more than 30 minutes)");
                    job.setCompletedAt(LocalDateTime.now());
                    jobRepository.save(job);
                    
                    // Log stuck job event
                    // splunkService.logJobEvent(job, "JOB_STUCK_TIMEOUT", // Commented out 
                    //     "Job was marked as failed due to timeout");
                    
                    // Send alert email
                    emailNotificationService.sendSystemAlert("STUCK_JOB_TIMEOUT", 
                        "Job " + job.getName() + " was stuck and marked as failed", 
                        job.getCreatedBy(), null);
                    
                    logger.warn("Marked stuck job as failed: {} (ID: {})", job.getName(), job.getId());
                }
            }
        } catch (Exception e) {
            logger.error("Error checking stuck jobs", e);
            // splunkService.logSystemEvent("SCHEDULER_ERROR", "Error checking stuck jobs: " + e.getMessage(), null); // Commented out
        }
    }

    private void updateScheduleForNextExecution(JobSchedule schedule) {
        try {
            if (schedule.getScheduleType() == ScheduleType.ONE_TIME) {
                // One-time jobs should be deactivated after execution
                schedule.deactivate();
            } else if (schedule.getScheduleType() == ScheduleType.CRON) {
                // For cron jobs, calculate next execution time
                // This is a simplified implementation - in production, you'd use a proper cron parser
                LocalDateTime nextExecution = calculateNextCronExecution(schedule.getCronExpression());
                schedule.setNextExecution(nextExecution);
            }
            
            schedule.updateLastExecution();
            jobScheduleRepository.save(schedule);
            
        } catch (Exception e) {
            logger.error("Error updating schedule for next execution: {}", schedule.getId(), e);
        }
    }

    private LocalDateTime calculateNextCronExecution(String cronExpression) {
        // Simplified cron calculation - in production, use a proper cron library like Quartz
        // This is just a placeholder that adds 1 hour to current time
        return LocalDateTime.now().plusHours(1);
    }

    public void scheduleJob(Job job) {
        try {
            if (job.getJobType() == com.Prod.Chronos.entity.JobType.ONE_TIME) {
                // One-time jobs are already scheduled with their scheduledAt time
                logger.info("One-time job scheduled: {} (ID: {})", job.getName(), job.getId());
            } else if (job.getJobType() == com.Prod.Chronos.entity.JobType.RECURRING) {
                // Recurring jobs are handled by the cron scheduler
                logger.info("Recurring job scheduled: {} (ID: {})", job.getName(), job.getId());
            }
            
            // splunkService.logJobEvent(job, "JOB_SCHEDULED", "Job scheduled for execution"); // Commented out
            
        } catch (Exception e) {
            logger.error("Error scheduling job: {}", job.getId(), e);
        }
    }

    public void cancelJobSchedule(Long jobId) {
        try {
            JobSchedule schedule = jobScheduleRepository.findByJobId(jobId).orElse(null);
            if (schedule != null) {
                schedule.deactivate();
                jobScheduleRepository.save(schedule);
                
                logger.info("Cancelled schedule for job: {}", jobId);
            }
        } catch (Exception e) {
            logger.error("Error cancelling job schedule: {}", jobId, e);
        }
    }
}
