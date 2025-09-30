package com.Prod.Chronos.listener;

import com.Prod.Chronos.entity.Job;
import com.Prod.Chronos.service.JobService;
import com.Prod.Chronos.service.JobExecutorService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Component
public class KafkaRetryEventListener {

    private static final Logger logger = LoggerFactory.getLogger(KafkaRetryEventListener.class);

    @Autowired
    private JobService jobService;

    @Autowired
    private JobExecutorService jobExecutorService;

    @Autowired
    private ObjectMapper objectMapper;

    @KafkaListener(topics = "chronos.retry.pipeline", 
                   groupId = "chronos-retry-consumer",
                   containerFactory = "retryKafkaListenerContainerFactory")
    public void handleRetryEvent(@Payload String message,
                                 @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                 @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                                 @Header(KafkaHeaders.OFFSET) long offset,
                                 Acknowledgment acknowledgment) {
        try {
            logger.info("Received retry event from topic: {}, partition: {}, offset: {}", 
                       topic, partition, offset);

            Map<String, Object> retryEvent = objectMapper.readValue(message, Map.class);
            Long jobId = Long.valueOf(retryEvent.get("jobId").toString());
            String jobName = retryEvent.get("jobName").toString();
            Integer retryCount = Integer.valueOf(retryEvent.get("retryCount").toString());
            Long retryDelay = Long.valueOf(retryEvent.get("retryDelay").toString());

            logger.info("Processing retry event for job: {} (ID: {}, attempt: {})", 
                       jobName, jobId, retryCount);

            Job job = jobService.findById(jobId).orElse(null);
            if (job == null) {
                logger.error("Job not found for retry event: {}", jobId);
                acknowledgment.acknowledge();
                return;
            }

            // Apply retry delay
            if (retryDelay > 0) {
                logger.info("Waiting {}ms before retrying job: {}", retryDelay, jobId);
                Thread.sleep(retryDelay);
            }

            // Execute retry asynchronously
            CompletableFuture<Void> retryFuture = jobExecutorService.retryJob(jobId);
            
            // Don't wait for completion, let it run asynchronously
            retryFuture.whenComplete((result, throwable) -> {
                if (throwable != null) {
                    logger.error("Error during retry execution for job: {}", jobId, throwable);
                } else {
                    logger.info("Retry execution completed for job: {}", jobId);
                }
            });

            acknowledgment.acknowledge();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Retry processing interrupted for message: {}", message, e);
        } catch (Exception e) {
            logger.error("Error processing retry event: {}", message, e);
            // Don't acknowledge on error to allow retry
        }
    }
}
