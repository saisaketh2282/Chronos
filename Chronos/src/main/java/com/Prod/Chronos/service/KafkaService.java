package com.Prod.Chronos.service;

import com.Prod.Chronos.entity.Job;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class KafkaService {

    private static final Logger logger = LoggerFactory.getLogger(KafkaService.class);

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String FAILURE_EVENTS_TOPIC = "chronos.failure.events";
    private static final String RETRY_PIPELINE_TOPIC = "chronos.retry.pipeline";
    private static final String DEAD_LETTER_QUEUE_TOPIC = "chronos.dead.letter.queue";

    public void sendFailureEvent(Job job, String errorReason) {
        try {
            Map<String, Object> failureEvent = new HashMap<>();
            failureEvent.put("jobId", job.getId());
            failureEvent.put("jobName", job.getName());
            failureEvent.put("errorReason", errorReason);
            failureEvent.put("retryCount", job.getCurrentRetryCount());
            failureEvent.put("maxRetries", job.getMaxRetries());
            failureEvent.put("timestamp", LocalDateTime.now());
            failureEvent.put("canRetry", job.canRetry());

            String message = objectMapper.writeValueAsString(failureEvent);
            
            kafkaTemplate.send(FAILURE_EVENTS_TOPIC, String.valueOf(job.getId()), message);
            
            logger.info("Sent failure event to Kafka for job: {} (ID: {})", job.getName(), job.getId());
        } catch (JsonProcessingException e) {
            logger.error("Error serializing failure event for job: {}", job.getId(), e);
        } catch (Exception e) {
            logger.error("Error sending failure event to Kafka for job: {}", job.getId(), e);
        }
    }

    public void sendRetryEvent(Job job) {
        try {
            Map<String, Object> retryEvent = new HashMap<>();
            retryEvent.put("jobId", job.getId());
            retryEvent.put("jobName", job.getName());
            retryEvent.put("retryCount", job.getCurrentRetryCount());
            retryEvent.put("maxRetries", job.getMaxRetries());
            retryEvent.put("timestamp", LocalDateTime.now());
            retryEvent.put("retryDelay", calculateRetryDelay(job.getCurrentRetryCount()));

            String message = objectMapper.writeValueAsString(retryEvent);
            
            kafkaTemplate.send(RETRY_PIPELINE_TOPIC, String.valueOf(job.getId()), message);
            
            logger.info("Sent retry event to Kafka for job: {} (ID: {})", job.getName(), job.getId());
        } catch (JsonProcessingException e) {
            logger.error("Error serializing retry event for job: {}", job.getId(), e);
        } catch (Exception e) {
            logger.error("Error sending retry event to Kafka for job: {}", job.getId(), e);
        }
    }

    public void sendToDeadLetterQueue(Job job, String reason) {
        try {
            Map<String, Object> deadLetterEvent = new HashMap<>();
            deadLetterEvent.put("jobId", job.getId());
            deadLetterEvent.put("jobName", job.getName());
            deadLetterEvent.put("reason", reason);
            deadLetterEvent.put("finalRetryCount", job.getCurrentRetryCount());
            deadLetterEvent.put("maxRetries", job.getMaxRetries());
            deadLetterEvent.put("timestamp", LocalDateTime.now());
            deadLetterEvent.put("payload", job.getPayload());
            deadLetterEvent.put("errorMessage", job.getErrorMessage());

            String message = objectMapper.writeValueAsString(deadLetterEvent);
            
            kafkaTemplate.send(DEAD_LETTER_QUEUE_TOPIC, String.valueOf(job.getId()), message);
            
            logger.warn("Sent job to dead letter queue: {} (ID: {}) - Reason: {}", 
                       job.getName(), job.getId(), reason);
        } catch (JsonProcessingException e) {
            logger.error("Error serializing dead letter event for job: {}", job.getId(), e);
        } catch (Exception e) {
            logger.error("Error sending job to dead letter queue: {}", job.getId(), e);
        }
    }

    private long calculateRetryDelay(int retryCount) {
        // Exponential backoff: 5s, 10s, 20s, 40s, etc.
        return Math.min(5000L * (1L << retryCount), 300000L); // Max 5 minutes
    }

    public void sendJobEvent(Job job, String eventType, String message) {
        try {
            Map<String, Object> jobEvent = new HashMap<>();
            jobEvent.put("jobId", job.getId());
            jobEvent.put("jobName", job.getName());
            jobEvent.put("eventType", eventType);
            jobEvent.put("message", message);
            jobEvent.put("timestamp", LocalDateTime.now());
            jobEvent.put("status", job.getStatus());
            jobEvent.put("jobType", job.getJobType());

            String eventMessage = objectMapper.writeValueAsString(jobEvent);
            
            kafkaTemplate.send("chronos.job.events", String.valueOf(job.getId()), eventMessage);
            
            logger.debug("Sent job event to Kafka: {} for job: {} (ID: {})", 
                        eventType, job.getName(), job.getId());
        } catch (JsonProcessingException e) {
            logger.error("Error serializing job event for job: {}", job.getId(), e);
        } catch (Exception e) {
            logger.error("Error sending job event to Kafka for job: {}", job.getId(), e);
        }
    }
}
