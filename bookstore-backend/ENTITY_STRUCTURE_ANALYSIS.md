# Bookstore Backend - JPA Entity Structure Analysis

## Summary
- **Total Entities:** 30
- **Analysis Date:** 2026-06-16
- **Focus:** CartJpaEntity, CartItemJpaEntity, CouponUsageJpaEntity, CategoryJpaEntity + all core entities

---

## Quick Reference: Relationship Types Legend
- **@OneToOne** → Single object reference (1-to-1 mapping)
- **@OneToMany** → Collection of objects (1-to-many mapping, typically List<>)
- **@ManyToOne** → Foreign key reference to single parent (many-to-1 mapping)
- **@ManyToMany** → Join table with Set<> collection (many-to-many mapping)
- **@JoinColumn(name=...)** → Specifies foreign key column name in database

---

# 1. CORE DOMAIN ENTITIES (Focus Areas)

## 1.1 CartJpaEntity
**Table:** `carts`  
**Key Feature:** One-to-One with User, One-to-Many with CartItems

| Type | Property Name | Java Type | DB Column/FK | Nullable | Notes |
|------|---------------|-----------|--------------|----------|-------|
| **PK** | id | UUID | id | NO | Identity |
| **RELATIONSHIP** | user | UserJpaEntity | user_id (FK) | NO | @OneToOne, LAZY, unique |
| **RELATIONSHIP** | items | List<CartItemJpaEntity> | (mapped by "cart") | - | @OneToMany, LAZY, orphanRemoval |
| **COLUMN** | createdAt | Instant | created_at | NO | - |
| **COLUMN** | updatedAt | Instant | updated_at | NO | - |

**Analysis:**
- **Relationships (2):** user (→UserJpaEntity), items (←CartItemJpaEntity)
- **Simple Fields (2):** createdAt, updatedAt
- **Foreign Keys:** user_id references users.id

---

## 1.2 CartItemJpaEntity
**Table:** `cart_items` (with UNIQUE constraint on cart_id + book_id)  
**Key Feature:** Junction entity between Cart and Book

| Type | Property Name | Java Type | DB Column/FK | Nullable | Notes |
|------|---------------|-----------|--------------|----------|-------|
| **PK** | id | UUID | id | NO | Identity |
| **RELATIONSHIP** | cart | CartJpaEntity | cart_id (FK) | NO | @ManyToOne, LAZY |
| **RELATIONSHIP** | book | BookJpaEntity | book_id (FK) | NO | @ManyToOne, LAZY |
| **COLUMN** | quantity | Integer | quantity | NO | - |
| **COLUMN** | createdAt | Instant | created_at | NO | - |
| **COLUMN** | updatedAt | Instant | updated_at | NO | - |

**Analysis:**
- **Relationships (2):** cart (→CartJpaEntity), book (→BookJpaEntity)
- **Simple Fields (3):** quantity, createdAt, updatedAt
- **Foreign Keys:** cart_id, book_id
- **Composite Unique:** (cart_id, book_id)

---

## 1.3 CouponUsageJpaEntity
**Table:** `coupon_usages`  
**Key Feature:** Tracks coupon application to orders by users

| Type | Property Name | Java Type | DB Column/FK | Nullable | Notes |
|------|---------------|-----------|--------------|----------|-------|
| **PK** | id | UUID | id | NO | Identity |
| **RELATIONSHIP** | coupon | CouponJpaEntity | coupon_id (FK) | NO | @ManyToOne, LAZY |
| **RELATIONSHIP** | user | UserJpaEntity | user_id (FK) | NO | @ManyToOne, LAZY |
| **RELATIONSHIP** | order | OrderJpaEntity | order_id (FK) | NO | @ManyToOne, LAZY |
| **COLUMN** | discountAmount | BigDecimal | discount_amount | NO | precision 19, scale 2 |
| **COLUMN** | usedAt | Instant | used_at | NO | - |

**Analysis:**
- **Relationships (3):** coupon (→CouponJpaEntity), user (→UserJpaEntity), order (→OrderJpaEntity)
- **Simple Fields (2):** discountAmount, usedAt
- **Foreign Keys:** coupon_id, user_id, order_id

---

## 1.4 CategoryJpaEntity
**Table:** `categories`  
**Key Feature:** Simple lookup entity, NO relationships

| Type | Property Name | Java Type | DB Column/FK | Nullable | Notes |
|------|---------------|-----------|--------------|----------|-------|
| **PK** | id | UUID | id | - | Identity |
| **COLUMN** | name | String | name | NO | length 100, unique |
| **COLUMN** | description | String | description | YES | TEXT type |
| **COLUMN** | createdAt | Instant | created_at | NO | - |
| **COLUMN** | updatedAt | Instant | updated_at | NO | - |
| **COLUMN** | deletedAt | Instant | deleted_at | YES | Soft delete |

**Analysis:**
- **Relationships:** NONE (pure lookup entity)
- **Simple Fields (5):** name, description, createdAt, updatedAt, deletedAt
- **Note:** Referenced by BookJpaEntity (many-to-one)

---

# 2. USER & AUTHENTICATION ENTITIES

## 2.1 UserJpaEntity
**Table:** `users`  
**Key Feature:** Central user entity with roles and multiple relationships

| Type | Property Name | Java Type | DB Column/FK | Nullable | Notes |
|------|---------------|-----------|--------------|----------|-------|
| **PK** | id | UUID | id | NO | Identity |
| **COLUMN** | username | String | username | NO | length 100, unique |
| **COLUMN** | passwordHash | String | password_hash | YES | - |
| **COLUMN** | phoneNumber | String | phone_number | YES | length 20, unique |
| **COLUMN** | email | String | email | NO | length 255, unique |
| **COLUMN** | status | UserStatus (Enum) | status | NO | EnumType.STRING |
| **COLUMN** | locked | boolean | locked | NO | - |
| **RELATIONSHIP** | roles | Set<RoleJpaEntity> | (join table) | - | @ManyToMany, LAZY, via user_roles |
| **COLUMN** | createdAt | Instant | created_at | NO | - |
| **COLUMN** | updatedAt | Instant | updated_at | NO | - |
| **COLUMN** | deletedAt | Instant | deleted_at | YES | Soft delete |

