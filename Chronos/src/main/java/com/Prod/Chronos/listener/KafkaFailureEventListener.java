package com.Prod.Chronos.listener;

import com.Prod.Chronos.entity.Job;
import com.Prod.Chronos.service.JobService;
import com.Prod.Chronos.service.KafkaService;
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

@Component
public class KafkaFailureEventListener {

    private static final Logger logger = LoggerFactory.getLogger(KafkaFailureEventListener.class);

    @Autowired
    private JobService jobService;

    @Autowired
    private KafkaService kafkaService;

    @Autowired
    private ObjectMapper objectMapper;

    @KafkaListener(topics = "chronos.failure.events", groupId = "chronos-failure-consumer")
    public void handleFailureEvent(@Payload String message,
                                   @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                   @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                                   @Header(KafkaHeaders.OFFSET) long offset,
                                   Acknowledgment acknowledgment) {
        try {
            logger.info("Received failure event from topic: {}, partition: {}, offset: {}", 
                       topic, partition, offset);

            Map<String, Object> failureEvent = objectMapper.readValue(message, Map.class);
            Long jobId = Long.valueOf(failureEvent.get("jobId").toString());
            String errorReason = failureEvent.get("errorReason").toString();
            Integer retryCount = Integer.valueOf(failureEvent.get("retryCount").toString());
            Integer maxRetries = Integer.valueOf(failureEvent.get("maxRetries").toString());
            Boolean canRetry = Boolean.valueOf(failureEvent.get("canRetry").toString());

            logger.info("Processing failure event for job: {} (retry: {}/{})", 
                       jobId, retryCount, maxRetries);

            Job job = jobService.findById(jobId).orElse(null);
            if (job == null) {
                logger.error("Job not found for failure event: {}", jobId);
                acknowledgment.acknowledge();
                return;
            }

            if (canRetry) {
                // Send to retry pipeline with delay
                kafkaService.sendRetryEvent(job);
                logger.info("Job {} sent to retry pipeline", jobId);
            } else {
                // Send to dead letter queue
                kafkaService.sendToDeadLetterQueue(job, "Max retries exceeded");
                logger.warn("Job {} sent to dead letter queue (max retries exceeded)", jobId);
            }

            acknowledgment.acknowledge();
        } catch (Exception e) {
            logger.error("Error processing failure event: {}", message, e);
            // Don't acknowledge on error to allow retry
        }
    }
}
