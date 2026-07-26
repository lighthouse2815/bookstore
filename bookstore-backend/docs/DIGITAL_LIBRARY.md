# Digital Library Phase 1

## Scope

Phase 1 ships paid digital-library access inside the current bookstore flow without introducing subscriptions, rentals, DRM, or a separate fulfillment pipeline.

## Current Behavior

- `DigitalAsset` supports `downloadAllowed`, `purchaseAllowed`, and `published`.
- Public book detail only exposes safe digital metadata:
  - format
  - title
  - file name
  - price
  - `downloadAllowed`
  - `purchaseAllowed`
  - `sampleAvailable`
- Public responses do not expose storage keys, file asset ids, public sample URLs, or checksums.
- Customers can add `DIGITAL_ASSET` items to cart alongside `PHYSICAL_BOOK`.
- Digital cart lines are always quantity `1`.
- Checkout supports digital-only orders:
  - no address is required
  - shipping fee is `0`
  - shipping method is effectively pickup/no-delivery
  - COD is blocked on the frontend for digital-only checkout
- Digital library access is granted from purchased order items by `digitalAssetId`, not by book id.
- Access is granted only for digital assets that are both `published` and `purchaseAllowed`.
- Cancelling an order revokes purchased digital access for that order.
- COD orders grant digital access when the order is delivered.

## Backend Notes

- `CartService` accepts both `PHYSICAL_BOOK` and `DIGITAL_ASSET`.
- `OrderService` now loads book data for digital items during checkout so digital-only checkout can create valid `OrderItem` rows.
- `OrderService` only persists stock-updated books during checkout; digital-only orders do not save unchanged books.
- `DigitalLibraryService` signs sample/read/download URLs from private storage and enforces access checks before issuing read/download URLs.

## Frontend Notes

- `/library` is authenticated-only.
- `/library/:digitalAssetId/read` is the in-app reader route for PDF/EPUB/audio assets.
- Digital-only checkout hides address/shipping sections and uses the QR payment flow.
- Admin book digital-asset management includes `purchaseAllowed`.

## Non-Goals In This Phase

- Subscription access
- Borrow/rental access
- Offline sync
- DRM or watermarking
- Multi-file bundle delivery
