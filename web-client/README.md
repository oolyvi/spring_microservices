# Web Client

Web UI application for the ATM banking microservices platform, serving the browser interface via `WebController` and
`src/main/resources/templates/index.html`. Built with Java and Spring Boot using Maven, with configuration in
`application.yaml` and Docker support.

## Architecture & Routing

- **Service**: `web-client` (`com.atm.webclient`)
- **Web Interface**: Serves `templates/index.html` through `WebController`.
- **API Gateway**: Integrated with the ATM platform's API Gateway on port **`8765`** (`http://localhost:8765`) for
  routing client/API traffic and edge concerns.
- **Containerization**: Includes a `Dockerfile` and `.dockerignore` for standalone or multi-container deployment.

## Project Structure

```text
web-client/
├── Dockerfile
├── .dockerignore
├── pom.xml
├── README.md
└── src/
    ├── main/
    │   ├── java/com/atm/webclient/
    │   │   ├── WebClientApplication.java
    │   │   └── WebController.java
    │   └── resources/
    │       ├── application.yaml
    │       └── templates/
    │           └── index.html
    └── test/
        └── java/com/atm/webclient/
            ├── WebClientApplicationTests.java
            └── WebControllerTest.java