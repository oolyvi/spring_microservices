# API Gateway

Edge routing and entry-point service for the ATM banking microservices platform, exposing external client traffic on
port **`8765`**. Built with Java and Spring Boot using Maven, with configuration in `application.yaml` and Docker
support.

## Architecture & Routing

- **Service**: `api-gateway` (`com.atm.apigateway`)
- **Gateway Port**: Listens on port **`8765`** (`http://localhost:8765`) as the unified entry point for downstream
  microservices (`account-service`, `admin-service`, and other platform services).
- **Route & Gateway Configuration**: Defined in `src/main/resources/application.yaml`.
- **Containerization**: Includes a `Dockerfile` and `.dockerignore` for standalone or multi-container deployment.

## Project Structure

```text
api-gateway/
├── Dockerfile
├── .dockerignore
├── pom.xml
├── README.md
└── src/
    ├── main/
    │   ├── java/com/atm/apigateway/
    │   │   └── ApiGatewayApplication.java
    │   └── resources/
    │       └── application.yaml
    └── test/
        └── java/com/atm/apigateway/
            └── ApiGatewayApplicationTests.java