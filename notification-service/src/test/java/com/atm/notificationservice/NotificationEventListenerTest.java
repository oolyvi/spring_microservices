package com.atm.notificationservice;

import com.atm.notificationservice.event.CardEvent;
import com.atm.notificationservice.event.CashLowEvent;
import com.atm.notificationservice.event.TransactionCompletedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationEventListenerTest {

    @Mock
    private NotificationRoutingService routingService;

    @Test
    void transactionEvent_isSentOverWebSocketToTheAccountTopic() {
        NotificationEventListener listener = new NotificationEventListener(routingService, "");

        listener.onTransactionCompleted(new TransactionCompletedEvent(
                7L, "DEBIT", new BigDecimal("150.00"), 1L, null, null, "2026-01-01T00:00:00Z"));

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(routingService).routeNotification(captor.capture());
        Notification sent = captor.getValue();
        assertEquals(NotificationType.WEBSOCKET, sent.getType());
        assertEquals("account-1", sent.getRecipient());
        assertTrue(sent.getMessage().contains("150.00"));
    }

    @Test
    void cashLowEvent_alertsAdminOverWebSocketOnly_whenNoAdminEmailConfigured() {
        NotificationEventListener listener = new NotificationEventListener(routingService, "");

        listener.onCashLow(new CashLowEvent(1L, 500, 4, 5, "2026-01-01T00:00:00Z"));

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(routingService, times(1)).routeNotification(captor.capture());
        assertEquals(NotificationType.WEBSOCKET, captor.getValue().getType());
        assertEquals("admin", captor.getValue().getRecipient());
    }

    @Test
    void cashLowEvent_alsoEmailsAdmin_whenAdminEmailConfigured() {
        NotificationEventListener listener = new NotificationEventListener(routingService, "ops@atm.com");

        listener.onCashLow(new CashLowEvent(1L, 500, 4, 5, "2026-01-01T00:00:00Z"));

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(routingService, times(2)).routeNotification(captor.capture());
        assertEquals(NotificationType.EMAIL, captor.getAllValues().get(1).getType());
        assertEquals("ops@atm.com", captor.getAllValues().get(1).getRecipient());
    }

    @Test
    void cardEvent_isSentToTheCustomerTopic() {
        NotificationEventListener listener = new NotificationEventListener(routingService, "");

        listener.onCardEvent(new CardEvent(3L, "****-****-****-8812", 2L, 5L, "BLOCKED", "2026-01-01T00:00:00Z"));

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(routingService).routeNotification(captor.capture());
        assertEquals("customer-2", captor.getValue().getRecipient());
        assertTrue(captor.getValue().getMessage().contains("BLOCKED"));
    }
}