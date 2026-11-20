package com.atm.notificationservice;

public interface NotificationSender {
    NotificationType getType();

    void send(Notification request) throws Exception;
}