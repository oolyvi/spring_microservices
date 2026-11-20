package com.atm.transactionservice.event;

import com.atm.transactionservice.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class TransactionEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(TransactionEventPublisher.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String topic;

    public TransactionEventPublisher(KafkaTemplate<String, Object> kafkaTemplate,
                                     @Value("${atm.kafka.topics.transaction-completed}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    public void publishCompleted(Transaction transaction) {
        try {
            TransactionCompletedEvent event = new TransactionCompletedEvent(
                    transaction.getId(),
                    transaction.getType().name(),
                    transaction.getAmount(),
                    transaction.getAccountId(),
                    transaction.getRelatedAccountId(),
                    transaction.getOriginalTransactionId(),
                    Instant.now().toString()
            );
            kafkaTemplate.send(topic, String.valueOf(transaction.getAccountId()), event)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.error("Failed to publish event for transaction {}", transaction.getId(), ex);
                        }
                    });
        } catch (Exception e) {
            log.error("Failed to publish event for transaction {}", transaction.getId(), e);
        }
    }
}