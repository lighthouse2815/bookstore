# Transactional outbox

`V14__add_refund_ledger_and_transactional_outbox.sql` adds `outbox_events` and `outbox_deliveries`.

Business services write a notification event in the same Spring transaction as order/payment/refund changes. Current producers cover order creation/POS creation, cancellation, payment expiry, payment paid, late-IPN reconciliation, and every refund state change.

`OutboxWorker` claims a bounded batch under pessimistic row locks, changes rows to `PROCESSING`, then performs delivery outside that claim transaction. Completion is persisted in a separate short transaction.

| Status | Meaning |
| --- | --- |
| `PENDING` | Ready for a worker. |
| `PROCESSING` | Claimed; stale claims are reclaimed after `OUTBOX_PROCESSING_TIMEOUT_SECONDS`. |
| `SUCCEEDED` | Delivery completed. |
| `FAILED` | Retryable failure with bounded exponential backoff. |
| `DEAD` | Retry limit reached; an admin may retry it through `/api/admin/outbox/{id}/retry`. |

`outbox_deliveries` has a unique `(event_id, consumer)` key. Notification creation and the delivery row commit together, so a worker crash after delivery but before event completion is replay-safe.

Payload is serialized JSON and rejects field names containing password, token, secret, authorization, or OTP. It is intentionally not used for OTP email challenges; those security challenges remain synchronous and do not expose token material in the outbox.

Runtime settings are in `bookstore-backend/.env.example`: `OUTBOX_ENABLED`, `OUTBOX_DELAY_MS`, `OUTBOX_BATCH_SIZE`, `OUTBOX_MAX_ATTEMPTS`, `OUTBOX_PROCESSING_TIMEOUT_SECONDS`, and `OUTBOX_BACKLOG_WARNING_THRESHOLD`.

The actuator `outbox` health contributor reports pending/failed/dead counts; Micrometer exposes `bookstore.outbox.events` by status.

Admins can inspect and filter events at `/admin/outbox`; a `DEAD` event can be requeued through the page or `POST /api/admin/outbox/{id}/retry`.