**Analysis:**
- **Relationships (1 M2M):** roles (→RoleJpaEntity via user_roles join table)
- **Simple Fields (9):** username, passwordHash, phoneNumber, email, status, locked, createdAt, updatedAt, deletedAt
- **Reverse Relationships:** Referenced by Cart, Orders, Notifications, UserAddresses, Reviews, CouponUsages, etc.
- **ManyToMany Join Table:** `user_roles` (user_id, role_id)

---

## 2.2 ProfileJpaEntity
**Table:** `profiles`  
**Key Feature:** One-to-One with User, holds personal information

| Type | Property Name | Java Type | DB Column/FK | Nullable | Notes |
|------|---------------|-----------|--------------|----------|-------|
| **PK** | id | UUID | id | NO | Identity |
| **RELATIONSHIP** | user | UserJpaEntity | user_id (FK) | NO | @OneToOne, LAZY, unique |
| **COLUMN** | lastName | String | last_name | YES | length 100 |
| **COLUMN** | firstName | String | first_name | YES | length 100 |
| **COLUMN** | avatarUrl | String | avatar_url | YES | length 500 |
| **COLUMN** | gender | Gender (Enum) | gender | YES | EnumType.STRING |
| **COLUMN** | dateOfBirth | LocalDate | date_of_birth | YES | - |
| **COLUMN** | createdAt | Instant | created_at | NO | - |
| **COLUMN** | updatedAt | Instant | updated_at | NO | - |
| **COLUMN** | deletedAt | Instant | deleted_at | YES | - |

**Analysis:**
- **Relationships (1):** user (→UserJpaEntity, unique FK)
- **Simple Fields (8):** lastName, firstName, avatarUrl, gender, dateOfBirth, createdAt, updatedAt, deletedAt
- **Foreign Keys:** user_id (unique)

---

## 2.3 UserAddressJpaEntity
**Table:** `user_addresses`  
**Key Feature:** One-to-Many from User (user can have multiple addresses)

| Type | Property Name | Java Type | DB Column/FK | Nullable | Notes |
|------|---------------|-----------|--------------|----------|-------|
| **PK** | id | UUID | id | NO | Identity |
| **RELATIONSHIP** | user | UserJpaEntity | user_id (FK) | NO | @ManyToOne, LAZY |
| **COLUMN** | receiverName | String | receiver_name | NO | length 255 |
| **COLUMN** | receiverPhone | String | receiver_phone | NO | length 20 |
| **COLUMN** | receiverAddress | String | receiver_address | NO | TEXT type |
| **COLUMN** | defaultAddress | boolean | default_address | NO | - |
| **COLUMN** | createdAt | Instant | created_at | NO | - |
| **COLUMN** | updatedAt | Instant | updated_at | NO | - |
| **COLUMN** | deletedAt | Instant | deleted_at | YES | Soft delete |

**Analysis:**
- **Relationships (1):** user (→UserJpaEntity)
- **Simple Fields (7):** receiverName, receiverPhone, receiverAddress, defaultAddress, createdAt, updatedAt, deletedAt
- **Foreign Keys:** user_id

---

## 2.4 UserOtpJpaEntity
**Table:** `user_otps` (indexed on user_id, purpose, created_at)  
**Key Feature:** Stores OTP verification tokens

| Type | Property Name | Java Type | DB Column/FK | Nullable | Notes |
|------|---------------|-----------|--------------|----------|-------|
| **PK** | id | UUID | id | NO | Identity |
| **RELATIONSHIP** | user | UserJpaEntity | user_id (FK) | NO | @ManyToOne, LAZY |
| **COLUMN** | purpose | OtpPurpose (Enum) | purpose | NO | EnumType.STRING |
| **COLUMN** | otpHash | String | otp_hash | NO | length 100 |
| **COLUMN** | expiresAt | Instant | expires_at | NO | - |
| **COLUMN** | verifiedAt | Instant | verified_at | YES | - |
| **COLUMN** | invalidatedAt | Instant | invalidated_at | YES | - |
| **COLUMN** | createdAt | Instant | created_at | NO | - |
| **COLUMN** | updatedAt | Instant | updated_at | NO | - |

**Analysis:**
- **Relationships (1):** user (→UserJpaEntity)
- **Simple Fields (8):** purpose, otpHash, expiresAt, verifiedAt, invalidatedAt, createdAt, updatedAt
- **Index:** Composite on (user_id, purpose, created_at)

---

## 2.5 UserAuthIdentityJpaEntity
**Table:** `user_auth_identities`  
**Key Feature:** OAuth/SAML identity providers linked to users

| Type | Property Name | Java Type | DB Column/FK | Nullable | Notes |
|------|---------------|-----------|--------------|----------|-------|
| **PK** | id | UUID | id | NO | Identity |
| **RELATIONSHIP** | user | UserJpaEntity | user_id (FK) | NO | @ManyToOne, LAZY |
| **COLUMN** | provider | AuthProvider (Enum) | provider | NO | EnumType.STRING, length 50 |
| **COLUMN** | providerSubject | String | provider_subject | NO | length 255 |
| **COLUMN** | providerEmail | String | provider_email | YES | length 255 |

**Analysis:**
- **Relationships (1):** user (→UserJpaEntity)
- **Simple Fields (3):** provider, providerSubject, providerEmail
- **Composite Unique:** (provider, provider_subject) and (user_id, provider)

---

## 2.6 RefreshTokenJpaEntity
**Table:** `refresh_tokens`  
**Key Feature:** JWT refresh token storage

| Type | Property Name | Java Type | DB Column/FK | Nullable | Notes |
|------|---------------|-----------|--------------|----------|-------|
| **PK** | id | UUID | id | NO | Identity |
| **RELATIONSHIP** | user | UserJpaEntity | user_id (FK) | NO | @ManyToOne, LAZY |
| **COLUMN** | token | String | token | NO | length 512, unique |
| **COLUMN** | expiresAt | Instant | expires_at | NO | - |
| **COLUMN** | revoked | boolean | revoked | NO | - |
| **COLUMN** | createdAt | Instant | created_at | NO | - |

**Analysis:**
- **Relationships (1):** user (→UserJpaEntity)
- **Simple Fields (4):** token, expiresAt, revoked, createdAt

---

## 2.7 PasswordResetTokenJpaEntity
**Table:** `password_reset_tokens` (indexed on user_id, created_at)  
**Key Feature:** Password reset token management

