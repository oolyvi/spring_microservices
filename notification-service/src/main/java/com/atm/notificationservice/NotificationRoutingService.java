package com.atm.notificationservice;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class NotificationRoutingService {

    private final Map<NotificationType, NotificationSender> senders = new HashMap<>();

    public NotificationRoutingService(List<NotificationSender> senderList) {
        for (NotificationSender sender : senderList) {
            senders.put(sender.getType(), sender);
        }
    }

    public void routeNotification(Notification request) {
        NotificationSender sender = senders.get(request.getType());
        if (sender == null) {
            throw new IllegalArgumentException("No channel found for: " + request.getType());
        }
        try {
            sender.send(request);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to send " + request.getType() + ": " + e.getMessage(), e);
        }
    }
}