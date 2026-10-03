# TapWallet

TapWallet is a full-stack Web Technology project for secure wallet-to-merchant payments. It combines a responsive Next.js interface, a layered Spring Boot API, relational and document persistence, OAuth2 Resource Server-compatible JWT authentication, RBAC, event publishing, tests, Postman, Docker Compose, and CI.

The customer workspace supports balance review, merchant payment, and searchable activity. Merchants receive a scoped sales view. Administrators have complete user, merchant, and wallet management, controlled balance operations, payment reversal, confirmation dialogs, and clear success/error notifications.

**Student:** Pacifique Bakundukize (26798)

**Course:** Web Technology, 2026-2027

**Instructor:** Jeremie U. Tuyisenge

## Quick start - no Docker required

The default classroom mode needs no external database or broker. It uses H2, an in-memory document store, and an in-memory notification publisher.

```bash
# Terminal 1 - API
mvn spring-boot:run

# Terminal 2 - web app
cd frontend
bun install
bun run dev
```

Open `http://localhost:3000`. The API runs on `http://localhost:8080`.

| Role | Email | Password |
|---|---|---|
| Administrator | `admin@tapwallet.rw` | `Admin@123` |
| Customer | `alice@tapwallet.rw` | `Pass@123` |
| Customer | `eric@tapwallet.rw` | `Pass@123` |
| Merchant | `cafe@tapwallet.rw` | `Pass@123` |
| Merchant | `shop@tapwallet.rw` | `Pass@123` |

## Full stack with Docker

The infrastructure mode uses PostgreSQL, MongoDB, and RabbitMQ.

```bash
cp .env.example .env
# Set a stable JWT_SECRET in .env for persistent deployments, then:
docker compose up --build -d
docker compose ps
```

- Web app: `http://localhost:3000`
- API health: `http://localhost:8080/actuator/health`
- RabbitMQ management: `http://localhost:15672`

## Architecture

```text
Next.js UI
    │ bearer JWT + JSON
    ▼
Spring Boot controllers
    ▼
Transactional services
    ▼
Domain entities and repository/event/document ports
    ├── H2 or PostgreSQL        operational records
    ├── memory or MongoDB       activity documents
    └── memory or RabbitMQ      notification events
```

The backend follows the lecturer's organization:

```text
controller/   thin HTTP endpoints
dto/          validated request/response contracts
domain/       JPA entities and business state
repository/   Spring Data relational repositories
service/      business rules and transactions
security/     user details, authentication, JWT issuing
document/     non-relational activity port and adapters
event/        notification port and memory/RabbitMQ adapters
config/       seed data and security configuration
exception/    consistent JSON errors
```

## Verification

```bash
mvn test

cd frontend
bun run lint
bun run typecheck
bun run build

docker compose config --quiet
```

Import [postman/TapWallet.postman_collection.json](postman/TapWallet.postman_collection.json) into Postman, set its `adminPassword`, `customerPassword`, and `merchantPassword` variables from the local accounts table above, then run the Authentication folder first.

## Documentation

- [Full project report](docs/PROJECT_REPORT.md) - requirements, stories, quality attributes, UML, ERD, schema, architecture, sequences, RBAC, wireframes, and traceability.
- [Deployment runbook](docs/DEPLOYMENT.md) - local/Docker operation, controls, production checklist, and rollback.
- [Testing and QA](docs/TESTING.md) - automated coverage, commands, responsive checks, and results.
- [UI design record](docs/UI_DESIGN.md) - research, reference lock, decisions, and accessibility.

## Main API areas

| Area | Endpoints |
|---|---|
| Authentication | `POST /api/auth/login` |
| Current actor | `GET /api/me`, `/api/me/wallet`, `/api/me/merchant`, `/api/me/payments` |
| Customer actions | `POST /api/me/wallet/top-up`, `POST /api/me/payments` |
| Admin resources | `/api/users`, `/api/wallets`, `/api/merchants`, `/api/payments`, `/api/activity` |

Protected requests use `Authorization: Bearer <accessToken>`. Anonymous access returns 401 and insufficient role access returns 403.

## Git workflow

Development takes place on `feature/final-webtech-project`. GitHub Actions verifies the backend, frontend, Compose configuration, and image builds. The final delivery is a pull request into `main` after all checks pass.
