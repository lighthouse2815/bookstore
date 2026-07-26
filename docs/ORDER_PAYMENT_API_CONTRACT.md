# Order and payment API contract

## Checkout and expiry

`POST /api/orders/checkout` requires an authenticated user and a UUID `Idempotency-Key` header. A retry with the same key and identical canonical payload replays the original order, payment, transfer content, and `paymentExpiresAt`; it never creates another payment or extends the expiry. A key reused with a different payload returns `409 Conflict`.

For `BANK_TRANSFER_QR`, the successful `201` response includes `paymentExpiresAt` in ISO-8601 UTC. `COD` returns `null` for this field. The same property is returned by `GET /api/orders/{id}` and `GET /api/orders/my` as `paymentExpiresAt`.

## Customer cancellation

`PUT /api/orders/{id}/cancel` requires the JWT owner of the order.

Request:

```json
{ "reason": "Tôi không còn nhu cầu mua sách" }
```

`reason` is trimmed, required, and limited to 500 characters. Only an unpaid pending order can be cancelled. Success returns the updated `OrderResponse`, with both `status` and `paymentStatus` set to `CANCELLED`.

| Condition | HTTP status |
| --- | --- |
| Not the owner or order not visible to the user | 404 |
| Invalid or blank reason | 400 |
| Paid, shipping, delivered, already cancelled, or otherwise non-cancellable | 409 |

## Payment reconciliation

All endpoints below require `ADMIN`.

| Method | Path | Purpose |
| --- | --- | --- |
| `GET` | `/api/admin/payment-reconciliation` | Paginated issues; filters: `page`, `size`, `status`, `issueType`, `from`, `to` |
| `GET` | `/api/admin/payment-reconciliation/{id}` | Read one issue |
| `PUT` | `/api/admin/payment-reconciliation/{id}/resolve` | Resolve an open issue with a required `resolutionNote` (max 1000 characters) |

An IPN that reaches an expired or cancelled payment records its transfer reference and creates one deduplicated reconciliation issue. It never marks the payment paid, confirms the order, re-debits stock, recreates coupon usage, or sends a normal payment-success confirmation. Resolving an issue does not issue a refund.

## Paid order cancellation

`PUT /api/orders/{id}/cancel` remains only for unpaid pending orders. A paid order returns `409` with `code=ORDER_PAID_REFUND_REQUIRED`. The administrative refund contract is in `REFUND_API_CONTRACT.md`.
