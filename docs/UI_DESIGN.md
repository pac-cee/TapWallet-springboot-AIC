# TapWallet UI Design Record

## Brief

Designing a responsive digital wallet for customers, merchants, and administrators on the web. The goal is to make balances, payment actions, and transaction evidence immediately understandable. The tone is trustworthy, direct, and modern. The primary risk is making an academic dashboard feel generic or hiding important financial status inside decorative surfaces.

## Reference synthesis

| Reference | Bounded contribution | Adaptation |
|---|---|---|
| Stripe Dashboard/Payments | Dense transaction hierarchy, useful columns, action-first operations | Payment tables show reference, counterparty, status, date, and aligned amount. |
| Wise account interface | Balance-first customer hierarchy and mobile continuity | The available balance is the main surface and contains the payment action. |
| Paystack transactions | Filters and practical African payment-dashboard context | Activity supports text search and status filtering with restrained controls. |

## Reference lock

- Primary direction: precise, utility-first payment dashboard.
- Preserve: light canvas, strong numeric hierarchy, compact navigation, clear tables, rare accent use.
- Borrow only: Wise-like balance emphasis and Paystack-like filtering.
- Role rules: teal is for primary action, selection, focus, and positive operational status; red is semantic error/frozen only.
- Media strategy: product UI itself is the evidence; no decorative stock imagery is required.
- Reject: purple gradients, dark-by-default UI, excessive cards, emoji icons, decorative side stripes, or serif headline swaps.
- Tokens: neutral canvas/surfaces, system sans, teal `#087f69`, 8/12/18 px radii, dividers for structure, layered shadow only where elevation is useful.

## Decision ledger

| Decision | Source | Rule | Reason |
|---|---|---|---|
| Balance is the customer dashboard's dominant element | Wise pattern | Primary financial state first | A wallet user first asks “how much can I use?” |
| Transaction records use a table on desktop | Stripe/Paystack pattern | Optimize scanning and comparison | Reference, counterparty, state, and amount repeat across rows. |
| Sidebar becomes bottom navigation on mobile | User constraint + responsive craft | Keep frequent actions within thumb reach | Supports 360 px layouts without a hidden drawer. |
| One teal accent | Color craft reference | Fewer colors, strict semantic purpose | Builds trust and avoids visual noise. |
| System sans family | Finance/product typography craft | One neutral family, weight for hierarchy | Fast, legible, and requires no external font request. |
| Cards are limited to interactive/summary surfaces | Anti-AI-slop craft | Dividers/sections before cards | Prevents “card soup” and clarifies structure. |
| Button press uses `scale(.96)` | Better UI motion | Exact, interruptible press response | Tactile feedback without distracting animation. |

## Accessibility rules

- Semantic headings, labels, tables, nav landmarks, and status text.
- `:focus-visible` rings; focus is never removed without replacement.
- Minimum 44 px mobile targets for primary controls and navigation.
- Errors use text and `role="alert"`, not color alone.
- Inputs use appropriate type, input mode, autocomplete, and paste remains enabled.
- Motion is brief and removed under `prefers-reduced-motion`.
