package com.atm.notificationservice;

import com.atm.notificationservice.event.CardEvent;
import com.atm.notificationservice.event.CashLowEvent;
import com.atm.notificationservice.event.TransactionCompletedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationEventListener {

    private static final Logger log = LoggerFactory.getLogger(NotificationEventListener.class);

    private final NotificationRoutingService routingService;
    private final String adminEmail;

    public NotificationEventListener(NotificationRoutingService routingService,
                                     @Value("${notification.admin-email:}") String adminEmail) {
        this.routingService = routingService;
        this.adminEmail = adminEmail;
    }

    @KafkaListener(topics = "${atm.kafka.topics.transaction-completed}")
    public void onTransactionCompleted(TransactionCompletedEvent event) {
        log.info("Received transaction event: {}", event);
        String message = event.type() + " of " + event.amount() + " on account " + event.accountId()
                + (event.relatedAccountId() != null ? " (related account " + event.relatedAccountId() + ")" : "")
                + ", transaction #" + event.transactionId();
        routingService.routeNotification(new Notification(
                NotificationType.WEBSOCKET,
                "account-" + event.accountId(),
                "Transaction completed",
                message));
    }

    @KafkaListener(topics = "${atm.kafka.topics.cash-low}")
    public void onCashLow(CashLowEvent event) {
        log.warn("Received cash-low event: {}", event);
        String message = "ATM " + event.atmId() + ": denomination " + event.denomination()
                + " is low (" + event.remaining() + " left, threshold " + event.threshold() + ")";
        routingService.routeNotification(new Notification(
                NotificationType.WEBSOCKET, "admin", "Low cash alert", message));
        if (adminEmail != null && !adminEmail.isBlank()) {
            routingService.routeNotification(new Notification(
                    NotificationType.EMAIL, adminEmail, "Low cash alert", message));
        }
    }

    @KafkaListener(topics = "${atm.kafka.topics.card-events}")
    public void onCardEvent(CardEvent event) {
        log.info("Received card event: {}", event);
        String message = "Card " + event.maskedCardNumber() + ": " + event.eventType();
        routingService.routeNotification(new Notification(
                NotificationType.WEBSOCKET,
                "customer-" + event.customerId(),
                "Card update",
                message));
    }
}