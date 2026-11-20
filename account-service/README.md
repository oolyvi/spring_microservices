# Account Service

Microservice responsible for managing customer bank accounts, balances, and account operations within the ATM banking
platform. Built with Java and Spring Boot, using Maven for builds, versioned SQL migrations under `db/migration`, Spring
Security configuration, and Docker support.

## Architecture & Routing

- **Service**: `account-service` (`com.atm.accountservice`)
- **API Gateway**: Exposed through the API Gateway on port **`8765`** (`http://localhost:8765`) for external/client
  routing, service routing, and edge concerns.
- **Persistence & Migrations**: Relational database schema and seed/operation migrations managed via
  `src/main/resources/db/migration/` (`V1__accounts.sql`, `V2__seed_accounts.sql`, `V3__account_operations.sql`).
- **Security**: Configured via `com.atm.accountservice.config.SecurityConfig`.

## Project Structure

```text
account-service/
├── Dockerfile
├── .dockerignore
├── pom.xml
├── README.md
└── src/
    ├── main/
    │   ├── java/com/atm/accountservice/
    │   │   ├── AccountServiceApplication.java
    │   │   ├── AccountController.java
    │   │   ├── AccountService.java
    │   │   ├── Account.java
    │   │   ├── AccountDto.java
    │   │   ├── AmountRequest.java
    │   │   ├── AccountOperation.java
    │   │   ├── AccountRepository.java
    │   │   ├── AccountOperationRepository.java
    │   │   └── config/
    │   │       └── SecurityConfig.java
    │   └── resources/
    │       ├── application.yaml
    │       └── db/migration/
    │           ├── V1__accounts.sql
    │           ├── V2__seed_accounts.sql
    │           └── V3__account_operations.sql
    └── test/
        └── java/com/atm/accountservice/
            ├── AccountServiceApplicationTests.java
            ├── AccountServiceTest.java
            └── AccountControllerTest.java