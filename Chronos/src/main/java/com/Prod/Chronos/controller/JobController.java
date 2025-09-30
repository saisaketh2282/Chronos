package com.Prod.Chronos.controller;

import com.Prod.Chronos.entity.*;
import com.Prod.Chronos.service.JobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/jobs")
@CrossOrigin(origins = "*")
public class JobController {

    @Autowired
    private JobService jobService;

    @PostMapping
    public ResponseEntity<?> createJob(@Valid @RequestBody CreateJobRequest request, Authentication authentication) {
        try {
            String createdBy = authentication.getName();
            Job job;

            if (request.getJobType() == JobType.ONE_TIME) {
                if (request.getScheduledAt() == null) {
                    return ResponseEntity.badRequest().body(Map.of("error", "Scheduled time is required for one-time jobs"));
                }
                job = jobService.createOneTimeJob(
                    request.getName(),
                    request.getDescription(),
                    request.getPayload(),
                    request.getScheduledAt(),
                    createdBy
                );
            } else if (request.getJobType() == JobType.RECURRING) {
                if (request.getCronExpression() == null || request.getCronExpression().trim().isEmpty()) {
                    return ResponseEntity.badRequest().body(Map.of("error", "Cron expression is required for recurring jobs"));
                }
                job = jobService.createRecurringJob(
                    request.getName(),
                    request.getDescription(),
                    request.getPayload(),
                    request.getCronExpression(),
                    createdBy
                );
            } else {
                job = jobService.createJob(new Job(
                    request.getName(),
                    request.getDescription(),
                    request.getJobType(),
                    request.getPayload(),
                    createdBy
                ));
            }

            return ResponseEntity.status(HttpStatus.CREATED).body(job);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to create job");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllJobs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) JobStatus status,
            @RequestParam(required = false) String createdBy) {
        
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);

            Page<Job> jobs;
            if (status != null && createdBy != null) {
                jobs = jobService.findByStatus(status, pageable);
                // Filter by createdBy in memory (in production, this should be done in the query)
                jobs = jobs.map(job -> job.getCreatedBy().equals(createdBy) ? job : null);
            } else if (status != null) {
                jobs = jobService.findByStatus(status, pageable);
            } else if (createdBy != null) {
                jobs = jobService.findByCreatedBy(createdBy, pageable);
            } else {
                jobs = jobService.findAll(pageable);
            }

            return ResponseEntity.ok(jobs);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve jobs");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getJobById(@PathVariable Long id, @RequestParam(defaultValue = "false") boolean includeLogs) {
        try {
            Optional<Job> jobOpt;
            if (includeLogs) {
                jobOpt = jobService.findByIdWithScheduleAndLogs(id);
            } else {
                jobOpt = jobService.findByIdWithSchedule(id);
            }

            if (jobOpt.isPresent()) {
                return ResponseEntity.ok(jobOpt.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve job");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateJob(@PathVariable Long id, @Valid @RequestBody UpdateJobRequest request) {
        try {
            Optional<Job> jobOpt = jobService.findById(id);
            if (jobOpt.isPresent()) {
                Job job = jobOpt.get();
                
                // Only allow updates to certain fields
                if (request.getName() != null) job.setName(request.getName());
                if (request.getDescription() != null) job.setDescription(request.getDescription());
                if (request.getPayload() != null) job.setPayload(request.getPayload());
                if (request.getPriority() != null) job.setPriority(request.getPriority());
                if (request.getMaxRetries() != null) job.setMaxRetries(request.getMaxRetries());

                Job updatedJob = jobService.updateJob(job);
                return ResponseEntity.ok(updatedJob);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update job");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteJob(@PathVariable Long id) {
        try {
            Optional<Job> jobOpt = jobService.findById(id);
            if (jobOpt.isPresent()) {
                jobService.deleteJob(id);
                return ResponseEntity.ok(Map.of("message", "Job deleted successfully"));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to delete job");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<?> cancelJob(@PathVariable Long id) {
        try {
            Job cancelledJob = jobService.cancelJob(id);
            if (cancelledJob != null) {
                return ResponseEntity.ok(cancelledJob);
            } else {
                return ResponseEntity.badRequest().body(Map.of("error", "Job cannot be cancelled or not found"));
            }
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to cancel job");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/{id}/logs")
    public ResponseEntity<?> getJobLogs(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        try {
            Optional<Job> jobOpt = jobService.findById(id);
            if (jobOpt.isPresent()) {
                Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
                Page<ExecutionLog> logs = jobService.getExecutionLogs(id, pageable);
                return ResponseEntity.ok(logs);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve job logs");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<?> getJobsByStatus(@PathVariable JobStatus status) {
        try {
            List<Job> jobs = jobService.findByStatus(status);
            return ResponseEntity.ok(jobs);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve jobs by status");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/statistics")
    public ResponseEntity<?> getJobStatistics() {
        try {
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalJobs", jobService.countJobsByStatus(JobStatus.SCHEDULED) + 
                               jobService.countJobsByStatus(JobStatus.RUNNING) + 
                               jobService.countJobsByStatus(JobStatus.COMPLETED) + 
                               jobService.countJobsByStatus(JobStatus.FAILED));
            stats.put("scheduledJobs", jobService.countJobsByStatus(JobStatus.SCHEDULED));
            stats.put("runningJobs", jobService.countJobsByStatus(JobStatus.RUNNING));
            stats.put("completedJobs", jobService.countJobsByStatus(JobStatus.COMPLETED));
            stats.put("failedJobs", jobService.countJobsByStatus(JobStatus.FAILED));
            stats.put("oneTimeJobs", jobService.countJobsByType(JobType.ONE_TIME));
            stats.put("recurringJobs", jobService.countJobsByType(JobType.RECURRING));
            stats.put("batchJobs", jobService.countJobsByType(JobType.BATCH));

            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve job statistics");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // Request DTOs
    public static class CreateJobRequest {
        private String name;
        private String description;
        private JobType jobType;
        private String payload;
        private LocalDateTime scheduledAt;
        private String cronExpression;
        private Integer priority;
        private Integer maxRetries;

        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public JobType getJobType() { return jobType; }
        public void setJobType(JobType jobType) { this.jobType = jobType; }
        public String getPayload() { return payload; }
        public void setPayload(String payload) { this.payload = payload; }
        public LocalDateTime getScheduledAt() { return scheduledAt; }
        public void setScheduledAt(LocalDateTime scheduledAt) { this.scheduledAt = scheduledAt; }
        public String getCronExpression() { return cronExpression; }
        public void setCronExpression(String cronExpression) { this.cronExpression = cronExpression; }
        public Integer getPriority() { return priority; }
        public void setPriority(Integer priority) { this.priority = priority; }
        public Integer getMaxRetries() { return maxRetries; }
        public void setMaxRetries(Integer maxRetries) { this.maxRetries = maxRetries; }
    }

    public static class UpdateJobRequest {
        private String name;
        private String description;
        private String payload;
        private Integer priority;
        private Integer maxRetries;

        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getPayload() { return payload; }
        public void setPayload(String payload) { this.payload = payload; }
        public Integer getPriority() { return priority; }
        public void setPriority(Integer priority) { this.priority = priority; }
        public Integer getMaxRetries() { return maxRetries; }
        public void setMaxRetries(Integer maxRetries) { this.maxRetries = maxRetries; }
    }
}
