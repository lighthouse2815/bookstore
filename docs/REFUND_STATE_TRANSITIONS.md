# Refund state transitions

`refunds` is a financial ledger. An approved return request is not a successful refund, and a refund does not restock inventory.

```text
REQUESTED -> APPROVED -> PROCESSING -> SUCCEEDED
                           |             terminal
                           v
                         FAILED -> PROCESSING

REQUESTED -> CANCELLED
APPROVED  -> CANCELLED
FAILED    -> CANCELLED
```

- `SUCCEEDED` and `CANCELLED` are terminal.
- A refund can only be created for an order and payment whose snapshots are both `PAID`.
- Every create/transition locks the payment before the order. The payment lock serializes refund amount checks across instances.
- `REQUESTED`, `APPROVED`, `PROCESSING`, and `SUCCEEDED` reserve amount. Their sum cannot exceed `payments.amount`.
- A `SUCCEEDED` transition checks the successful sum again and requires `externalReference` plus either `evidenceUrl` or `evidenceMetadata`.
- A failed bank refund can return to `PROCESSING`; a retry must pass the amount reservation check again.
- Return approval/restock is independent. Return approval creates neither a ledger refund nor a payment-success timeline event.
- Paid orders cannot use the simple cancellation route. It returns `409` with code `ORDER_PAID_REFUND_REQUIRED`; shipping/delivered stock is never blindly rolled back.

All ledger changes create an audit log, an order timeline event, and a transactional outbox notification. SePay has no refund API in this project; admins record a manual bank transfer with its reference/evidence.
