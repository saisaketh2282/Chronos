package com.Prod.Chronos.repository;

import com.Prod.Chronos.entity.Job;
import com.Prod.Chronos.entity.JobStatus;
import com.Prod.Chronos.entity.JobType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {
    
    // Find jobs by status
    List<Job> findByStatus(JobStatus status);
    
    // Find jobs by type
    List<Job> findByJobType(JobType jobType);
    
    // Find jobs by creator
    List<Job> findByCreatedBy(String createdBy);
    
    // Find jobs by status and type
    List<Job> findByStatusAndJobType(JobStatus status, JobType jobType);
    
    // Find jobs ready for execution (scheduled and time has passed)
    @Query("SELECT j FROM Job j WHERE j.status = 'SCHEDULED' AND j.scheduledAt <= :currentTime ORDER BY j.priority DESC, j.scheduledAt ASC")
    List<Job> findJobsReadyForExecution(@Param("currentTime") LocalDateTime currentTime);
    
    // Find jobs that need retry
    @Query("SELECT j FROM Job j WHERE j.status = 'FAILED' AND j.currentRetryCount < j.maxRetries")
    List<Job> findJobsNeedingRetry();
    
    // Find jobs by date range
    @Query("SELECT j FROM Job j WHERE j.createdAt BETWEEN :startDate AND :endDate")
    List<Job> findJobsByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    // Find jobs by status with pagination
    Page<Job> findByStatus(JobStatus status, Pageable pageable);
    
    // Find jobs by creator with pagination
    Page<Job> findByCreatedBy(String createdBy, Pageable pageable);
    
    // Count jobs by status
    long countByStatus(JobStatus status);
    
    // Count jobs by type
    long countByJobType(JobType jobType);
    
    // Find jobs with execution logs
    @Query("SELECT j FROM Job j LEFT JOIN FETCH j.executionLogs WHERE j.id = :jobId")
    Optional<Job> findByIdWithExecutionLogs(@Param("jobId") Long jobId);
    
    // Find jobs with schedules
    @Query("SELECT j FROM Job j LEFT JOIN FETCH j.jobSchedule WHERE j.id = :jobId")
    Optional<Job> findByIdWithSchedule(@Param("jobId") Long jobId);
    
    // Find all jobs with schedules and logs
    @Query("SELECT j FROM Job j LEFT JOIN FETCH j.jobSchedule LEFT JOIN FETCH j.executionLogs WHERE j.id = :jobId")
    Optional<Job> findByIdWithScheduleAndLogs(@Param("jobId") Long jobId);
    
    // Find jobs for cleanup (older than specified days)
    @Query("SELECT j FROM Job j WHERE j.completedAt < :cutoffDate AND j.status IN ('COMPLETED', 'FAILED', 'CANCELLED')")
    List<Job> findJobsForCleanup(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    // Find running jobs (for monitoring)
    @Query("SELECT j FROM Job j WHERE j.status = 'RUNNING' AND j.startedAt < :timeoutThreshold")
    List<Job> findStuckRunningJobs(@Param("timeoutThreshold") LocalDateTime timeoutThreshold);
}
