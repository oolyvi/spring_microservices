# Transaction Service

Microservice responsible for processing financial transactions, transfers, transaction receipts, event publishing, and
downstream account coordination within the ATM banking platform. Built with Java and Spring Boot using Maven, with
versioned SQL migrations under `db/migration`, Apache Kafka event support, a downstream account client, Spring Security
configuration, global exception handling, and Docker support.

## Architecture & Routing

- **Service**: `transaction-service` (`com.atm.transactionservice`)
- **API Gateway**: Exposed through the API Gateway on port **`8765`** (`http://localhost:8765`) for external/client
  routing, service routing, and edge concerns.
- **Event Streaming (Kafka)**: Publishes transaction completion events via `com.atm.transactionservice.event`
  (`TransactionCompletedEvent`, `TransactionEventPublisher`) with topic configuration in `KafkaTopicConfig`.
- **Inter-Service Clients**: Includes `AccountServiceClient` under `com.atm.transactionservice.client` for communicating
  with `account-service`.
- **Persistence & Migrations**: Transaction schema and seed data managed via `src/main/resources/db/migration/`
  (`V1__transactions.sql`, `V2__seed_transactions.sql`).
- **Security & Error Handling**: Configured via `com.atm.transactionservice.config.SecurityConfig` and
  `GlobalExceptionHandler`.

## Project Structure

```text
transaction-service/
├── Dockerfile
├── .dockerignore
├── pom.xml
├── README.md
└── src/
    ├── main/
    │   ├── java/com/atm/transactionservice/
    │   │   ├── TransactionServiceApplication.java
    │   │   ├── TransactionController.java
    │   │   ├── TransactionService.java
    │   │   ├── Transaction.java
    │   │   ├── TransactionRequest.java
    │   │   ├── TransferRequest.java
    │   │   ├── TransactionResponseDto.java
    │   │   ├── TransactionReceiptDto.java
    │   │   ├── TransactionRepository.java
    │   │   ├── TransactionNotFoundException.java
    │   │   ├── GlobalExceptionHandler.java
    │   │   ├── client/
    │   │   │   └── AccountServiceClient.java
    │   │   ├── config/
    │   │   │   ├── KafkaTopicConfig.java
    │   │   │   └── SecurityConfig.java
    │   │   └── event/
    │   │       ├── TransactionCompletedEvent.java
    │   │       └── TransactionEventPublisher.java
    │   └── resources/
    │       ├── application.yaml
    │       └── db/migration/
    │           ├── V1__transactions.sql
    │           └── V2__seed_transactions.sql
    └── test/
        └── java/com/atm/transactionservice/
            ├── TransactionServiceApplicationTests.java
            ├── TransactionControllerTest.java
            └── TransactionServiceTest.java