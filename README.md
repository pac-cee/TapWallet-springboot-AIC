# TapWallet — Spring Boot REST API

Web Technology **Assignment 4**: full CRUD on three TapWallet entities through a
Spring Boot REST API, tested with Postman.

**Author:** Pacifique Bakundukize (26798)
**Repository:** https://github.com/pac-cee/TapWallet-springboot-AIC
**Video walkthrough:** see `docs/TapWallet_Assignment4_Documentation.html`

## Stack

Java 17 · Spring Boot 3.3.5 · Spring Web · Spring Data JPA · Bean Validation ·
H2 in-memory database · Maven. No frontend: the API is consumed with Postman.

## Run it

```bash
mvn spring-boot:run
```

The API is at `http://localhost:8080/api`. The H2 console is at
`http://localhost:8080/h2-console` (JDBC URL `jdbc:h2:mem:tapwallet`, user `sa`,
empty password). The database is recreated and seeded on every start.

```bash
mvn test          # 20 MockMvc tests covering CRUD and every business rule
```

## Seed data

| Id | Entity | Detail |
|---|---|---|
| 1 | User | System Admin (ADMIN) |
| 2 | User | Alice Uwase (CUSTOMER) — owns wallet 1, 50 000 RWF |
| 3 | User | Eric Mugisha (CUSTOMER) — owns wallet 2, 12 000 RWF |
| 4 | User | Kigali Cafe Ltd (MERCHANT) — operates merchant 1 `CAF-001` |
| 5 | User | Nyarugenge Shop (MERCHANT) — operates merchant 2 `SHP-002` |

## Architecture

```
rw.ac.auca.tapwallet
├── domain/        JPA entities + enums (User, Wallet, Merchant, Payment)
├── repository/    Spring Data JPA interfaces
├── service/       business rules, one @Transactional service per entity
├── controller/    REST endpoints (@RestController), thin — no logic
├── dto/           validated request bodies (records + Bean Validation)
├── exception/     custom exceptions + @RestControllerAdvice → JSON errors
└── config/        DataSeeder (demo data on start-up)
```

Request flow: `Controller → Service → Repository → H2`. Controllers validate
the body (`@Valid`), services enforce the rules, repositories only persist.

### Relationships

```
User 1 ──── 0..1 Wallet        (Wallet.owner,     unique user_id)
User 1 ──── 0..1 Merchant      (Merchant.operator, unique user_id)
Wallet 1 ──── * Payment        (Payment.wallet)
Merchant 1 ──── * Payment      (Payment.merchant)
```

## The three CRUD entities

| Entity | Endpoints |
|---|---|
| **User** | `POST /api/users` · `GET /api/users` · `GET /api/users/{id}` · `PUT /api/users/{id}` · `PATCH /api/users/{id}/freeze` · `PATCH /api/users/{id}/activate` · `DELETE /api/users/{id}` |
| **Wallet** | `POST /api/wallets` · `GET /api/wallets` · `GET /api/wallets/{id}` · `GET /api/wallets/by-owner/{userId}` · `PUT /api/wallets/{id}` · `POST /api/wallets/{id}/top-up` · `POST /api/wallets/{id}/withdraw` · `DELETE /api/wallets/{id}` |
| **Merchant** | `POST /api/merchants` · `GET /api/merchants` · `GET /api/merchants/{id}` · `PUT /api/merchants/{id}` · `PATCH /api/merchants/{id}/freeze` · `PATCH /api/merchants/{id}/activate` · `DELETE /api/merchants/{id}` |

**Payment** is a fourth, supporting entity. It is a write-once record, so it
has create, read and reverse but no update or delete:
`POST /api/payments` · `GET /api/payments` · `GET /api/payments/{id}` ·
`GET /api/payments/by-wallet/{walletId}` · `GET /api/payments/by-merchant/{merchantId}` ·
`POST /api/payments/{id}/reverse`.

## Business rules

| Area | Rule | HTTP |
|---|---|---|
| User | Email (case-insensitive) and phone are unique | 409 |
| User | Name 3–60 chars, valid email, Rwandan phone, role required | 400 |
| User | Cannot be deleted while owning a wallet or operating a shop | 422 |
| User | Role cannot change while owning a wallet or a shop | 422 |
| Wallet | Only an ACTIVE `CUSTOMER` can own a wallet, and only one | 422 / 409 |
| Wallet | Balance changes only via top-up, withdraw, pay, reverse | — |
| Wallet | Amounts must be > 0, max 2 decimals | 400 |
| Wallet | No overdraft on withdraw or pay | 422 |
| Wallet | FROZEN wallet (or frozen owner) cannot transact | 422 |
| Wallet | Currency can change only while balance is zero | 422 |
| Wallet | Cannot delete with a balance or payment history | 422 |
| Merchant | Code unique, format `ABC-123`; stored upper-case | 400 / 409 |
| Merchant | Operator must be an ACTIVE `MERCHANT` user, one shop each | 422 / 409 |
| Merchant | FROZEN merchant does not accept payments | 422 |
| Merchant | Cannot delete with payment history (freeze instead) | 422 |
| Payment | Customer cannot pay their own shop | 422 |
| Payment | Debit, credit and record happen in one transaction | — |
| Payment | Reversal allowed exactly once; refunds wallet, deducts revenue | 422 |
| Any | Unknown id | 404 |

Every error is returned as JSON:

```json
{ "timestamp": "...", "status": 422, "error": "Unprocessable Entity",
  "message": "Insufficient funds: balance is 12000.00 RWF, payment is 99999." }
```

## Postman

Import `postman/TapWallet.postman_collection.json`. It contains 54 requests in
four folders (Users, Wallets, Merchants, Payments). Run them top to bottom:
each create request stores the new id in a collection variable, and requests
prefixed `[Rule]` demonstrate a business rule and assert the expected error
status.
