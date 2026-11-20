# ATM Service

Microservice responsible for managing ATMs, ATM terminals, ATM login/transaction requests, and downstream card coordination within the ATM banking platform. Built with Java and Spring Boot using Maven, with versioned SQL migrations under `db/migration`, Spring Security configuration, global exception handling, a declarative card client, and Docker support.

## Architecture & Routing

- **Service**: `atm-service` (`com.atm.atmservice`)
- **API Gateway**: Exposed through the API Gateway on port **`8765`** (`http://localhost:8765`) for external/client routing, service routing, and edge concerns.
- **Inter-Service Clients**: Includes `CardInterface` under `com.atm.atmservice.client` for communicating with the card service.
- **Persistence & Migrations**: ATM and terminal schema and seed migrations managed via `src/main/resources/db/migration/` (`V1__atms.sql`, `V2__atm_terminals.sql`, `V3__seed_atm_terminals.sql`).
- **Security & Error Handling**: Configured via `com.atm.atmservice.config.SecurityConfig` and `GlobalExceptionHandler`.

## Project Structure

```text
atm-service/
├── Dockerfile
├── .dockerignore
├── pom.xml
├── README.md
└── src/
    ├── main/
    │   ├── java/com/atm/atmservice/
    │   │   ├── AtmServiceApplication.java
    │   │   ├── AtmController.java
    │   │   ├── AtmTerminalController.java
    │   │   ├── AtmTerminalService.java
    │   │   ├── AtmTerminal.java
    │   │   ├── AtmTerminalRepository.java
    │   │   ├── AtmLoginRequest.java
    │   │   ├── TransactionRequest.java
    │   │   ├── GlobalExceptionHandler.java
    │   │   ├── client/
    │   │   │   └── CardInterface.java
    │   │   └── config/
    │   │       └── SecurityConfig.java
    │   └── resources/
    │       ├── application.yaml
    │       └── db/migration/
    │           ├── V1__atms.sql
    │           ├── V2__atm_terminals.sql
    │           └── V3__seed_atm_terminals.sql
    └── test/
        └── java/com/atm/atmservice/
            ├── AtmServiceApplicationTests.java
            ├── AtmControllerTest.java
            ├── AtmTerminalTest.java
            ├── AtmTerminalServiceTest.java
            ├── AtmTerminalControllerTest.java
            └── AtmLoginRequestTest.java