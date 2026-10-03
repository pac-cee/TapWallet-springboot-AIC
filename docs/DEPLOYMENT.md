# TapWallet Deployment and Operations Runbook

## Runtime modes

### 1. Classroom mode - no external infrastructure

Terminal 1:

```bash
mvn spring-boot:run
```

Terminal 2:

```bash
cd frontend
bun install
bun run dev
```

This mode uses H2 (`jdbc:h2:mem:tapwallet`), the in-memory document adapter, and the in-memory event publisher. Open:

- Web application: `http://localhost:3000`
- API health: `http://localhost:8080/actuator/health`
- H2 console: `http://localhost:8080/h2-console`

### 2. Full infrastructure mode - Docker Compose

```bash
cp .env.example .env
# Set JWT_SECRET to 32+ random characters and change passwords for a stable deployment.
docker compose up --build -d
docker compose ps
```

Services:

| Service | Address | Purpose |
|---|---|---|
| Frontend | `http://localhost:3000` | Next.js role-based UI |
| Backend | `http://localhost:8080` | Spring Boot REST API |
| RabbitMQ management | `http://localhost:15672` | Event exchange/queue inspection |
| PostgreSQL | Internal only | Relational operational data |
| MongoDB | Internal only | Activity documents |

The backend activates `postgres,mongodb,rabbitmq`, waits for all three dependencies to become healthy, and exposes its own Actuator health check before the frontend starts.

## Useful controls

```bash
docker compose ps
docker compose logs -f backend frontend
docker compose restart backend
docker compose stop
docker compose down
```

`docker compose down` keeps named database volumes. To intentionally remove demo data, use `docker compose down -v`; this is destructive and should only be used when a clean database is desired.

## Production checklist

- Replace every default password and use a 32+ byte random `JWT_SECRET`.
- Put the frontend and API behind TLS/HTTPS.
- Restrict CORS to the deployed frontend origin.
- Do not publish database or RabbitMQ ports publicly.
- Use managed PostgreSQL, MongoDB, and RabbitMQ where available.
- Set automated backups and test restoration.
- Use `ddl-auto=validate` plus migrations (Flyway/Liquibase) after the academic prototype.
- Send logs/metrics to a central service and alert on health failures.
- Add a real email/SMS consumer for `tapwallet.notifications`.

## Rollback

Images are immutable. Tag every successful build with the Git commit SHA. To roll back, restore the previous image tags in the deployment configuration and restart the backend/frontend. Database schema changes must use backward-compatible migrations before production use.
