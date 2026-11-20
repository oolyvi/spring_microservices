# Notification Service

Microservice responsible for consuming platform events, routing notifications, and delivering alerts via email and
WebSocket within the ATM banking platform. Built with Java and Spring Boot using Maven, with Apache Kafka consumer/topic
support, WebSocket configuration, and Docker support.

## Architecture & Routing

- **Service**: `notification-service` (`com.atm.notificationservice`)
- **API Gateway**: Exposed through the API Gateway on port **`8765`** (`http://localhost:8765`) for external/client
  routing, service routing, and edge concerns.
- **Event Consumption (Kafka)**: Listens to domain events under `com.atm.notificationservice.event` (`CashLowEvent`,
  `TransactionCompletedEvent`, `CardEvent`) via `NotificationEventListener` with topic setup in `KafkaTopicConfig`.
- **Notification Delivery**: Routes and dispatches notifications via `NotificationRoutingService` and
  `NotificationSender` implementations (`EmailNotificationSender`, `WebSocketNotificationSender`) configured with
  `WebSocketConfig`.

## Project Structure

```text
notification-service/
├── Dockerfile
├── .dockerignore
├── pom.xml
├── README.md
└── src/
    ├── main/
    │   ├── java/com/atm/notificationservice/
    │   │   ├── NotificationServiceApplication.java
    │   │   ├── NotificationController.java
    │   │   ├── NotificationRoutingService.java
    │   │   ├── Notification.java
    │   │   ├── NotificationType.java
    │   │   ├── NotificationSender.java
    │   │   ├── EmailNotificationSender.java
    │   │   ├── WebSocketNotificationSender.java
    │   │   ├── NotificationEventListener.java
    │   │   ├── KafkaTopicConfig.java
    │   │   ├── WebSocketConfig.java
    │   │   └── event/
    │   │       ├── CashLowEvent.java
    │   │       ├── TransactionCompletedEvent.java
    │   │       └── CardEvent.java
    │   └── resources/
    │       └── application.yaml
    └── test/
        └── java/com/atm/notificationservice/
            ├── NotificationServiceApplicationTests.java
            ├── NotificationControllerTest.java
            ├── NotificationRoutingServiceTest.java
            ├── EmailNotificationSenderTest.java
            ├── WebSocketNotificationSenderTest.java
            └── NotificationEventListenerTest.java