# SMOKE_TEST_FLOW

Danh sach thao tac tay de check nhanh truoc demo.

## Customer

- [ ] Register
- [ ] Verify OTP after register
- [ ] Login
- [ ] Browse books
- [ ] View book detail
- [ ] Add to cart
- [ ] Update cart item quantity
- [ ] Remove cart item
- [ ] Checkout without coupon
- [ ] Checkout with book coupon
- [ ] View order history
- [ ] View order detail

## Admin

- [ ] Login admin
- [ ] View dashboard
- [ ] Create/update/delete book
- [ ] Manage category
- [ ] Manage author
- [ ] Manage publisher
- [ ] Manage supplier
- [ ] Manage order status
- [ ] Assign shipper to eligible order
- [ ] Manage coupon / promotion

## Shipper

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
