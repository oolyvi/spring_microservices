package com.atm.notificationservice;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic transactionCompletedTopic(@Value("${atm.kafka.topics.transaction-completed}") String name) {
        return TopicBuilder.name(name).partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic cashLowTopic(@Value("${atm.kafka.topics.cash-low}") String name) {
        return TopicBuilder.name(name).partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic cardEventsTopic(@Value("${atm.kafka.topics.card-events}") String name) {
        return TopicBuilder.name(name).partitions(1).replicas(1).build();
    }
}