package com.atm.cashservice.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class CashEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(CashEventPublisher.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String topic;

    public CashEventPublisher(KafkaTemplate<String, Object> kafkaTemplate,
                              @Value("${atm.kafka.topics.cash-low}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    public void publishCashLow(Long atmId, int denomination, int remaining, int threshold) {
        try {
            CashLowEvent event = new CashLowEvent(
                    atmId, denomination, remaining, threshold, Instant.now().toString());
            kafkaTemplate.send(topic, String.valueOf(atmId), event)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.error("Failed to publish cash-low event for ATM {}", atmId, ex);
                        }
                    });
        } catch (Exception e) {
            log.error("Failed to publish cash-low event for ATM {}", atmId, e);
        }
    }
}