| Type | Property Name | Java Type | DB Column/FK | Nullable | Notes |
|------|---------------|-----------|--------------|----------|-------|
| **PK** | id | UUID | id | NO | Identity |
| **RELATIONSHIP** | user | UserJpaEntity | user_id (FK) | NO | @ManyToOne, LAZY |
| **COLUMN** | tokenHash | String | token_hash | NO | length 64, unique |
| **COLUMN** | expiresAt | Instant | expires_at | NO | - |
| **COLUMN** | usedAt | Instant | used_at | YES | - |
| **COLUMN** | createdAt | Instant | created_at | NO | - |

**Analysis:**
- **Relationships (1):** user (→UserJpaEntity)
- **Simple Fields (4):** tokenHash, expiresAt, usedAt, createdAt

---

# 3. BOOK & CATALOG ENTITIES

## 3.1 BookJpaEntity
**Table:** `books`  
**Key Feature:** Core book entity with multiple relationships

| Type | Property Name | Java Type | DB Column/FK | Nullable | Notes |
|------|---------------|-----------|--------------|----------|-------|
| **PK** | id | UUID | id | NO | Identity |
| **COLUMN** | title | String | title | NO | length 255 |
| **COLUMN** | isbn | String | isbn | YES | length 32 |
| **COLUMN** | description | String | description | YES | TEXT type |
| **COLUMN** | price | BigDecimal | price | NO | precision 19, scale 2 |
| **COLUMN** | stockQuantity | Integer | stock_quantity | NO | - |
| **COLUMN** | imageUrl | String | image_url | YES | TEXT type |
| **RELATIONSHIP** | images | List<BookImageJpaEntity> | (mapped by "book") | - | @OneToMany, LAZY, orphanRemoval |
| **RELATIONSHIP** | detail | BookDetailJpaEntity | (mapped by "book") | - | @OneToOne, LAZY, orphanRemoval |
| **RELATIONSHIP** | category | CategoryJpaEntity | category_id (FK) | NO | @ManyToOne, LAZY |
| **RELATIONSHIP** | author | AuthorJpaEntity | author_id (FK) | NO | @ManyToOne, LAZY |
| **RELATIONSHIP** | publisher | PublisherJpaEntity | publisher_id (FK) | NO | @ManyToOne, LAZY |
| **COLUMN** | createdAt | Instant | created_at | NO | - |
| **COLUMN** | updatedAt | Instant | updated_at | NO | - |
| **COLUMN** | deletedAt | Instant | deleted_at | YES | Soft delete |

**Analysis:**
- **Relationships (5):** images (←BookImageJpaEntity), detail (←BookDetailJpaEntity), category (→CategoryJpaEntity), author (→AuthorJpaEntity), publisher (→PublisherJpaEntity)
- **Simple Fields (8):** title, isbn, description, price, stockQuantity, imageUrl, createdAt, updatedAt, deletedAt
- **Foreign Keys:** category_id, author_id, publisher_id

---

## 3.2 BookDetailJpaEntity
**Table:** `book_details`  
**Key Feature:** Extended book metadata (one-to-one with Book)

| Type | Property Name | Java Type | DB Column/FK | Nullable | Notes |
|------|---------------|-----------|--------------|----------|-------|
| **PK** | id | UUID | id | NO | Identity |
| **RELATIONSHIP** | book | BookJpaEntity | book_id (FK) | NO | @OneToOne, LAZY, unique |
| **COLUMN** | pageCount | Integer | page_count | YES | - |
| **COLUMN** | publicationYear | Integer | publication_year | YES | - |
| **COLUMN** | language | String | language | YES | length 100 |
| **COLUMN** | coverType | String | cover_type | YES | length 100 |
| **COLUMN** | dimensions | String | dimensions | YES | length 100 |
| **COLUMN** | weight | Integer | weight | YES | - |
| **COLUMN** | translator | String | translator | YES | length 255 |
| **COLUMN** | edition | String | edition | YES | length 100 |

**Analysis:**
- **Relationships (1):** book (→BookJpaEntity, unique FK)
- **Simple Fields (8):** pageCount, publicationYear, language, coverType, dimensions, weight, translator, edition
- **Foreign Keys:** book_id (unique)

---

## 3.3 BookImageJpaEntity
**Table:** `book_images`  
**Key Feature:** One-to-Many from Book (multiple images per book)

| Type | Property Name | Java Type | DB Column/FK | Nullable | Notes |
|------|---------------|-----------|--------------|----------|-------|
| **PK** | id | UUID | id | NO | Identity |
| **RELATIONSHIP** | book | BookJpaEntity | book_id (FK) | NO | @ManyToOne, LAZY |
| **COLUMN** | imageUrl | String | image_url | NO | TEXT type |
| **COLUMN** | primaryImage | Boolean | primary_image | NO | - |
| **COLUMN** | sortOrder | Integer | sort_order | NO | - |
| **COLUMN** | altText | String | alt_text | YES | TEXT type |
| **COLUMN** | createdAt | Instant | created_at | NO | - |

**Analysis:**
- **Relationships (1):** book (→BookJpaEntity)
- **Simple Fields (6):** imageUrl, primaryImage, sortOrder, altText, createdAt
- **Note:** Ordered by (sortOrder, createdAt) in BookJpaEntity

---

## 3.4 AuthorJpaEntity
**Table:** `authors`  
**Key Feature:** Simple author lookup entity

| Type | Property Name | Java Type | DB Column/FK | Nullable | Notes |
|------|---------------|-----------|--------------|----------|-------|
| **PK** | id | UUID | id | - | Identity |
| **COLUMN** | name | String | name | NO | length 100, unique |
| **COLUMN** | biography | String | biography | YES | TEXT type |
| **COLUMN** | avatarUrl | String | avatar_url | YES | length 500 |
| **COLUMN** | birthYear | Integer | birth_year | YES | - |
| **COLUMN** | deathYear | Integer | death_year | YES | - |
| **COLUMN** | createdAt | Instant | created_at | NO | - |
| **COLUMN** | updatedAt | Instant | updated_at | NO | - |
| **COLUMN** | deletedAt | Instant | deleted_at | YES | Soft delete |

**Analysis:**
- **Relationships:** NONE (pure lookup entity, referenced by BookJpaEntity)
- **Simple Fields (8):** name, biography, avatarUrl, birthYear, deathYear, createdAt, updatedAt, deletedAt

