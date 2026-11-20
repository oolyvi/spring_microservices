# Admin Service

Microservice responsible for administrative operations, admin authentication, and cross-service coordination within the
ATM banking platform. Built with Java and Spring Boot, using Maven for builds, versioned SQL migrations under
`db/migration`, Spring Security configuration, global exception handling, declarative service clients, and Docker
support.

## Architecture & Routing

- **Service**: `admin-service` (`com.atm.adminservice`)
- **API Gateway**: Exposed through the API Gateway on port **`8765`** (`http://localhost:8765`) for external/client
  routing, service routing, and edge concerns.
- **Inter-Service Clients**: Includes client interfaces under `com.atm.adminservice.client` (`CardInterface`,
  `AtmInterface`, `AccountInterface`) for communicating with downstream services.
- **Persistence & Migrations**: Admin schema and initial seed data managed via `src/main/resources/db/migration/`
  (`V1__admins.sql`, `V2__seed_admins.sql`).
- **Security & Error Handling**: Configured via `com.atm.adminservice.config.SecurityConfig` and
  `GlobalExceptionHandler`.

## Project Structure

```text
admin-service/
├── Dockerfile
├── .dockerignore
├── pom.xml
├── README.md
└── src/
    ├── main/
    │   ├── java/com/atm/adminservice/
    │   │   ├── AdminServiceApplication.java
    │   │   ├── AdminController.java
    │   │   ├── AdminService.java
    │   │   ├── Admin.java
    │   │   ├── AdminLoginDto.java
    │   │   ├── AdminRepository.java
    │   │   ├── GlobalExceptionHandler.java
    │   │   ├── client/
    │   │   │   ├── CardInterface.java
    │   │   │   ├── AtmInterface.java
    │   │   │   └── AccountInterface.java
    │   │   └── config/
    │   │       └── SecurityConfig.java
    │   └── resources/
    │       ├── application.yaml
    │       └── db/migration/
    │           ├── V1__admins.sql
    │           └── V2__seed_admins.sql
    └── test/
        └── java/com/atm/adminservice/
            ├── AdminServiceApplicationTests.java
            ├── AdminControllerTest.java
            └── AdminServiceTest.java