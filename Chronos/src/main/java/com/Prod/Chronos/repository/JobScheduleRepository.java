package com.Prod.Chronos.repository;

import com.Prod.Chronos.entity.JobSchedule;
import com.Prod.Chronos.entity.ScheduleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface JobScheduleRepository extends JpaRepository<JobSchedule, Long> {
    
    // Find schedules by type
    List<JobSchedule> findByScheduleType(ScheduleType scheduleType);
    
    // Find active schedules
    List<JobSchedule> findByIsActiveTrue();
    
    // Find schedules ready for execution
    @Query("SELECT js FROM JobSchedule js WHERE js.isActive = true AND js.nextExecution <= :currentTime ORDER BY js.nextExecution ASC")
    List<JobSchedule> findSchedulesReadyForExecution(@Param("currentTime") LocalDateTime currentTime);
    
    // Find recurring schedules (cron-based)
    @Query("SELECT js FROM JobSchedule js WHERE js.scheduleType = 'CRON' AND js.isActive = true")
    List<JobSchedule> findActiveRecurringSchedules();
    
    // Find one-time schedules ready for execution
    @Query("SELECT js FROM JobSchedule js WHERE js.scheduleType = 'ONE_TIME' AND js.isActive = true AND js.nextExecution <= :currentTime")
    List<JobSchedule> findOneTimeSchedulesReadyForExecution(@Param("currentTime") LocalDateTime currentTime);
    
    // Find schedule by job ID
    Optional<JobSchedule> findByJobId(Long jobId);
    
    // Find schedules by job IDs
    @Query("SELECT js FROM JobSchedule js WHERE js.job.id IN :jobIds")
    List<JobSchedule> findByJobIdIn(@Param("jobIds") List<Long> jobIds);
    
    // Update next execution time
    @Query("UPDATE JobSchedule js SET js.nextExecution = :nextExecution, js.updatedAt = :updatedAt WHERE js.id = :scheduleId")
    void updateNextExecution(@Param("scheduleId") Long scheduleId, @Param("nextExecution") LocalDateTime nextExecution, @Param("updatedAt") LocalDateTime updatedAt);
    
    // Find schedules that need next execution calculation
    @Query("SELECT js FROM JobSchedule js WHERE js.scheduleType = 'CRON' AND js.isActive = true AND (js.nextExecution IS NULL OR js.nextExecution <= :currentTime)")
    List<JobSchedule> findSchedulesNeedingNextExecutionCalculation(@Param("currentTime") LocalDateTime currentTime);
    
    // Count active schedules by type
    long countByScheduleTypeAndIsActiveTrue(ScheduleType scheduleType);
    
    // Find schedules by timezone
    List<JobSchedule> findByTimezone(String timezone);
    
    // Find schedules created in date range
    @Query("SELECT js FROM JobSchedule js WHERE js.createdAt BETWEEN :startDate AND :endDate")
    List<JobSchedule> findSchedulesByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}
