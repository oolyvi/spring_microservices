# Card Service

Microservice responsible for managing banking cards, card creation, PIN verification and changes, event publishing, and
downstream account/customer lookups within the ATM banking platform. Built with Java and Spring Boot using Maven, with
versioned SQL migrations under `db/migration`, Apache Kafka event support, Spring Security configuration, global
exception handling, and Docker support.

## Architecture & Routing

- **Service**: `card-service` (`com.atm.cardservice`)
- **API Gateway**: Exposed through the API Gateway on port **`8765`** (`http://localhost:8765`) for external/client
  routing, service routing, and edge concerns.
- **Event Streaming (Kafka)**: Publishes card events via `com.atm.cardservice.event` (`CardEvent`, `CardEventPublisher`)
  with topic configuration in `KafkaTopicConfig`.
- **Inter-Service Clients**: Includes `AccountInterface` and `CustomerInterface` under `com.atm.cardservice.client` for
  communicating with downstream services.
- **Persistence & Migrations**: Card schema and initial seed data managed via `src/main/resources/db/migration/`
  (`V1__cards.sql`, `V2__seed_cards.sql`).
- **Security & Error Handling**: Configured via `com.atm.cardservice.config.SecurityConfig`, `CardServiceConfig`, and
  `GlobalExceptionHandler`.

## Project Structure

```text
card-service/
├── Dockerfile
├── .dockerignore
├── pom.xml
├── README.md
└── src/
    ├── main/
    │   ├── java/com/atm/cardservice/
    │   │   ├── CardServiceApplication.java
    │   │   ├── CardController.java
    │   │   ├── CardService.java
    │   │   ├── Card.java
    │   │   ├── CardDto.java
    │      ├── CreateCardRequest.java
    │      ├── PinVerifyRequest.java
    │      ├── PinChangeDto.java
    │      ├── CardNotFoundException.java
    │   ├── CardRepository.java
    │   ├── GlobalExceptionHandler.java
    │   ├── client/
    │   │   ├── AccountInterface.java
    │   │   └── CustomerInterface.java
    │   ├── config/
    │   │   ├── CardServiceConfig.java
    │   │   ├── KafkaTopicConfig.java
    │   │   └── SecurityConfig.java
    │   └── event/
    │       ├── CardEvent.java
    │       └── CardEventPublisher.java
    └── resources/
        ├── application.yaml
        └── db/migration/
            ├── V1__cards.sql
            └── V2__seed_cards.sql
    └── test/
        └── java/com/atm/cardservice/
            ├── CardServiceApplicationTests.java
            ├── CardControllerTest.java
            └── CardServiceTest.java