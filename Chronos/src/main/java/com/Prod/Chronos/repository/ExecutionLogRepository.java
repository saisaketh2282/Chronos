package com.Prod.Chronos.repository;

import com.Prod.Chronos.entity.ExecutionLog;
import com.Prod.Chronos.entity.LogLevel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ExecutionLogRepository extends JpaRepository<ExecutionLog, Long> {
    
    // Find logs by job ID
    List<ExecutionLog> findByJobIdOrderByCreatedAtDesc(Long jobId);
    
    // Find logs by job ID with pagination
    Page<ExecutionLog> findByJobIdOrderByCreatedAtDesc(Long jobId, Pageable pageable);
    
    // Find logs by log level
    List<ExecutionLog> findByLogLevel(LogLevel logLevel);
    
    // Find logs by job ID and log level
    List<ExecutionLog> findByJobIdAndLogLevelOrderByCreatedAtDesc(Long jobId, LogLevel logLevel);
    
    // Find logs by date range
    @Query("SELECT el FROM ExecutionLog el WHERE el.createdAt BETWEEN :startDate AND :endDate ORDER BY el.createdAt DESC")
    List<ExecutionLog> findLogsByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    // Find logs by job ID and date range
    @Query("SELECT el FROM ExecutionLog el WHERE el.job.id = :jobId AND el.createdAt BETWEEN :startDate AND :endDate ORDER BY el.createdAt DESC")
    List<ExecutionLog> findLogsByJobIdAndDateRange(@Param("jobId") Long jobId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    // Find error logs for a job
    @Query("SELECT el FROM ExecutionLog el WHERE el.job.id = :jobId AND el.logLevel = 'ERROR' ORDER BY el.createdAt DESC")
    List<ExecutionLog> findErrorLogsByJobId(@Param("jobId") Long jobId);
    
    // Find logs by thread name
    List<ExecutionLog> findByThreadNameOrderByCreatedAtDesc(String threadName);
    
    // Count logs by job ID and log level
    long countByJobIdAndLogLevel(Long jobId, LogLevel logLevel);
    
    // Find logs for cleanup (older than specified days)
    @Query("SELECT el FROM ExecutionLog el WHERE el.createdAt < :cutoffDate")
    List<ExecutionLog> findLogsForCleanup(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    // Find recent logs (last N hours)
    @Query("SELECT el FROM ExecutionLog el WHERE el.createdAt >= :since ORDER BY el.createdAt DESC")
    List<ExecutionLog> findRecentLogs(@Param("since") LocalDateTime since);
    
    // Find logs by job IDs
    @Query("SELECT el FROM ExecutionLog el WHERE el.job.id IN :jobIds ORDER BY el.createdAt DESC")
    List<ExecutionLog> findByJobIdIn(@Param("jobIds") List<Long> jobIds);
    
    // Find logs with duration greater than specified milliseconds
    @Query("SELECT el FROM ExecutionLog el WHERE el.durationMs > :minDuration ORDER BY el.durationMs DESC")
    List<ExecutionLog> findLogsWithLongDuration(@Param("minDuration") Long minDuration);
    
    // Get log statistics for a job
    @Query("SELECT el.logLevel, COUNT(el) FROM ExecutionLog el WHERE el.job.id = :jobId GROUP BY el.logLevel")
    List<Object[]> getLogStatisticsByJobId(@Param("jobId") Long jobId);
}
