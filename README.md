# OAuth2 Spring Boot

Centralized authentication/identity microservice. Built as a standards-based
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
H2 itself is a dev/test-only dependency and is not included in the
production build (`./gradlew bootJar`) or the Docker image.

## Production deployment

The `prod` Spring profile runs against real PostgreSQL instead of H2. The
easiest way to run it locally is via Docker Compose:

```bash
cp .env.example .env   # fill in real values — .env is gitignored
docker compose up --build
```

This builds the app image, starts a Postgres container, and wires them
together. Required environment variables (see `.env.example`):

- `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD` — PostgreSQL connection
- `APP_BASE_URL` — externally reachable base URL; also used as the OAuth2/OIDC issuer
- `MAIL_FROM`, `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD` — SMTP for
  verification/password-reset emails (registration fails without a reachable SMTP server)
- `CORS_ALLOWED_ORIGINS` — comma-separated origins allowed to call `/api/**` cross-origin;
  empty by default (no cross-origin access) until explicitly set

Note: the `test-client` OAuth2 client used for local Postman/Insomnia testing is only
seeded outside the `prod` profile — it is never created in a production database.

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