---

## 3.5 PublisherJpaEntity
**Table:** `publishers`  
**Key Feature:** Simple publisher lookup entity

| Type | Property Name | Java Type | DB Column/FK | Nullable | Notes |
|------|---------------|-----------|--------------|----------|-------|
| **PK** | id | UUID | id | - | Identity |
| **COLUMN** | name | String | name | NO | length 100, unique |
| **COLUMN** | description | String | description | YES | TEXT type |
| **COLUMN** | createdAt | Instant | created_at | NO | - |
| **COLUMN** | updatedAt | Instant | updated_at | NO | - |
| **COLUMN** | deletedAt | Instant | deleted_at | YES | Soft delete |

**Analysis:**
- **Relationships:** NONE (pure lookup entity, referenced by BookJpaEntity)
- **Simple Fields (5):** name, description, createdAt, updatedAt, deletedAt

---

# 4. COUPON & DISCOUNT ENTITIES

## 4.1 CouponJpaEntity
**Table:** `coupons`  
**Key Feature:** Master coupon definition with targets

| Type | Property Name | Java Type | DB Column/FK | Nullable | Notes |
|------|---------------|-----------|--------------|----------|-------|
| **PK** | id | UUID | id | NO | Identity |
| **RELATIONSHIP** | targets | List<CouponTargetJpaEntity> | (mapped by "coupon") | - | @OneToMany, LAZY, orphanRemoval |
| **COLUMN** | code | String | code | NO | length 100, unique |
| **COLUMN** | description | String | description | YES | TEXT type |
| **COLUMN** | couponType | CouponType (Enum) | coupon_type | NO | EnumType.STRING, length 32 |
| **COLUMN** | discountType | CouponDiscountType (Enum) | discount_type | NO | EnumType.STRING, length 32 |
| **COLUMN** | discountValue | BigDecimal | discount_value | NO | precision 19, scale 2 |
| **COLUMN** | minOrderAmount | BigDecimal | min_order_amount | NO | precision 19, scale 2 |
| **COLUMN** | maxDiscountAmount | BigDecimal | max_discount_amount | YES | precision 19, scale 2 |
| **COLUMN** | maxUsageCount | Integer | max_usage_count | YES | - |
| **COLUMN** | usedCount | Integer | used_count | NO | - |
| **COLUMN** | startsAt | Instant | starts_at | NO | - |
| **COLUMN** | expiresAt | Instant | expires_at | NO | - |
| **COLUMN** | active | boolean | active | NO | - |
| **COLUMN** | createdAt | Instant | created_at | NO | - |
| **COLUMN** | updatedAt | Instant | updated_at | NO | - |
| **COLUMN** | deletedAt | Instant | deleted_at | YES | Soft delete |

**Analysis:**
- **Relationships (1):** targets (←CouponTargetJpaEntity)
- **Simple Fields (14):** code, description, couponType, discountType, discountValue, minOrderAmount, maxDiscountAmount, maxUsageCount, usedCount, startsAt, expiresAt, active, createdAt, updatedAt, deletedAt

---

## 4.2 CouponTargetJpaEntity
**Table:** `coupon_targets`  
**Key Feature:** Specifies which entities (books/categories) a coupon applies to

| Type | Property Name | Java Type | DB Column/FK | Nullable | Notes |
|------|---------------|-----------|--------------|----------|-------|
| **PK** | id | UUID | id | NO | Identity |
| **RELATIONSHIP** | coupon | CouponJpaEntity | coupon_id (FK) | NO | @ManyToOne, LAZY |
| **COLUMN** | targetType | CouponTargetType (Enum) | target_type | NO | EnumType.STRING, length 32 |
| **COLUMN** | targetId | UUID | target_id | YES | Foreign reference (not FK) |
| **COLUMN** | createdAt | Instant | created_at | NO | - |
| **COLUMN** | updatedAt | Instant | updated_at | NO | - |

**Analysis:**
- **Relationships (1):** coupon (→CouponJpaEntity)
- **Simple Fields (4):** targetType, targetId, createdAt, updatedAt
- **Note:** targetId is a generic UUID, not a traditional FK constraint (polymorphic reference)

---

# 5. ORDER & TRANSACTION ENTITIES

## 5.1 OrderJpaEntity
**Table:** `orders`  
**Key Feature:** Primary order entity with items and coupon references

| Type | Property Name | Java Type | DB Column/FK | Nullable | Notes |
|------|---------------|-----------|--------------|----------|-------|
| **PK** | id | UUID | id | NO | Identity |
| **COLUMN** | orderCode | String | order_code | NO | length 50, unique |
| **RELATIONSHIP** | user | UserJpaEntity | user_id (FK) | NO | @ManyToOne, LAZY |
| **RELATIONSHIP** | items | List<OrderItemJpaEntity> | (mapped by "order") | - | @OneToMany, LAZY, orphanRemoval |
| **COLUMN** | productTotal | BigDecimal | product_total | NO | precision 19, scale 2 |
| **COLUMN** | totalAmount | BigDecimal | total_amount | NO | precision 19, scale 2 |
| **COLUMN** | discountAmount | BigDecimal | discount_amount | NO | precision 19, scale 2 |
| **COLUMN** | shippingFee | BigDecimal | shipping_fee | NO | precision 19, scale 2 |
| **COLUMN** | shippingDiscount | BigDecimal | shipping_discount | NO | precision 19, scale 2 |
| **COLUMN** | couponDiscount | BigDecimal | coupon_discount | NO | precision 19, scale 2 |
| **COLUMN** | finalAmount | BigDecimal | final_amount | NO | precision 19, scale 2 |
| **RELATIONSHIP** | bookCoupon | CouponJpaEntity | book_coupon_id (FK) | YES | @ManyToOne, LAZY |
| **COLUMN** | bookCouponCode | String | book_coupon_code | YES | length 100 |
| **RELATIONSHIP** | shippingCoupon | CouponJpaEntity | shipping_coupon_id (FK) | YES | @ManyToOne, LAZY |
| **COLUMN** | shippingCouponCode | String | shipping_coupon_code | YES | length 100 |
| **COLUMN** | paymentMethod | PaymentMethod (Enum) | payment_method | NO | EnumType.STRING, length 32 |
| **COLUMN** | paymentStatus | PaymentStatus (Enum) | payment_status | NO | EnumType.STRING, length 32 |
| **COLUMN** | status | OrderStatus (Enum) | status | NO | EnumType.STRING, length 32 |
| **COLUMN** | receiverName | String | receiver_name | NO | length 255 |
| **COLUMN** | receiverPhone | String | receiver_phone | NO | length 20 |
| **COLUMN** | receiverAddress | String | receiver_address | NO | TEXT type |
| **COLUMN** | createdAt | Instant | created_at | NO | - |
| **COLUMN** | updatedAt | Instant | updated_at | NO | - |
| **COLUMN** | cancelledAt | Instant | cancelled_at | YES | - |

