# Customer Service

Microservice responsible for managing customer records, profiles, and customer data operations within the ATM banking
platform. Built with Java and Spring Boot using Maven, with versioned SQL migrations under `db/migration`, Spring
Security configuration, and Docker support.

## Architecture & Routing

- **Service**: `customer-service` (`com.atm.customerservice`)
- **API Gateway**: Exposed through the API Gateway on port **`8765`** (`http://localhost:8765`) for external/client
  routing, service routing, and edge concerns.
- **Persistence & Migrations**: Customer schema and initial seed data managed via `src/main/resources/db/migration/`
  (`V1__customers.sql`, `V2__seed_customers.sql`).
- **Security**: Configured via `com.atm.customerservice.config.SecurityConfig`.

## Project Structure

```text
customer-service/
├── Dockerfile
├── .dockerignore
├── pom.xml
├── README.md
└── src/
    ├── main/
    │   ├── java/com/atm/customerservice/
    │   │   ├── CustomerServiceApplication.java
    │   │   ├── CustomerController.java
    │   │   ├── CustomerService.java
    │   │   ├── Customer.java
    │   │   ├── CustomerDto.java
    │   │   ├── CustomerRepository.java
    │   │   └── config/
    │   │       └── SecurityConfig.java
    │   └── resources/
    │       ├── application.yaml
    │       └── db/migration/
    │           ├── V1__customers.sql
    │           └── V2__seed_customers.sql
    └── test/
        └── java/com/atm/customerservice/
            ├── CustomerServiceApplicationTests.java
            ├── CustomerControllerTest.java
            └── CustomerServiceTest.java