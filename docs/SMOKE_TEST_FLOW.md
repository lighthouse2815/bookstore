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