**Analysis:**
- **Relationships (4):** user (→UserJpaEntity), items (←OrderItemJpaEntity), bookCoupon (→CouponJpaEntity), shippingCoupon (→CouponJpaEntity)
- **Simple Fields (18):** orderCode, productTotal, totalAmount, discountAmount, shippingFee, shippingDiscount, couponDiscount, finalAmount, bookCouponCode, shippingCouponCode, paymentMethod, paymentStatus, status, receiverName, receiverPhone, receiverAddress, createdAt, updatedAt, cancelledAt
- **Foreign Keys:** user_id, book_coupon_id, shipping_coupon_id
- **Note:** Two references to CouponJpaEntity for different coupon types

---

## 5.2 OrderItemJpaEntity
**Table:** `order_items`  
**Key Feature:** Line items in orders (snapshot of book state at order time)

| Type | Property Name | Java Type | DB Column/FK | Nullable | Notes |
|------|---------------|-----------|--------------|----------|-------|
| **PK** | id | UUID | id | NO | Identity |
| **RELATIONSHIP** | order | OrderJpaEntity | order_id (FK) | NO | @ManyToOne, LAZY |
| **RELATIONSHIP** | book | BookJpaEntity | book_id (FK) | NO | @ManyToOne, LAZY |
| **COLUMN** | bookTitle | String | book_title | NO | length 255 |
| **COLUMN** | unitPrice | BigDecimal | unit_price | NO | precision 19, scale 2 |
| **COLUMN** | quantity | Integer | quantity | NO | - |
| **COLUMN** | lineTotal | BigDecimal | line_total | NO | precision 19, scale 2 |

**Analysis:**
- **Relationships (2):** order (→OrderJpaEntity), book (→BookJpaEntity)
- **Simple Fields (4):** bookTitle, unitPrice, quantity, lineTotal
- **Foreign Keys:** order_id, book_id
- **Note:** bookTitle is snapshot (denormalized) at order time

---

## 5.3 PaymentJpaEntity
**Table:** `payments` (indexed on order_id, reference_code, transaction_id)  
**Key Feature:** Payment transaction records (NO relationship to Order, uses orderId as simple field)

| Type | Property Name | Java Type | DB Column/FK | Nullable | Notes |
|------|---------------|-----------|--------------|----------|-------|
| **PK** | id | UUID | id | NO | Identity |
| **COLUMN** | orderId | UUID | order_id | NO | Simple field (not FK) |
| **COLUMN** | provider | PaymentProvider (Enum) | provider | NO | EnumType.STRING, length 32 |
| **COLUMN** | status | PaymentStatus (Enum) | status | NO | EnumType.STRING, length 32 |
| **COLUMN** | amount | BigDecimal | amount | NO | precision 19, scale 2 |
| **COLUMN** | merchantId | String | merchant_id | YES | length 100 |
| **COLUMN** | transactionId | String | transaction_id | YES | length 100 |
| **COLUMN** | referenceCode | String | reference_code | NO | length 100 |
| **COLUMN** | transferContent | String | transfer_content | NO | length 255 |
| **COLUMN** | gateway | String | gateway | YES | length 100 |
| **COLUMN** | paidAt | Instant | paid_at | YES | - |
| **COLUMN** | createdAt | Instant | created_at | NO | - |
| **COLUMN** | updatedAt | Instant | updated_at | NO | - |

**Analysis:**
- **Relationships:** NONE (orderId is a simple UUID field, not a JPA FK)
- **Simple Fields (12):** orderId, provider, status, amount, merchantId, transactionId, referenceCode, transferContent, gateway, paidAt, createdAt, updatedAt
- **Indexes:** Composite indexes on (order_id), (reference_code), (transaction_id)

---

# 6. REVIEW & FEEDBACK ENTITIES

## 6.1 ReviewJpaEntity
**Table:** `reviews` (UNIQUE constraint on order_item_id)  
**Key Feature:** User product reviews linked to order items

| Type | Property Name | Java Type | DB Column/FK | Nullable | Notes |
|------|---------------|-----------|--------------|----------|-------|
| **PK** | id | UUID | id | NO | Identity |
| **RELATIONSHIP** | user | UserJpaEntity | user_id (FK) | NO | @ManyToOne, LAZY |
| **RELATIONSHIP** | book | BookJpaEntity | book_id (FK) | NO | @ManyToOne, LAZY |
| **RELATIONSHIP** | orderItem | OrderItemJpaEntity | order_item_id (FK) | NO | @OneToOne, LAZY, unique |
| **COLUMN** | rating | Integer | rating | NO | - |
| **COLUMN** | comment | String | comment | YES | TEXT type |
| **COLUMN** | createdAt | Instant | created_at | NO | - |
| **COLUMN** | updatedAt | Instant | updated_at | NO | - |
| **COLUMN** | deletedAt | Instant | deleted_at | YES | Soft delete |

**Analysis:**
- **Relationships (3):** user (→UserJpaEntity), book (→BookJpaEntity), orderItem (→OrderItemJpaEntity, unique)
- **Simple Fields (6):** rating, comment, createdAt, updatedAt, deletedAt
- **Foreign Keys:** user_id, book_id, order_item_id (unique)

---

# 7. INVENTORY & SUPPLIER ENTITIES

## 7.1 StockMovementJpaEntity
**Table:** `stock_movements`  
**Key Feature:** Audit trail for inventory changes

