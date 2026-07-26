# Order and payment state transitions

## Lock order

Flows that can compete for the same pending QR order use this order: **payment -> order -> physical books by ascending ID -> coupons**. This applies to customer cancellation, admin cancellation, scheduler expiry, and SePay IPN. A flow reloads and rechecks the terminal state after it holds the payment/order locks.

Checkout first holds the cart and then its inventory/coupon locks; it does not transition an existing payment. A checkout replay reads the original payment and returns its fixed expiry.

## Valid terminal outcomes

| Event | Payment | Order | Inventory and coupon usage |
| --- | --- | --- |
| SePay IPN before expiry | `PENDING -> PAID` | remains `PENDING` until the normal order workflow confirms it | retained |
| Customer/admin cancel of unpaid pending order | `PENDING -> CANCELLED` | `PENDING -> CANCELLED` | return exactly once |
| Scheduler expiry of QR payment | `PENDING -> EXPIRED` | `PENDING -> CANCELLED`, response snapshot `paymentStatus=EXPIRED` | return exactly once |
| SePay IPN after expiry/cancellation | stays `EXPIRED` or `CANCELLED` | stays `CANCELLED` | no second stock/coupon change; reconciliation issue only |

`PAID`, `SHIPPING`, and `DELIVERED` orders cannot use the simple customer cancellation route. A paid cancellation returns `409` with code `ORDER_PAID_REFUND_REQUIRED`; an admin must create a separate refund ledger entry. A refund never cancels shipment or rolls back/restocks stock by itself. See `REFUND_STATE_TRANSITIONS.md`.
