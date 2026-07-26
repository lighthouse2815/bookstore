# SMOKE_TEST_FLOW

Danh sach thao tac tay de check nhanh truoc demo.

## Preflight

- [ ] `GET /actuator/health` tra `UP`
- [ ] Demo deploy: `APP_SWAGGER_ENABLED=true` thi `/swagger-ui/index.html` mo duoc
- [ ] Real prod deploy: `APP_SWAGGER_ENABLED=false` thi `/v3/api-docs` va `/swagger-ui/index.html` tra `404`
- [ ] Demo accounts/password chi lay tu env, khong copy secret vao docs
- [ ] Neu dung demo seed, co the chay `D:\bookstore\scripts\smoke-demo.ps1`

## Customer

- [ ] Login user demo (`minhanh.nguyen` + `APP_DEMO_USER_PASSWORD`)
- [ ] Books list load du
- [ ] Categories load du
- [ ] View book detail / page detail
- [ ] Recently viewed UI khong loi sau khi mo book detail
- [ ] Add/remove wishlist
- [ ] Add to cart
- [ ] Update cart item quantity
- [ ] Remove cart item
- [ ] `GET /api/cart/best-coupon` tra coupon seed hop le (`DOCHEM03` tren fresh demo DB), khong can insert DB thu cong
- [ ] Apply coupon tu best-coupon suggestion
- [ ] Checkout without coupon
- [ ] Checkout with book coupon
- [ ] Create a `BANK_TRANSFER_QR` order, confirm the QR countdown is fixed, and retry the same checkout request with the same idempotency key: the expiry must not move
- [ ] Let a QR payment expire in a smoke database; confirm the scheduler sets payment/order to `EXPIRED`/`CANCELLED`, returns stock/coupon once, and writes timeline/audit/notification
- [ ] Cancel a pending unpaid order as its owner with a reason; confirm the order/payment are both `CANCELLED` and timeline/notification are present
- [ ] Attempt to cancel a paid, shipping, or delivered order: API/UI must refuse it
- [ ] Send a valid SePay callback after expiry/cancellation: confirm no order resurrection or stock re-debit, then verify exactly one open reconciliation issue with the received external transaction ID
- [ ] View order history
- [ ] View order detail / timeline neu UI co hien

## Admin

- [ ] Login admin
- [ ] View dashboard tai `/admin`
- [ ] `/admin/dashboard` redirect/render dashboard thanh cong
- [ ] Dashboard co revenue, orders, top books, low stock
- [ ] Open admin order list
- [ ] Tim pending order vua tao trong customer flow
- [ ] Confirm hoac cancel order pending theo muc tieu demo
- [ ] Open `/admin/payment-reconciliation`, filter an issue, and resolve it with a mandatory note; no automatic refund is performed
- [ ] Neu can demo rollback stock/coupon, chi test tren DB smoke/demo rieng

## Optional shipper / staff

- [ ] Login shipper
- [ ] View assigned orders
- [ ] Open shipment detail
- [ ] Update shipment to `PICKED_UP`
- [ ] Update shipment to `DELIVERING`
- [ ] Update shipment to `DELIVERED`
- [ ] Mark shipment `FAILED` when testing failure path

## Desktop

- [ ] Build app
- [ ] Login
- [ ] Search books
- [ ] Add items to POS cart
- [ ] Create POS order
- [ ] View receipt preview
- [ ] Export receipt `.txt`
- [ ] Lookup recent order
- [ ] Check inventory view
- [ ] Reports - Not implemented yet

## Notes

- Neu chuc nang chua co that, danh dau ro `Not implemented yet`, khong ghi pass gia.
- Neu shipper khong thay don, quay lai admin flow va xac nhan da assign shipment.
- Neu mobile/ship app test tren may that, backend URL phai la IP LAN thay vi `localhost`.
- Thu tu UI demo khuyen nghi: `D:\bookstore\docs\DEMO_SCRIPT.md`
## Authentication/session smoke additions

1. Log in from the website, reload, and confirm the account restores through `/api/auth/web/refresh`.
2. Confirm DevTools storage has no refresh token and the `BOOKSTORE_REFRESH` cookie is HttpOnly; production must mark it Secure.
3. Capture an old native refresh token after a rotation, replay it once, and confirm every session in its family is revoked.
4. Open two devices, revoke one through Profile > Bảo mật & thiết bị, and confirm its next protected request fails.
5. Complete password reset and confirm every device is logged out.
6. Send invalid OTPs until `OTP_MAX_ATTEMPTS`, then confirm resend invalidates the old code.
7. Trigger account/IP login throttles and verify `429` plus `Retry-After`; wait for policy expiry and log in successfully.
8. Verify Android upgrade migrates existing tokens, no plain `bookstore_tokens` values remain, and backup is disabled.
9. Verify desktop/shipper still rotate native refresh tokens and Google sign-in remains available.

## Staging automation

Run `pnpm smoke:staging` from `bookstore-website` using the disposable-account variables listed in `STAGING_SMOKE_REPORT.md`. The script intentionally does not print credentials, cookies, tokens, or response bodies.