| Type | Property Name | Java Type | DB Column/FK | Nullable | Notes |
|------|---------------|-----------|--------------|----------|-------|
| **PK** | id | UUID | id | NO | Identity |
| **RELATIONSHIP** | book | BookJpaEntity | book_id (FK) | NO | @ManyToOne, LAZY |
| **COLUMN** | type | StockMovementType (Enum) | type | NO | EnumType.STRING, length 32 |
| **COLUMN** | quantity | Integer | quantity | NO | - |
| **COLUMN** | beforeQuantity | Integer | before_quantity | NO | - |
| **COLUMN** | afterQuantity | Integer | after_quantity | NO | - |
| **COLUMN** | referenceId | UUID | reference_id | YES | Generic ref (not FK) |
| **COLUMN** | referenceType | String | reference_type | NO | length 50 |
| **COLUMN** | note | String | note | YES | TEXT type |
| **COLUMN** | createdAt | Instant | created_at | NO | - |
| **RELATIONSHIP** | createdBy | UserJpaEntity | created_by (FK) | NO | @ManyToOne, LAZY |

**Analysis:**
- **Relationships (2):** book (→BookJpaEntity), createdBy (→UserJpaEntity)
- **Simple Fields (8):** type, quantity, beforeQuantity, afterQuantity, referenceId, referenceType, note, createdAt
- **Foreign Keys:** book_id, created_by
- **Note:** referenceId is a generic UUID for polymorphic references

---

## 7.2 SupplierJpaEntity
**Table:** `suppliers`  
**Key Feature:** Simple supplier lookup entity

| Type | Property Name | Java Type | DB Column/FK | Nullable | Notes |
|------|---------------|-----------|--------------|----------|-------|
| **PK** | id | UUID | id | NO | Identity |
| **COLUMN** | name | String | name | NO | length 100, unique |
| **COLUMN** | phone | String | phone | YES | length 20 |
| **COLUMN** | email | String | email | YES | length 255 |
| **COLUMN** | address | String | address | YES | TEXT type |
| **COLUMN** | note | String | note | YES | TEXT type |
| **COLUMN** | createdAt | Instant | created_at | NO | - |
| **COLUMN** | updatedAt | Instant | updated_at | NO | - |
| **COLUMN** | deletedAt | Instant | deleted_at | YES | Soft delete |

**Analysis:**
- **Relationships:** NONE (pure lookup entity, referenced by ImportReceiptJpaEntity)
- **Simple Fields (8):** name, phone, email, address, note, createdAt, updatedAt, deletedAt

---

## 7.3 ImportReceiptJpaEntity
**Table:** `import_receipts`  
**Key Feature:** Supplier import/purchase orders for stock replenishment

| Type | Property Name | Java Type | DB Column/FK | Nullable | Notes |
|------|---------------|-----------|--------------|----------|-------|
| **PK** | id | UUID | id | NO | Identity |
| **RELATIONSHIP** | supplier | SupplierJpaEntity | supplier_id (FK) | NO | @ManyToOne, LAZY |
| **RELATIONSHIP** | items | List<ImportReceiptItemJpaEntity> | (mapped by "importReceipt") | - | @OneToMany, LAZY, orphanRemoval |
| **COLUMN** | totalAmount | BigDecimal | total_amount | NO | precision 19, scale 2 |
| **COLUMN** | note | String | note | YES | TEXT type |
| **COLUMN** | createdAt | Instant | created_at | NO | - |
| **COLUMN** | updatedAt | Instant | updated_at | NO | - |
| **COLUMN** | createdBy | UUID | created_by | NO | Simple UUID field |

**Analysis:**
- **Relationships (2):** supplier (→SupplierJpaEntity), items (←ImportReceiptItemJpaEntity)
- **Simple Fields (5):** totalAmount, note, createdAt, updatedAt, createdBy
- **Foreign Keys:** supplier_id
- **Note:** createdBy is a simple UUID field (not a JPA relationship)

---

## 7.4 ImportReceiptItemJpaEntity
**Table:** `import_receipt_items`  
**Key Feature:** Line items in import receipts

| Type | Property Name | Java Type | DB Column/FK | Nullable | Notes |
|------|---------------|-----------|--------------|----------|-------|
| **PK** | id | UUID | id | NO | Identity |
| **RELATIONSHIP** | importReceipt | ImportReceiptJpaEntity | import_receipt_id (FK) | NO | @ManyToOne, LAZY |
| **RELATIONSHIP** | book | BookJpaEntity | book_id (FK) | NO | @ManyToOne, LAZY |
| **COLUMN** | bookTitle | String | book_title | NO | length 255 |
| **COLUMN** | unitCost | BigDecimal | unit_cost | NO | precision 19, scale 2 |
| **COLUMN** | quantity | Integer | quantity | NO | - |
| **COLUMN** | lineTotal | BigDecimal | line_total | NO | precision 19, scale 2 |

**Analysis:**
- **Relationships (2):** importReceipt (→ImportReceiptJpaEntity), book (→BookJpaEntity)
- **Simple Fields (4):** bookTitle, unitCost, quantity, lineTotal
- **Foreign Keys:** import_receipt_id, book_id

---

# 8. NOTIFICATION ENTITY

## 8.1 NotificationJpaEntity
**Table:** `notifications`  
**Key Feature:** User notification records

| Type | Property Name | Java Type | DB Column/FK | Nullable | Notes |
|------|---------------|-----------|--------------|----------|-------|
| **PK** | id | UUID | id | NO | Identity |
| **RELATIONSHIP** | user | UserJpaEntity | user_id (FK) | NO | @ManyToOne, LAZY |
| **COLUMN** | title | String | title | NO | - |
| **COLUMN** | content | String | content | NO | TEXT type |
| **COLUMN** | read | boolean | read_flag | NO | - |
| **COLUMN** | createdAt | Instant | created_at | NO | - |
| **COLUMN** | updatedAt | Instant | updated_at | NO | - |
| **COLUMN** | readAt | Instant | read_at | YES | - |
| **COLUMN** | deletedAt | Instant | deleted_at | YES | Soft delete |

**Analysis:**
- **Relationships (1):** user (→UserJpaEntity)
- **Simple Fields (7):** title, content, read, createdAt, updatedAt, readAt, deletedAt
- **Foreign Keys:** user_id

---

# 9. RBAC (ROLE-BASED ACCESS CONTROL) ENTITIES

## 9.1 RoleJpaEntity
**Table:** `roles`  
**Key Feature:** Role definitions with permissions

