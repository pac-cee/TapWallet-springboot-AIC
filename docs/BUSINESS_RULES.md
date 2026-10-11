# TapWallet Business Rules and User Flows

## Money and wallet rules

- TapWallet supports RWF only. Currency conversion and exchange-rate risk are outside the academic scope.
- Only an active CUSTOMER may own a wallet, and each customer may own at most one.
- Customers cannot create money by topping up their own wallet. Balance top-up and withdrawal are administrator-controlled demo settlement operations.
- Amounts must be positive. Wallets never permit overdrafts.
- Frozen users, wallets, and merchants cannot participate in payments.
- Mutable wallet, merchant, and payment rows use optimistic versions so concurrent changes fail instead of overwriting a newer balance or reversal.

## Payment flow

1. The customer signs in and receives a signed bearer JWT.
2. The payment endpoint derives the customer and wallet from the signed `userId` claim; the browser never selects a wallet ID.
3. The service validates ownership, active statuses, amount, balance, merchant, and the own-shop rule.
4. One relational transaction debits the wallet, credits merchant revenue, and creates the immutable payment record.
5. An activity document records the operation.
6. Email and SMS notification events are published. In Docker mode RabbitMQ consumes them through the safe logging delivery adapter.

## Reversal flow

- Only ADMIN may reverse a payment.
- Only a completed payment may be reversed, and only once.
- Reversal marks the original record `REVERSED`; it never deletes financial history.
- The wallet is refunded and merchant revenue reduced in one relational transaction.
- Audit and email/SMS events preserve evidence of the reversal.

## Identity and access

- ADMIN manages resources, balances, reversals, audit records, and reports.
- CUSTOMER views their wallet, pays merchants, and views their own payments.
- MERCHANT views their own shop, revenue, and received payments.
- Actor-scoped endpoints derive identity from the validated token instead of accepting a browser-supplied user ID.
- Missing/invalid authentication returns 401; insufficient role returns 403.

## Reporting flow

1. ADMIN selects inclusive From and To dates, up to 366 days.
2. The API returns completed/reversed volumes, payment counts, user/merchant status counts, current balances/revenue, daily volume, and top merchants.
3. CSV export applies the same date rules and includes transaction-level evidence.
4. CUSTOMER and MERCHANT report requests are rejected with 403.

## Technical boundaries

- PostgreSQL is the operational source of truth.
- MongoDB is the flexible audit/document store.
- RabbitMQ decouples payment work from notification delivery.
- The default profile replaces external infrastructure with H2 and in-memory adapters for classroom use.
- OAuth2 Resource Server support validates bearer JWTs. A third-party social login authorization-code flow is not claimed.
- Simulated notification delivery is intentional; no personal email account or third-party credential is stored in the project.
