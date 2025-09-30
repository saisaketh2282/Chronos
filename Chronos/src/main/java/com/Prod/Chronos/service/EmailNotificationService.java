package com.Prod.Chronos.service;

import com.Prod.Chronos.entity.Job;
import com.Prod.Chronos.entity.JobStatus;
import com.Prod.Chronos.entity.LogLevel;
import com.Prod.Chronos.repository.ExecutionLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Service
public class EmailNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(EmailNotificationService.class);

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private ExecutionLogRepository executionLogRepository;

    @Autowired
    @Qualifier("notificationExecutor")
    private Executor notificationExecutor;

    @Async("notificationExecutor")
    public CompletableFuture<Void> sendJobCompletionNotification(Job job, String recipientEmail) {
        return CompletableFuture.runAsync(() -> {
            try {
                String subject = String.format("Job Completed: %s", job.getName());
                String content = buildJobCompletionEmailContent(job);
                
                sendHtmlEmail(recipientEmail, subject, content);
                
                logger.info("Sent job completion notification for job: {} to: {}", 
                           job.getName(), recipientEmail);
            } catch (Exception e) {
                logger.error("Error sending job completion notification for job: {}", job.getId(), e);
            }
        }, notificationExecutor);
    }

    @Async("notificationExecutor")
    public CompletableFuture<Void> sendJobFailureNotification(Job job, String recipientEmail) {
        return CompletableFuture.runAsync(() -> {
            try {
                String subject = String.format("Job Failed: %s", job.getName());
                String content = buildJobFailureEmailContent(job);
                
                sendHtmlEmail(recipientEmail, subject, content);
                
                logger.info("Sent job failure notification for job: {} to: {}", 
                           job.getName(), recipientEmail);
            } catch (Exception e) {
                logger.error("Error sending job failure notification for job: {}", job.getId(), e);
            }
        }, notificationExecutor);
    }

    @Async("notificationExecutor")
    public CompletableFuture<Void> sendBatchJobSummary(List<Job> jobs, String recipientEmail) {
        return CompletableFuture.runAsync(() -> {
            try {
                String subject = "Batch Job Execution Summary";
                String content = buildBatchJobSummaryEmailContent(jobs);
                
                sendHtmlEmail(recipientEmail, subject, content);
                
                logger.info("Sent batch job summary for {} jobs to: {}", 
                           jobs.size(), recipientEmail);
            } catch (Exception e) {
                logger.error("Error sending batch job summary", e);
            }
        }, notificationExecutor);
    }

    @Async("notificationExecutor")
    public CompletableFuture<Void> sendSystemAlert(String alertType, String message, 
                                                  String recipientEmail, Map<String, Object> additionalData) {
        return CompletableFuture.runAsync(() -> {
            try {
                String subject = String.format("Chronos System Alert: %s", alertType);
                String content = buildSystemAlertEmailContent(alertType, message, additionalData);
                
                sendHtmlEmail(recipientEmail, subject, content);
                
                logger.info("Sent system alert: {} to: {}", alertType, recipientEmail);
            } catch (Exception e) {
                logger.error("Error sending system alert: {}", alertType, e);
            }
        }, notificationExecutor);
    }

    private void sendHtmlEmail(String to, String subject, String content) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(content, true); // true indicates HTML content
        
        mailSender.send(message);
    }

    private void sendSimpleEmail(String to, String subject, String content) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(content);
        
        mailSender.send(message);
    }

    private String buildJobCompletionEmailContent(Job job) {
        StringBuilder content = new StringBuilder();
        content.append("<html><body>");
        content.append("<h2>✅ Job Completed Successfully</h2>");
        content.append("<table border='1' style='border-collapse: collapse; width: 100%;'>");
        content.append("<tr><td><strong>Job Name:</strong></td><td>").append(job.getName()).append("</td></tr>");
        content.append("<tr><td><strong>Job ID:</strong></td><td>").append(job.getId()).append("</td></tr>");
        content.append("<tr><td><strong>Job Type:</strong></td><td>").append(job.getJobType()).append("</td></tr>");
        content.append("<tr><td><strong>Status:</strong></td><td>").append(job.getStatus()).append("</td></tr>");
        content.append("<tr><td><strong>Created By:</strong></td><td>").append(job.getCreatedBy()).append("</td></tr>");
        
        if (job.getScheduledAt() != null) {
            content.append("<tr><td><strong>Scheduled At:</strong></td><td>")
                   .append(job.getScheduledAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                   .append("</td></tr>");
        }
        
        if (job.getStartedAt() != null) {
            content.append("<tr><td><strong>Started At:</strong></td><td>")
                   .append(job.getStartedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                   .append("</td></tr>");
        }
        
        if (job.getCompletedAt() != null) {
            content.append("<tr><td><strong>Completed At:</strong></td><td>")
                   .append(job.getCompletedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                   .append("</td></tr>");
        }
        
        content.append("</table>");
        
        if (job.getDescription() != null && !job.getDescription().trim().isEmpty()) {
            content.append("<h3>Description:</h3>");
            content.append("<p>").append(job.getDescription()).append("</p>");
        }
        
        content.append("<p><em>This is an automated notification from Chronos Job Scheduler.</em></p>");
        content.append("</body></html>");
        
        return content.toString();
    }

    private String buildJobFailureEmailContent(Job job) {
        StringBuilder content = new StringBuilder();
        content.append("<html><body>");
        content.append("<h2>❌ Job Failed</h2>");
        content.append("<table border='1' style='border-collapse: collapse; width: 100%;'>");
        content.append("<tr><td><strong>Job Name:</strong></td><td>").append(job.getName()).append("</td></tr>");
        content.append("<tr><td><strong>Job ID:</strong></td><td>").append(job.getId()).append("</td></tr>");
        content.append("<tr><td><strong>Job Type:</strong></td><td>").append(job.getJobType()).append("</td></tr>");
        content.append("<tr><td><strong>Status:</strong></td><td>").append(job.getStatus()).append("</td></tr>");
        content.append("<tr><td><strong>Created By:</strong></td><td>").append(job.getCreatedBy()).append("</td></tr>");
        content.append("<tr><td><strong>Retry Count:</strong></td><td>")
               .append(job.getCurrentRetryCount()).append("/").append(job.getMaxRetries()).append("</td></tr>");
        
        if (job.getScheduledAt() != null) {
            content.append("<tr><td><strong>Scheduled At:</strong></td><td>")
                   .append(job.getScheduledAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                   .append("</td></tr>");
        }
        
        if (job.getStartedAt() != null) {
            content.append("<tr><td><strong>Started At:</strong></td><td>")
                   .append(job.getStartedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                   .append("</td></tr>");
        }
        
        if (job.getCompletedAt() != null) {
            content.append("<tr><td><strong>Failed At:</strong></td><td>")
                   .append(job.getCompletedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                   .append("</td></tr>");
        }
        
        content.append("</table>");
        
        if (job.getErrorMessage() != null && !job.getErrorMessage().trim().isEmpty()) {
            content.append("<h3>Error Message:</h3>");
            content.append("<div style='background-color: #f8d7da; border: 1px solid #f5c6cb; padding: 10px; border-radius: 4px;'>");
            content.append("<pre>").append(job.getErrorMessage()).append("</pre>");
            content.append("</div>");
        }
        
        if (job.getDescription() != null && !job.getDescription().trim().isEmpty()) {
            content.append("<h3>Description:</h3>");
            content.append("<p>").append(job.getDescription()).append("</p>");
        }
        
        content.append("<p><em>This is an automated notification from Chronos Job Scheduler.</em></p>");
        content.append("</body></html>");
        
        return content.toString();
    }

    private String buildBatchJobSummaryEmailContent(List<Job> jobs) {
        StringBuilder content = new StringBuilder();
        content.append("<html><body>");
        content.append("<h2>📊 Batch Job Execution Summary</h2>");
        
        // Count jobs by status
        long completed = jobs.stream().filter(j -> j.getStatus() == JobStatus.COMPLETED).count();
        long failed = jobs.stream().filter(j -> j.getStatus() == JobStatus.FAILED).count();
        long total = jobs.size();
        
        content.append("<table border='1' style='border-collapse: collapse; width: 100%; margin-bottom: 20px;'>");
        content.append("<tr><th>Total Jobs</th><th>✅ Completed</th><th>❌ Failed</th><th>Success Rate</th></tr>");
        content.append("<tr><td>").append(total).append("</td>");
        content.append("<td>").append(completed).append("</td>");
        content.append("<td>").append(failed).append("</td>");
        content.append("<td>").append(String.format("%.1f%%", (double) completed / total * 100)).append("</td></tr>");
        content.append("</table>");
        
        // Job details
        content.append("<h3>Job Details:</h3>");
        content.append("<table border='1' style='border-collapse: collapse; width: 100%;'>");
        content.append("<tr><th>Job Name</th><th>Status</th><th>Started At</th><th>Completed At</th><th>Error</th></tr>");
        
        for (Job job : jobs) {
            content.append("<tr>");
            content.append("<td>").append(job.getName()).append("</td>");
            content.append("<td>").append(job.getStatus()).append("</td>");
            content.append("<td>").append(job.getStartedAt() != null ? 
                job.getStartedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "N/A").append("</td>");
            content.append("<td>").append(job.getCompletedAt() != null ? 
                job.getCompletedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "N/A").append("</td>");
            content.append("<td>").append(job.getErrorMessage() != null ? 
                job.getErrorMessage().substring(0, Math.min(50, job.getErrorMessage().length())) + "..." : "N/A").append("</td>");
            content.append("</tr>");
        }
        
        content.append("</table>");
        content.append("<p><em>This is an automated notification from Chronos Job Scheduler.</em></p>");
        content.append("</body></html>");
        
        return content.toString();
    }

    private String buildSystemAlertEmailContent(String alertType, String message, Map<String, Object> additionalData) {
        StringBuilder content = new StringBuilder();
        content.append("<html><body>");
        content.append("<h2>🚨 System Alert: ").append(alertType).append("</h2>");
        content.append("<div style='background-color: #fff3cd; border: 1px solid #ffeaa7; padding: 15px; border-radius: 4px; margin-bottom: 20px;'>");
        content.append("<p><strong>Message:</strong> ").append(message).append("</p>");
        content.append("<p><strong>Timestamp:</strong> ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("</p>");
        content.append("</div>");
        
        if (additionalData != null && !additionalData.isEmpty()) {
            content.append("<h3>Additional Information:</h3>");
            content.append("<table border='1' style='border-collapse: collapse; width: 100%;'>");
            for (Map.Entry<String, Object> entry : additionalData.entrySet()) {
                content.append("<tr><td><strong>").append(entry.getKey()).append("</strong></td><td>").append(entry.getValue()).append("</td></tr>");
            }
            content.append("</table>");
        }
        
        content.append("<p><em>This is an automated alert from Chronos Job Scheduler.</em></p>");
        content.append("</body></html>");
        
        return content.toString();
    }
}