| Type | Property Name | Java Type | DB Column/FK | Nullable | Notes |
|------|---------------|-----------|--------------|----------|-------|
| **PK** | id | UUID | id | NO | Identity |
| **COLUMN** | name | String | name | NO | length 100, unique |
| **COLUMN** | description | String | description | NO | TEXT type |
| **RELATIONSHIP** | permissions | Set<PermissionJpaEntity> | (join table) | - | @ManyToMany, LAZY, via role_permissions |
| **COLUMN** | createdAt | Instant | created_at | NO | - |
| **COLUMN** | updatedAt | Instant | updated_at | NO | - |
| **COLUMN** | deletedAt | Instant | deleted_at | YES | Soft delete |

**Analysis:**
- **Relationships (1 M2M):** permissions (→PermissionJpaEntity via role_permissions join table)
- **Simple Fields (5):** name, description, createdAt, updatedAt, deletedAt
- **ManyToMany Join Table:** `role_permissions` (role_id, permission_id)
- **Reverse Relationship:** Referenced by UserJpaEntity via user_roles join table

---

## 9.2 PermissionJpaEntity
**Table:** `permissions`  
**Key Feature:** Permission code definitions

| Type | Property Name | Java Type | DB Column/FK | Nullable | Notes |
|------|---------------|-----------|--------------|----------|-------|
| **PK** | id | UUID | id | NO | Identity |
| **COLUMN** | code | PermissionCode (Enum) | code | NO | EnumType.STRING, length 100, unique |
| **COLUMN** | description | String | description | YES | TEXT type |
| **COLUMN** | createdAt | Instant | created_at | NO | - |
| **COLUMN** | updatedAt | Instant | updated_at | NO | - |
| **COLUMN** | deletedAt | Instant | deleted_at | YES | Soft delete |

**Analysis:**
- **Relationships:** NONE (pure lookup entity, referenced by RoleJpaEntity)
- **Simple Fields (5):** code, description, createdAt, updatedAt, deletedAt
- **Note:** Reverse relationship via role_permissions join table

---

# SUMMARY MATRIX

## Relationship Statistics

| Entity | Has @OneToOne | Has @OneToMany | Has @ManyToOne | Has @ManyToMany | Total Relationships |
|--------|---|---|---|---|---|
| CartJpaEntity | 1 | 1 | 0 | 0 | 2 |
| CartItemJpaEntity | 0 | 0 | 2 | 0 | 2 |
| CouponUsageJpaEntity | 0 | 0 | 3 | 0 | 3 |
| CategoryJpaEntity | 0 | 0 | 0 | 0 | 0 |
| UserJpaEntity | 0 | 0 | 0 | 1 | 1 |
| ProfileJpaEntity | 1 | 0 | 0 | 0 | 1 |
| UserAddressJpaEntity | 0 | 0 | 1 | 0 | 1 |
| UserOtpJpaEntity | 0 | 0 | 1 | 0 | 1 |
| UserAuthIdentityJpaEntity | 0 | 0 | 1 | 0 | 1 |
| RefreshTokenJpaEntity | 0 | 0 | 1 | 0 | 1 |
| PasswordResetTokenJpaEntity | 0 | 0 | 1 | 0 | 1 |
| BookJpaEntity | 1 | 2 | 3 | 0 | 6 |
| BookDetailJpaEntity | 1 | 0 | 0 | 0 | 1 |
| BookImageJpaEntity | 0 | 0 | 1 | 0 | 1 |
| AuthorJpaEntity | 0 | 0 | 0 | 0 | 0 |
| PublisherJpaEntity | 0 | 0 | 0 | 0 | 0 |
| CouponJpaEntity | 0 | 1 | 0 | 0 | 1 |
| CouponTargetJpaEntity | 0 | 0 | 1 | 0 | 1 |
| OrderJpaEntity | 0 | 1 | 3 | 0 | 4 |
| OrderItemJpaEntity | 0 | 0 | 2 | 0 | 2 |
| PaymentJpaEntity | 0 | 0 | 0 | 0 | 0 |
| ReviewJpaEntity | 0 | 0 | 2 | 0 | 3 |
| StockMovementJpaEntity | 0 | 0 | 2 | 0 | 2 |
| SupplierJpaEntity | 0 | 0 | 0 | 0 | 0 |
| ImportReceiptJpaEntity | 0 | 1 | 1 | 0 | 2 |
| ImportReceiptItemJpaEntity | 0 | 0 | 2 | 0 | 2 |
| NotificationJpaEntity | 0 | 0 | 1 | 0 | 1 |
| RoleJpaEntity | 0 | 0 | 0 | 1 | 1 |
| PermissionJpaEntity | 0 | 0 | 0 | 0 | 0 |

---

## Foreign Key Summary

