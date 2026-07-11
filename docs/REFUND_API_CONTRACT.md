# Refund API contract

All endpoints require `ADMIN`. Responses use the existing `ApiResponse` envelope and application failures include `code`.

| Method | Path | Purpose |
| --- | --- | --- |
| `POST` | `/api/admin/orders/{orderId}/refunds` | Create `REQUESTED` ledger entry; requires `Idempotency-Key`. |
| `GET` | `/api/admin/refunds` | Paginated filter: `page`, `size`, `status`, `method`, `from`, `to`. |
| `GET` | `/api/admin/refunds/{id}` | Read ledger/detail state. |
| `PUT` | `/api/admin/refunds/{id}/approve` | `REQUESTED -> APPROVED`. |
| `PUT` | `/api/admin/refunds/{id}/processing` | `APPROVED|FAILED -> PROCESSING`. |
| `PUT` | `/api/admin/refunds/{id}/succeed` | `PROCESSING -> SUCCEEDED`; evidence required. |
| `PUT` | `/api/admin/refunds/{id}/fail` | `PROCESSING -> FAILED`; failure reason required. |
| `PUT` | `/api/admin/refunds/{id}/cancel` | Cancel a non-processing, non-terminal request. |

Create body:

```json
{
  "returnRequestId": "optional UUID",
  "amount": 120000,
  "currency": "VND",
  "reason": "Khách hủy đơn đã thanh toán",
  "method": "MANUAL_BANK_TRANSFER"
}
```

Success body:

```json
{
  "externalReference": "BANK-REF-20260711-01",
  "evidenceUrl": "https://approved-storage.example/refunds/receipt.pdf",
  "evidenceMetadata": "optional sanitized bank receipt metadata"
}
```

Important codes: `REFUND_ORDER_NOT_PAID`, `REFUND_AMOUNT_EXCEEDS_REMAINING`, `REFUND_INVALID_TRANSITION`, `REFUND_EVIDENCE_REQUIRED`, `REFUND_RETURN_REQUEST_INVALID`, and `ORDER_PAID_REFUND_REQUIRED`.
