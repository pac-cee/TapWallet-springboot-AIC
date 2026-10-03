# TapWallet Testing and Quality Assurance

## Automated commands

```bash
# Backend compilation and 22 integration tests
mvn test

# Frontend verification
cd frontend
bun install --frozen-lockfile
bun run lint
bun run typecheck
bun run build

# Container configuration and builds
docker compose config --quiet
docker compose build backend frontend
```

## Test coverage map

| Risk | Automated evidence |
|---|---|
| Invalid user data | Bean Validation returns 400 with field errors. |
| Duplicate identity data | Email/phone/merchant-code conflict tests return 409. |
| Missing entity | Unknown ids return 404. |
| Broken role/domain rule | Role/ownership/shop constraints return 422. |
| Overspending | Withdraw/payment overdraft tests return 422 without balance corruption. |
| Frozen resource use | Frozen wallet/merchant tests deny payment. |
| Duplicate reversal | Second reversal returns 422. |
| Anonymous protected access | Security integration test expects 401. |
| Wrong role | Customer request to admin resource expects 403. |
| Actor isolation | Customer wallet is resolved from the signed token claim. |

## Manual responsive checks

Verify these routes at 360 x 800, 768 x 1024, and 1440 x 1000:

- `/` login and demo role selection
- `/dashboard` for customer, merchant, and admin
- `/pay` customer form, errors, loading, and success redirect
- `/activity` empty, populated, search, and status-filter states
- `/admin` user/merchant tabs and freeze/activate action
- `/admin` create/edit/delete users and merchants; open/edit/delete wallets; top-up/withdraw controls
- `/activity` admin reversal confirmation and success/error notification

Expected: no clipped headings, no page-level horizontal scroll, 44 px mobile targets, visible keyboard focus, readable status without relying on motion, and table overflow contained within its region.

For mutation checks, verify that invalid fields remain in the dialog with a specific message, successful changes display a notification and refresh the table, protected deletes explain the business rule, and an expired token returns the user to sign-in.

## Postman

Import `postman/TapWallet.postman_collection.json`. Set the three password variables from README, run `0. Authentication / Admin login` first to store `accessToken`, then run the remaining folders. Customer and merchant login examples store separate role tokens for actor-scoped requests.

## Recorded verification

| Check | Result |
|---|---|
| Maven tests | **Pass** - 22/22 tests on Java 17 |
| ESLint | **Pass** - no errors or warnings |
| TypeScript | **Pass** - strict no-emit check |
| Next.js production build | **Pass** - 8 static pages generated |
| Compose config | **Pass** - 5 services validated |
| Docker image build | **Pass** - backend and frontend images built |
| Full Docker stack | **Pass** - PostgreSQL, MongoDB, RabbitMQ, backend, and frontend healthy |
| API lifecycle | **Pass** - auth, validation, RBAC, self-lockout guard, all admin CRUD, balances, payment/reversal, and activity document |
| Desktop/mobile visual QA | **Pass** - 1440 x 1000 and 390 x 844; no horizontal overflow or browser errors |

Recorded on 3 October 2026. The containerized backend health endpoint returned `UP`, and the frontend returned HTTP 200.
