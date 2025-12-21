# Enterprise Backend Template

A lightweight Spring Boot template for multi-tenant enterprise backends using:

- Spring Boot 3.x
- Java 21 (toolchain configured in `build.gradle`)
- Spring Data MongoDB
- Keycloak admin client for user management
- Spring Security resource server (JWT)
- Opinionated project layout and helper services (indexing, secrets, repository abstractions)

This repository is intended as a starting point for backend services that need multi-tenant user management, indexing and secure secrets storage.

Prerequisites
-------------
- Java 21 (the project uses Gradle toolchain configured for Java 21)
- Git
- A running MongoDB instance (default: `mongodb://localhost:27017/youapp`)
- A Keycloak server for admin operations (optional for some local flows)
- (Optional) Docker to run services locally

Quickstart
----------
From the project root:

- Build the project:

```powershell
./gradlew clean build
```

- Run tests:

```powershell
./gradlew test
```

- Run the application (development):

```powershell
./gradlew bootRun
```

- Run the packaged jar:

```powershell
java -jar build/libs/enterprise_backend_template-0.0.1-SNAPSHOT.jar
```

Configuration
-------------
Application configuration is in `src/main/resources/application.yaml`. The most commonly used environment variables and properties are:

- `MONGO_DB_URI` — MongoDB connection string (default shown in `application.yaml`).
- `SERVER_PORT` — HTTP port (default: 8080).
- Keycloak admin properties (under `properties.keycloak`):
  - `KEYCLOAK_SERVER_URL` (default `https://auth-dev.youapp.com`)
  - `KEYCLOAK_REALM` (default `master`)
  - `KEYCLOAK_ADMIN_USERNAME`
  - `KEYCLOAK_ADMIN_PASSWORD`
  - Client ids/secrets for master clients / token issuers
- App crypto keys (under `app.crypto.keys`) — the template expects a 32-byte Base64 secret for AES-GCM. The default value exists for convenience in development only and should be replaced in production.

Important config keys in `application.yaml`:
- `spring.mongodb.uri`
- `spring.security.oauth2.resourceserver.jwt.issuer-uri` (derived from Keycloak server/realm)
- `app.crypto.keys` (list of keys used to encrypt secrets)

Environment variables can be used to override any property defined in `application.yaml`.

Development notes
-----------------
- The project uses Lombok. Ensure your IDE has Lombok support enabled.
- The Gradle wrapper (`gradlew` / `gradlew.bat`) is included so you don't need a local Gradle install.
- Java toolchain is configured in `build.gradle` (Java 21).

Services and components
-----------------------
The template includes common building blocks useful for enterprise apps. Some notable components to review:

- `src/main/java` contains the usual Spring Boot application entrypoint and domain packages.
- `services` contains business logic (examples: `UserAccountService`, `IndexingService`, `UserSecretService`).
- `repositories` contains a `GenericMongoRepository` abstraction used by services.
- `models` contains DTOs and Mongo document models for users and secrets.

Keycloak integration
--------------------
- The template uses the Keycloak admin client to provision and update users. For local development you can run a Keycloak instance (or use a shared dev Keycloak).
- Make sure Keycloak admin credentials and client secrets are set via environment variables before performing operations that interact with Keycloak.

Testing
-------
- Unit and integration tests run via Gradle: `./gradlew test`.
- Tests use JUnit Platform (configured in `build.gradle`).

Packaging and deployment
------------------------
- Build a fat jar with `./gradlew bootJar` (this is part of `./gradlew build`).
- The project is ready to be containerized. As a minimal step, create a Dockerfile that runs the jar and configure environment variables at runtime.

Contributing
------------
- Follow the existing code style and package layout.
- Add tests for new behavior (happy-path and important edge cases).
- Open a PR describing the change and why it's needed.

Security
--------
- Replace any placeholder crypto keys and Keycloak secrets before deploying to production.
- The default `app.crypto.keys` in `application.yaml` is for development convenience only.

Troubleshooting
---------------
- If the application fails to start due to MongoDB, confirm `MONGO_DB_URI` and that your MongoDB instance is reachable.
- If Keycloak integration fails, check `KEYCLOAK_SERVER_URL`, `KEYCLOAK_REALM`, and admin credentials.

Useful commands
---------------
- Build: `./gradlew clean build`
- Run: `./gradlew bootRun`
- Test: `./gradlew test`

More help
---------
See `HELP.md` for links to relevant Spring and Gradle documentation.
