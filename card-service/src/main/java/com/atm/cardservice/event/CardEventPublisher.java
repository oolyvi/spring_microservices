package com.atm.cardservice.event;

import com.atm.cardservice.Card;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class CardEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(CardEventPublisher.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String topic;

    public CardEventPublisher(KafkaTemplate<String, Object> kafkaTemplate,
                              @Value("${atm.kafka.topics.card-events}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    public void publish(Card card, String eventType) {
        try {
            CardEvent event = new CardEvent(
                    card.getId(),
                    Card.maskCardNumber(card.getCardNumber()),
                    card.getCustomerId(),
                    card.getAccountId(),
                    eventType,
                    Instant.now().toString()
            );
            kafkaTemplate.send(topic, String.valueOf(card.getId()), event)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.error("Failed to publish {} for card {}", eventType, card.getId(), ex);
                        }
                    });
        } catch (Exception e) {
            log.error("Failed to publish {} for card {}", eventType, card.getId(), e);
        }
    }
}