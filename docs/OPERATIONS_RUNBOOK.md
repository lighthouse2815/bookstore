# Operations runbook

## Late IPN

1. Open **Admin > Payment reconciliation**.
2. Compare payment reference, received amount, order terminal state, and SePay evidence.
3. Resolve with an audit note. Do not mark an expired/cancelled order paid or rerun stock/coupon rollback.
4. If money must return, create a separate refund ledger entry; do not alter the original late-IPN record.

## Failed refund

1. Open **Admin > Refunds**, check the payment, order, bank reference, and evidence.
2. `FAILED -> PROCESSING` is the only retry path; update the evidence/reference before `SUCCEEDED`.
3. Do not restock here. Restock remains a separately approved return operation.
4. If recovery is impossible, cancel the refund and retain the audit/timeline evidence.

## Dead outbox event

1. Inspect `/api/admin/outbox?status=DEAD` and its `lastError`; never paste event payloads into tickets if they contain customer data.
2. Fix the dependency/configuration first, then call `POST /api/admin/outbox/{id}/retry`.
3. Verify the event moves `PENDING -> PROCESSING -> SUCCEEDED`; confirm only one in-app notification exists.
4. Escalate if pending/failed/dead exceeds the configured backlog threshold or the actuator `outbox` health becomes degraded.

## Refresh-token reuse

1. Treat `AUTH_REFRESH_REUSE_DETECTED` as a compromised family, not as a normal expired session.
2. Review the sanitized audit event and device/session metadata; do not recover raw refresh tokens from logs.
3. Ask the user to sign in again. If account compromise is suspected, reset password and revoke all sessions.

## Migration failure

1. Stop rollout, keep the previous application version serving if possible, and capture Flyway version/checksum/error without secrets.
2. Do not edit V1–V14 or repair `flyway_schema_history` manually as a first response.
3. Restore from backup or prepare a new forward-only migration after identifying the failed environment condition.
