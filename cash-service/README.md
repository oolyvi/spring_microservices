# Cash Service

Microservice responsible for managing ATM cash inventory, cash operations, low-cash event publishing, and ATM terminal
coordination within the ATM banking platform. Built with Java and Spring Boot using Maven, with versioned SQL migrations
under `db/migration`, Apache Kafka event support, a terminal client, Spring Security configuration, global exception
handling, and Docker support.

## Architecture & Routing

- **Service**: `cash-service` (`com.atm.cashservice`)
- **API Gateway**: Exposed through the API Gateway on port **`8765`** (`http://localhost:8765`) for external/client
  routing, service routing, and edge concerns.
- **Event Streaming (Kafka)**: Publishes low-cash events via `com.atm.cashservice.event` (`CashLowEvent`,
  `CashEventPublisher`) with topic configuration in `KafkaTopicConfig`.
- **Inter-Service Clients**: Includes `AtmTerminalClient` under `com.atm.cashservice.client` for communicating with ATM
  terminal endpoints.
- **Persistence & Migrations**: Cash schema and initial seed data managed via `src/main/resources/db/migration/`
  (`V1__cash.sql`, `V2__seed_cash.sql`).
- **Security & Error Handling**: Configured via `com.atm.cashservice.config.SecurityConfig` and
  `GlobalExceptionHandler`.

## Project Structure

```text
cash-service/
├── Dockerfile
├── .dockerignore
├── pom.xml
├── README.md
└── src/
    ├── main/
    │   ├── java/com/atm/cashservice/
    │   │   ├── CashServiceApplication.java
    │   │   ├── CashController.java
    │   │   ├── CashService.java
    │   │   ├── Cash.java
    │   │   ├── CashRepository.java
    │   │   ├── GlobalExceptionHandler.java
    │   │   ├── client/
    │   │   │   └── AtmTerminalClient.java
    │   │   ├── config/
    │   │   │   ├── KafkaTopicConfig.java
    │   │   │   └── SecurityConfig.java
    │   │   └── event/
    │   │       ├── CashEventPublisher.java
    │   │       └── CashLowEvent.java
    │   └── resources/
    │       ├── application.yaml
    │       └── db/migration/
    │           ├── V1__cash.sql
    │           └── V2__seed_cash.sql
    └── test/
        └── java/com/atm/cashservice/
            ├── CashServiceApplicationTests.java
            ├── CashControllerTest.java
            └── CashServiceTest.java