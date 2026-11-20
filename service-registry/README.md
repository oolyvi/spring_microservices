# Service Registry

Service discovery and registration server for the ATM banking microservices platform. Built with Java and Spring Boot
using Maven, with configuration in `application.yaml` and Docker support.

## Architecture & Routing

- **Service**: `service-registry` (`com.atm.serviceregistry`)
- **Role**: Registers microservice instances and supports service discovery across the platform (including `api-gateway`
  and domain microservices).
- **API Gateway**: External client traffic enters through the API Gateway on port **`8765`** (`http://localhost:8765`),
  which routes requests to registered downstream services.
- **Registry Configuration**: Defined in `src/main/resources/application.yaml`.
- **Containerization**: Includes a `Dockerfile` and `.dockerignore` for standalone or multi-container
  deployment[cite: 9].

## Project Structure

```text
service-registry/
├── Dockerfile
├── .dockerignore
├── pom.xml
├── README.md
└── src/
    ├── main/
    │   ├── java/com/atm/serviceregistry/
    │   │   └── ServiceRegistryApplication.java
    │   └── resources/
    │       └── application.yaml
    └── test/
        └── java/com/atm/serviceregistry/
            └── ServiceRegistryApplicationTests.java