| Source Entity | Target Entity | FK Column(s) | Type | Cardinality |
|---|---|---|---|---|
| CartJpaEntity | UserJpaEntity | user_id | @OneToOne | 1:1 |
| CartItemJpaEntity | CartJpaEntity | cart_id | @ManyToOne | N:1 |
| CartItemJpaEntity | BookJpaEntity | book_id | @ManyToOne | N:1 |
| CouponUsageJpaEntity | CouponJpaEntity | coupon_id | @ManyToOne | N:1 |
| CouponUsageJpaEntity | UserJpaEntity | user_id | @ManyToOne | N:1 |
| CouponUsageJpaEntity | OrderJpaEntity | order_id | @ManyToOne | N:1 |
| UserJpaEntity | RoleJpaEntity | (join table) | @ManyToMany | M:N |
| ProfileJpaEntity | UserJpaEntity | user_id | @OneToOne | 1:1 |
| UserAddressJpaEntity | UserJpaEntity | user_id | @ManyToOne | N:1 |
| UserOtpJpaEntity | UserJpaEntity | user_id | @ManyToOne | N:1 |
| UserAuthIdentityJpaEntity | UserJpaEntity | user_id | @ManyToOne | N:1 |
| RefreshTokenJpaEntity | UserJpaEntity | user_id | @ManyToOne | N:1 |
| PasswordResetTokenJpaEntity | UserJpaEntity | user_id | @ManyToOne | N:1 |
| BookJpaEntity | CategoryJpaEntity | category_id | @ManyToOne | N:1 |
| BookJpaEntity | AuthorJpaEntity | author_id | @ManyToOne | N:1 |
| BookJpaEntity | PublisherJpaEntity | publisher_id | @ManyToOne | N:1 |
| BookDetailJpaEntity | BookJpaEntity | book_id | @OneToOne | 1:1 |
| BookImageJpaEntity | BookJpaEntity | book_id | @ManyToOne | N:1 |
| CouponTargetJpaEntity | CouponJpaEntity | coupon_id | @ManyToOne | N:1 |
| OrderJpaEntity | UserJpaEntity | user_id | @ManyToOne | N:1 |
| OrderJpaEntity | CouponJpaEntity | book_coupon_id | @ManyToOne | N:1 |
| OrderJpaEntity | CouponJpaEntity | shipping_coupon_id | @ManyToOne | N:1 |
| OrderItemJpaEntity | OrderJpaEntity | order_id | @ManyToOne | N:1 |
| OrderItemJpaEntity | BookJpaEntity | book_id | @ManyToOne | N:1 |
| ReviewJpaEntity | UserJpaEntity | user_id | @ManyToOne | N:1 |
| ReviewJpaEntity | BookJpaEntity | book_id | @ManyToOne | N:1 |
| ReviewJpaEntity | OrderItemJpaEntity | order_item_id | @OneToOne | 1:1 |
| StockMovementJpaEntity | BookJpaEntity | book_id | @ManyToOne | N:1 |
| StockMovementJpaEntity | UserJpaEntity | created_by | @ManyToOne | N:1 |
| ImportReceiptJpaEntity | SupplierJpaEntity | supplier_id | @ManyToOne | N:1 |
| ImportReceiptItemJpaEntity | ImportReceiptJpaEntity | import_receipt_id | @ManyToOne | N:1 |
| ImportReceiptItemJpaEntity | BookJpaEntity | book_id | @ManyToOne | N:1 |
| NotificationJpaEntity | UserJpaEntity | user_id | @ManyToOne | N:1 |
| RoleJpaEntity | PermissionJpaEntity | (join table) | @ManyToMany | M:N |

---

## Join Table Summary (Many-to-Many)

| Join Table | Left Entity | Left FK | Right Entity | Right FK |
|---|---|---|---|---|
| user_roles | UserJpaEntity | user_id | RoleJpaEntity | role_id |
| role_permissions | RoleJpaEntity | role_id | PermissionJpaEntity | permission_id |

---

## Key Insights

### Relationships by Type
- **One-to-One (5):** Cart↔User, Profile↔User, BookDetail↔Book, Review→OrderItem
- **One-to-Many (7):** Book→Images, Book→Details (mapped), Cart→Items, Order→Items, Coupon→Targets, ImportReceipt→Items
- **Many-to-One (25):** Most common relationship type for foreign keys
- **Many-to-Many (2):** User↔Roles, Role↔Permissions

### Entities with NO Relationships (Pure Lookup)
- **CategoryJpaEntity**
- **AuthorJpaEntity**
- **PublisherJpaEntity**
- **PermissionJpaEntity**
- **PaymentJpaEntity** (uses orderId as simple UUID field, not JPA FK)

### Highly Connected Entities (Hub Entities)
- **UserJpaEntity:** Referenced by 13+ entities (Cart, Orders, Notifications, Reviews, etc.)
- **BookJpaEntity:** Referenced by 9+ entities (CartItems, OrderItems, BookImages, StockMovements, etc.)
- **CouponJpaEntity:** Referenced by Orders (2 refs) and CouponUsages

### Cascade & Orphan Removal Patterns
| Entity | Cascade | Orphan Removal | Purpose |
|---|---|---|---|
| CartJpaEntity.items | CascadeType.ALL | true | Auto-delete cart items when cart deleted |
| BookJpaEntity.images | CascadeType.ALL | true | Auto-delete book images |
| BookJpaEntity.detail | CascadeType.ALL | true | Auto-delete book detail when book deleted |
| OrderJpaEntity.items | CascadeType.ALL | true | Auto-delete order items when order deleted |
| CouponJpaEntity.targets | CascadeType.ALL | true | Auto-delete coupon targets |
| ImportReceiptJpaEntity.items | CascadeType.ALL | true | Auto-delete import items when receipt deleted |

### Polymorphic References (Generic UUID Fields)
- **CouponTargetJpaEntity.targetId** - Points to Book or Category (no FK constraint)
- **StockMovementJpaEntity.referenceId** - Points to various entity types based on referenceType

### Soft Delete Pattern
Entities with soft delete support (deletedAt field):
- CategoryJpaEntity, BookJpaEntity, AuthorJpaEntity, PublisherJpaEntity
- UserJpaEntity, ProfileJpaEntity, UserAddressJpaEntity, UserOtpJpaEntity
- CouponJpaEntity, OrderJpaEntity, ReviewJpaEntity, StockMovementJpaEntity
- SupplierJpaEntity, ImportReceiptJpaEntity, NotificationJpaEntity
- RoleJpaEntity, PermissionJpaEntity

---

## Enum Types Used

| Enum | Used In | Purpose |
|---|---|---|
| UserStatus | UserJpaEntity | Account status (ACTIVE, INACTIVE, etc.) |
| Gender | ProfileJpaEntity | User demographics |
| OtpPurpose | UserOtpJpaEntity | OTP type (REGISTRATION, PASSWORD_RESET, etc.) |
| AuthProvider | UserAuthIdentityJpaEntity | OAuth provider (GOOGLE, GITHUB, etc.) |
| PaymentProvider | PaymentJpaEntity | Payment gateway type |
| PaymentStatus | PaymentJpaEntity, OrderJpaEntity | Payment state |
| OrderStatus | OrderJpaEntity | Order fulfillment state |
| PaymentMethod | OrderJpaEntity | Payment type (CARD, BANK_TRANSFER, etc.) |
| CouponType | CouponJpaEntity | Coupon scope (BOOK, SHIPPING, etc.) |
| CouponDiscountType | CouponJpaEntity | Discount calculation (PERCENTAGE, FIXED, etc.) |
| CouponTargetType | CouponTargetJpaEntity | Target entity type (BOOK, CATEGORY, etc.) |
| StockMovementType | StockMovementJpaEntity | Stock change reason (IMPORT, SALE, etc.) |
| PermissionCode | PermissionJpaEntity | Authorization codes (MANAGE_BOOKS, etc.) |

