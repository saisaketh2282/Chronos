package com.Prod.Chronos.controller;

import com.Prod.Chronos.entity.Job;
import com.Prod.Chronos.service.JobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    @Autowired
    private JobService jobService;

    @GetMapping("/jobs/stuck")
    public ResponseEntity<?> getStuckJobs(@RequestParam(defaultValue = "30") int timeoutMinutes) {
        try {
            List<Job> stuckJobs = jobService.findStuckRunningJobs(timeoutMinutes);
            return ResponseEntity.ok(stuckJobs);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve stuck jobs");
            error.put("message", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    @PostMapping("/jobs/stuck/reset")
    public ResponseEntity<?> resetStuckJobs(@RequestParam(defaultValue = "30") int timeoutMinutes) {
        try {
            List<Job> stuckJobs = jobService.findStuckRunningJobs(timeoutMinutes);
            int resetCount = 0;
            
            for (Job job : stuckJobs) {
                job.setStatus(com.Prod.Chronos.entity.JobStatus.FAILED);
                job.setErrorMessage("Job was reset due to timeout");
                jobService.updateJob(job);
                resetCount++;
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("message", "Reset " + resetCount + " stuck jobs");
            result.put("resetCount", resetCount);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to reset stuck jobs");
            error.put("message", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    @GetMapping("/jobs/cleanup")
    public ResponseEntity<?> getJobsForCleanup(@RequestParam(defaultValue = "30") int daysOld) {
        try {
            List<Job> jobsForCleanup = jobService.findJobsForCleanup(daysOld);
            return ResponseEntity.ok(jobsForCleanup);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve jobs for cleanup");
            error.put("message", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    @DeleteMapping("/jobs/cleanup")
    public ResponseEntity<?> cleanupOldJobs(@RequestParam(defaultValue = "30") int daysOld) {
        try {
            List<Job> jobsForCleanup = jobService.findJobsForCleanup(daysOld);
            int deletedCount = 0;
            
            for (Job job : jobsForCleanup) {
                jobService.deleteJob(job.getId());
                deletedCount++;
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("message", "Cleaned up " + deletedCount + " old jobs");
            result.put("deletedCount", deletedCount);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to cleanup old jobs");
            error.put("message", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    @GetMapping("/statistics")
    public ResponseEntity<?> getSystemStatistics() {
        try {
            Map<String, Object> stats = new HashMap<>();
            
            // Job statistics
            stats.put("totalJobs", jobService.countJobsByStatus(com.Prod.Chronos.entity.JobStatus.SCHEDULED) + 
                               jobService.countJobsByStatus(com.Prod.Chronos.entity.JobStatus.RUNNING) + 
                               jobService.countJobsByStatus(com.Prod.Chronos.entity.JobStatus.COMPLETED) + 
                               jobService.countJobsByStatus(com.Prod.Chronos.entity.JobStatus.FAILED));
            stats.put("scheduledJobs", jobService.countJobsByStatus(com.Prod.Chronos.entity.JobStatus.SCHEDULED));
            stats.put("runningJobs", jobService.countJobsByStatus(com.Prod.Chronos.entity.JobStatus.RUNNING));
            stats.put("completedJobs", jobService.countJobsByStatus(com.Prod.Chronos.entity.JobStatus.COMPLETED));
            stats.put("failedJobs", jobService.countJobsByStatus(com.Prod.Chronos.entity.JobStatus.FAILED));
            
            // Job type statistics
            stats.put("oneTimeJobs", jobService.countJobsByType(com.Prod.Chronos.entity.JobType.ONE_TIME));
            stats.put("recurringJobs", jobService.countJobsByType(com.Prod.Chronos.entity.JobType.RECURRING));
            stats.put("batchJobs", jobService.countJobsByType(com.Prod.Chronos.entity.JobType.BATCH));
            
            // System health
            List<Job> stuckJobs = jobService.findStuckRunningJobs(30);
            stats.put("stuckJobs", stuckJobs.size());
            
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve system statistics");
            error.put("message", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
}
