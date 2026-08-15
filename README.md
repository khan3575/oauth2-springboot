# OAuth2 Spring Boot

Central authentication/identity microservice. Built as a standards-based
OAuth2/OIDC provider so other applications authenticate against it instead
of each implementing their own login.

> Status: **early development** — Phase 1 (email/password signup & login)
> in progress. No stable API yet.

## Features

Planned, delivered in phases:

- [ ] Email/password signup & login, email verification, password reset
- [ ] Account security hardening (lockout, breached-password checks,
      session/token rotation)
- [ ] OAuth2 / OpenID Connect provider (Authorization Code + PKCE, client
      credentials, consent, JWKS/discovery)
- [ ] Passkeys / WebAuthn
- [ ] MFA (TOTP), admin tooling, self-service data export & deletion

## Tech stack

- Java 25, Spring Boot 4.1
- Spring Security, Spring Data JPA, Flyway
- H2 (local development, PostgreSQL compatibility mode) — PostgreSQL for production
- Gradle

## Getting started

### Prerequisites

- JDK 25
- No external services required for local dev (H2 is in-process)

### Run

```bash
./gradlew bootRun
```

### Test

```bash
./gradlew test
```

### H2 console (local dev)

With the app running, the H2 web console is available for inspecting the
local database — check `application.properties` for the enabled path.

## Project structure

```
src/main/java/com/khan/oauth2springboot/   application code
src/main/resources/                        config, Flyway migrations
src/test/java/com/khan/oauth2springboot/   tests
```

## Configuration

Configuration lives in `src/main/resources/application.properties`.
No secrets should ever be committed here — local dev uses H2 with no
credentials; anything beyond that should be supplied via environment
variables.

## License

Private project, all rights reserved.
