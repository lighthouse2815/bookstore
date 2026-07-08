package com.bookstore.bookstore.infrastructure.persistence;

import com.bookstore.bookstore.application.port.out.IPasswordEncoder;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

@Slf4j
@Component
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
@Order(100)
@RequiredArgsConstructor
public class DevelopmentDataSeeder implements ApplicationRunner {

    private static final long RECENT_ACTIVITY_WINDOW_MINUTES = 760L;
    private static final int DASHBOARD_LOW_STOCK_TARGET = 8;

    @Value("${app.seed.size:50}")
    private int seedSize;

    @Value("${app.seed.category-count:12}")
    private int categoryCount;

    @Value("${app.seed.exit-after-run:false}")
    private boolean exitAfterRun;

    private final JdbcTemplate jdbcTemplate;
    private final IPasswordEncoder passwordEncoder;
    private final TransactionTemplate transactionTemplate;
    private final ConfigurableApplicationContext applicationContext;
    private final Environment environment;
    private final Instant seedBaseTime = Instant.now()
            .truncatedTo(ChronoUnit.MINUTES)
            .minus(RECENT_ACTIVITY_WINDOW_MINUTES, ChronoUnit.MINUTES);

    @Override
    public void run(ApplicationArguments args) {
        transactionTemplate.executeWithoutResult(status -> seedDatabase());
        if (exitAfterRun) {
            SpringApplication.exit(applicationContext, () -> 0);
        }
    }

    private void seedDatabase() {
        validateSeedConfiguration();
        if (!isFreshDatabase()) {
            if (exitAfterRun) {
                throw new IllegalStateException(
                        "Seed profile requires a freshly created schema containing only the initialized admin account");
            }
            log.info("Skipping seed because database already contains application data");
            return;
        }

        String adminId = requiredId("SELECT BIN_TO_UUID(id) FROM users WHERE username = ?", adminUsername());
        String userRoleId = requiredId("SELECT BIN_TO_UUID(id) FROM roles WHERE name = ?", "USER");
        String staffRoleId = requiredId("SELECT BIN_TO_UUID(id) FROM roles WHERE name = ?", "STAFF");
        String shipperRoleId = requiredId("SELECT BIN_TO_UUID(id) FROM roles WHERE name = ?", "SHIPPER");

        SeedContext context = seedUsers(adminId, userRoleId, staffRoleId, shipperRoleId);
        seedUserOwnedData(context);
        seedCatalog(context);
        seedCommerce(context);
        seedDigitalLibrary(context);
        seedCustomerSupport(context);
        validateSeededData();

        log.info("Seeded a coherent development dataset for database bookstore_db");
    }

    private SeedContext seedUsers(
            String adminId,
            String userRoleId,
            String staffRoleId,
            String shipperRoleId
    ) {
        List<String> userIds = new ArrayList<>();
        List<String> customerIds = new ArrayList<>();
        List<String> staffIds = new ArrayList<>();
        List<String> shipperIds = new ArrayList<>();
        userIds.add(adminId);

        String passwordHash = passwordEncoder.encode(seedDefaultPassword());
        for (int i = 2; i <= seedSize; i++) {
            DevelopmentSeedCatalog.PersonSeed person = DevelopmentSeedCatalog.personAt(i - 2);
            String userId = id("user", i);
            String roleId;
            if (i % 15 == 0) {
                roleId = staffRoleId;
                staffIds.add(userId);
            } else if (i % 10 == 0) {
                roleId = shipperRoleId;
                shipperIds.add(userId);
            } else {
                roleId = userRoleId;
                customerIds.add(userId);
            }

            insert("""
                    INSERT INTO users (
                        id, created_at, email, password_hash, status,
                        username, deleted_at, locked, phone_number, updated_at
                    ) VALUES (UUID_TO_BIN(?), ?, ?, ?, 'ACTIVE', ?, NULL, ?, ?, ?)
                    """,
                    userId, time(i), person.email(), passwordHash, person.username(), false,
                    "09%d%07d".formatted(i % 8 + 1, 1_000_000 + i * 7_913), time(i + 1));

            insert("INSERT INTO user_roles (user_id, role_id) VALUES (UUID_TO_BIN(?), UUID_TO_BIN(?))",
                    userId, roleId);
            userIds.add(userId);
        }

        if (staffIds.isEmpty() || shipperIds.isEmpty()) {
            throw new IllegalStateException("Seed data must contain staff and shipper users");
        }

        return new SeedContext(userIds, customerIds, staffIds, shipperIds);
    }

    private void seedUserOwnedData(SeedContext context) {
        for (int i = 1; i <= seedSize; i++) {
            String userId = context.userIds().get(i - 1);
            DevelopmentSeedCatalog.PersonSeed person = DevelopmentSeedCatalog.personAt(Math.max(0, i - 2));

            if (i > 1) {
                insert("""
                        INSERT INTO profiles (
                            id, avatar_url, created_at, date_of_birth, deleted_at, first_name,
                            gender, last_name, updated_at, user_id, avatar_file_asset_id
                        ) VALUES (UUID_TO_BIN(?), ?, ?, ?, NULL, ?, ?, ?, ?, UUID_TO_BIN(?), NULL)
                        """,
                        id("profile", i), DevelopmentSeedCatalog.profileAvatarUrlAt(i - 2), time(i),
                        Date.valueOf(LocalDate.of(1985 + i % 18, i % 12 + 1, i % 27 + 1)),
                        person.firstName(), i % 3 == 0 ? "OTHER" : i % 2 == 0 ? "FEMALE" : "MALE",
                        person.lastName(), time(i + 1), userId);
            }

            insert("""
                    INSERT INTO user_addresses (
                        id, created_at, default_address, deleted_at, receiver_address,
                        receiver_name, receiver_phone, updated_at, user_id
                    ) VALUES (UUID_TO_BIN(?), ?, ?, NULL, ?, ?, ?, ?, UUID_TO_BIN(?))
                    """,
                    id("address", i), time(i), true, DevelopmentSeedCatalog.addressAt(i),
                    i == 1 ? adminFullName() : person.fullName(),
                    "09%d%07d".formatted(i % 8 + 1, 2_000_000 + i * 6_127), time(i + 1), userId);

            insert("INSERT INTO carts (id, created_at, updated_at, user_id) VALUES (UUID_TO_BIN(?), ?, ?, UUID_TO_BIN(?))",
                    id("cart", i), time(i), time(i + 1), userId);

            insert("""
                    INSERT INTO user_auth_identities (
                        id, created_at, email_verified, provider, provider_email,
                        provider_subject, updated_at, user_id
                    ) VALUES (UUID_TO_BIN(?), ?, ?, 'LOCAL', ?, ?, ?, UUID_TO_BIN(?))
                    """,
                    id("identity", i), time(i), true,
                    i == 1 ? adminEmail() : person.email(),
                    "local-seed-%02d".formatted(i), time(i + 1), userId);

            insert("""
                    INSERT INTO refresh_tokens (id, created_at, expires_at, revoked, token, user_id)
                    VALUES (UUID_TO_BIN(?), ?, ?, ?, ?, UUID_TO_BIN(?))
                    """,
                    id("refresh-token", i), time(i), time(i + 10000), i % 8 == 0,
                    "seed-refresh-token-%02d-%s".formatted(i, id("refresh-value", i)), userId);

            insert("""
                    INSERT INTO user_otps (
                        id, created_at, expires_at, invalidated_at, otp_hash,
                        updated_at, user_id, verified_at, purpose
                    ) VALUES (UUID_TO_BIN(?), ?, ?, NULL, ?, ?, UUID_TO_BIN(?), ?, ?)
                    """,
                    id("otp", i), time(i), time(i + 60), hashValue("otp", i), time(i + 1), userId,
                    i % 3 == 0 ? null : time(i + 2), i % 2 == 0 ? "PASSWORD_RESET" : "REGISTRATION");

            insert("""
                    INSERT INTO password_reset_tokens (
                        id, created_at, expires_at, token_hash, used_at, user_id
                    ) VALUES (UUID_TO_BIN(?), ?, ?, ?, ?, UUID_TO_BIN(?))
                    """,
                    id("password-reset", i), time(i), time(i + 120), hashValue("reset", i),
                    i % 4 == 0 ? time(i + 2) : null, userId);
        }
    }

    private void seedCatalog(SeedContext context) {
        for (int i = 1; i <= categoryCount; i++) {
            DevelopmentSeedCatalog.CategorySeed category = DevelopmentSeedCatalog.CATEGORIES.get(i - 1);
            insert("""
                    INSERT INTO categories (
                        id, created_at, deleted_at, description, name, updated_at
                    ) VALUES (UUID_TO_BIN(?), ?, NULL, ?, ?, ?)
                    """,
                    id("category", i), time(i), category.description(), category.name(), time(i + 1));
        }

        for (int i = 1; i <= DevelopmentSeedCatalog.PUBLISHERS.size(); i++) {
            DevelopmentSeedCatalog.PublisherSeed publisher = DevelopmentSeedCatalog.PUBLISHERS.get(i - 1);
            insert("""
                    INSERT INTO publishers (id, created_at, deleted_at, description, name, updated_at)
                    VALUES (UUID_TO_BIN(?), ?, NULL, ?, ?, ?)
                    """,
                    id("publisher", i), time(i), publisher.description(), publisher.name(), time(i + 1));
        }

        for (int i = 1; i <= DevelopmentSeedCatalog.SUPPLIERS.size(); i++) {
            DevelopmentSeedCatalog.SupplierSeed supplier = DevelopmentSeedCatalog.SUPPLIERS.get(i - 1);
            insert("""
                    INSERT INTO suppliers (
                        id, address, created_at, deleted_at, email, name, note, phone, updated_at
                    ) VALUES (UUID_TO_BIN(?), ?, ?, NULL, ?, ?, ?, ?, ?)
                    """,
                    id("supplier", i), supplier.address(), time(i), supplier.email(), supplier.name(),
                    DevelopmentSeedCatalog.supplierNoteAt(i - 1), supplier.phone(), time(i + 1));
        }

        Map<String, String> authorIds = new LinkedHashMap<>();
        for (int i = 1; i <= DevelopmentSeedCatalog.AUTHORS.size(); i++) {
            DevelopmentSeedCatalog.AuthorSeed author = DevelopmentSeedCatalog.AUTHORS.get(i - 1);
            String authorId = id("author", i);
            authorIds.put(author.name(), authorId);
            insert("""
                    INSERT INTO authors (
                        id, biography, created_at, deleted_at, name, updated_at,
                        avatar_url, birth_year, death_year, avatar_file_asset_id
                    ) VALUES (UUID_TO_BIN(?), ?, ?, NULL, ?, ?, ?, ?, ?, NULL)
                    """,
                    authorId, author.biography(), time(i), author.name(), time(i + 1),
                    author.avatarUrl(), author.birthYear(), author.deathYear());
        }

        for (int i = 1; i <= seedSize; i++) {
            DevelopmentSeedCatalog.BookSeed book = DevelopmentSeedCatalog.BOOKS.get(i - 1);
            String fileAssetId = id("file-asset", i);
            String coverFileAssetId = id("cover-file-asset", i);
            String authorId = authorIds.get(book.author());
            if (authorId == null) {
                throw new IllegalStateException("Missing author reference for seeded book: " + book.author());
            }

            int resolvedCategoryIndex = DevelopmentSeedCatalog.resolveCategoryIndex(book.categoryIndex(), categoryCount);
            DevelopmentSeedCatalog.CategorySeed category = DevelopmentSeedCatalog.CATEGORIES.get(resolvedCategoryIndex);
            String categoryId = id("category", resolvedCategoryIndex + 1);
            String publisherId = id("publisher", book.publisherIndex() + 1);
            String bookId = id("book", i);
            String digitalAssetId = id("digital-asset", i);
            String storageKey = "digital/books/%02d/ebook-%02d.pdf".formatted(i, i);

            insert("""
                    INSERT INTO file_assets (
                        id, bucket, checksum_sha256, content_type, created_at, created_by,
                        deleted_at, original_name, provider, public_url, purpose, size_bytes,
                        status, storage_key, updated_at, visibility
                    ) VALUES (UUID_TO_BIN(?), 'bookstore-digital', ?, 'application/pdf', ?, UUID_TO_BIN(?),
                        NULL, ?, 'R2', NULL, 'EBOOK_FILE', ?, 'ACTIVE', ?, ?, 'PRIVATE')
                    """,
                    fileAssetId, hashValue("file", i), time(i), context.userIds().get(0),
                    "ebook-%02d.pdf".formatted(i), 1_000_000L + i * 12_345L, storageKey, time(i + 1));

            insert("""
                    INSERT INTO file_assets (
                        id, bucket, checksum_sha256, content_type, created_at, created_by,
                        deleted_at, original_name, provider, public_url, purpose, size_bytes,
                        status, storage_key, updated_at, visibility
                    ) VALUES (UUID_TO_BIN(?), 'openlibrary-covers', ?, 'image/jpeg', ?, UUID_TO_BIN(?),
                        NULL, ?, 'R2', ?, 'BOOK_IMAGE', ?, 'ACTIVE', ?, ?, 'PUBLIC')
                    """,
                    coverFileAssetId, hashValue("cover", i), time(i), context.userIds().get(0),
                    "cover-%02d.jpg".formatted(i), book.coverUrl(), 180_000L + i * 1_337L,
                    "openlibrary/covers/%d-L.jpg".formatted(book.coverId()), time(i + 1));

            insert("""
                    INSERT INTO books (
                        id, author_id, category_id, created_at, deleted_at, description,
                        image_url, price, publisher_id, stock_quantity, title, updated_at, isbn
                    ) VALUES (UUID_TO_BIN(?), UUID_TO_BIN(?), UUID_TO_BIN(?), ?, NULL, ?, ?, ?,
                        UUID_TO_BIN(?), ?, ?, ?, ?)
                    """,
                    bookId, authorId, categoryId, time(i),
                    DevelopmentSeedCatalog.bookDescriptionAt(i - 1, book, category),
                    book.coverUrl(), money(book.price()), publisherId, seedStockQuantity(i),
                    book.title(), time(i + 1), book.isbn());

            insert("""
                    INSERT INTO book_details (
                        id, cover_type, dimensions, edition, language, page_count,
                        publication_year, translator, weight, book_id
                    ) VALUES (UUID_TO_BIN(?), ?, '14 x 20.5 cm', ?, 'Tiếng Việt', ?, ?, ?, ?, UUID_TO_BIN(?))
                    """,
                    id("book-detail", i), i % 5 == 0 ? "Bìa cứng" : "Bìa mềm",
                    i % 4 == 0 ? "Ấn bản có minh họa" : "Ấn bản tiêu chuẩn",
                    book.pageCount(), book.publicationYear(), null, 220 + book.pageCount() / 2, bookId);

            insert("""
                    INSERT INTO book_images (
                        id, alt_text, created_at, image_url, primary_image,
                        sort_order, book_id, file_asset_id
                    ) VALUES (UUID_TO_BIN(?), ?, ?, ?, ?, 0, UUID_TO_BIN(?), UUID_TO_BIN(?))
                    """,
                    id("book-image", i), "Bìa sách %s".formatted(book.title()), time(i),
                    book.coverUrl(), true, bookId, coverFileAssetId);

            insert("""
                    INSERT INTO digital_assets (
                        id, checksum, created_at, deleted_at, download_allowed, file_name,
                        file_size, format, mime_type, price, published, sample_storage_key,
                        storage_key, title, updated_at, book_id, file_asset_id,
                        sample_file_asset_id, purchase_allowed
                    ) VALUES (UUID_TO_BIN(?), ?, ?, NULL, ?, ?, ?, 'PDF', 'application/pdf', ?, ?, NULL,
                        ?, ?, ?, UUID_TO_BIN(?), UUID_TO_BIN(?), NULL, ?)
                    """,
                    digitalAssetId, hashValue("digital", i), time(i), i % 4 != 0,
                    "ebook-%02d.pdf".formatted(i), 1_000_000L + i * 12_345L,
                    money(Math.max(39_000L, book.price() * 55 / 100)), true, storageKey,
                    "Bản điện tử - %s".formatted(book.title()), time(i + 1), bookId, fileAssetId, true);
        }
    }
    private void seedCommerce(SeedContext context) {
        for (int i = 1; i <= seedSize; i++) {
            DevelopmentSeedCatalog.BookSeed book = DevelopmentSeedCatalog.BOOKS.get(i - 1);
            DevelopmentSeedCatalog.PersonSeed receiver = DevelopmentSeedCatalog.personAt(i - 1);
            String userId = context.customerIds().get((i - 1) % context.customerIds().size());
            String bookId = id("book", i);
            String couponId = id("coupon", i);
            String orderId = id("order", i);
            String orderItemId = id("order-item", i);
            String receiptId = id("receipt", i);
            String couponCode = i % 2 == 0 ? "FREESHIP%02d".formatted(i) : "DOCHEM%02d".formatted(i);
            boolean bookCoupon = i % 2 != 0;
            BigDecimal productTotal = money(book.price());
            BigDecimal shippingFee = money(30_000L);
            BigDecimal discount = money(10_000L);
            BigDecimal finalAmount = productTotal.add(shippingFee).subtract(discount);

            insert("""
                    INSERT INTO coupons (
                        id, active, code, created_at, deleted_at, description, discount_type,
                        discount_value, expires_at, max_discount_amount, max_usage_count,
                        min_order_amount, starts_at, updated_at, used_count, coupon_type
                    ) VALUES (UUID_TO_BIN(?), ?, ?, ?, NULL, ?, ?, ?, ?, ?, 100, ?, ?, ?, 1, ?)
                    """,
                    couponId, true, couponCode, time(i),
                    bookCoupon ? "Giảm 10% cho đầu sách được chọn trong chương trình đọc hè."
                            : "Hỗ trợ phí vận chuyển cho đơn hàng đủ điều kiện.",
                    bookCoupon ? "PERCENTAGE" : "FIXED_AMOUNT", bookCoupon ? money(10) : money(10_000),
                    time(i + 20_000), bookCoupon ? money(25_000) : null, money(50_000),
                    time(i - 100), time(i + 1), bookCoupon ? "BOOK" : "SHIPPING");

            String targetType = i % 3 == 0 ? "ALL_ORDER" : i % 3 == 1 ? "BOOK" : "CATEGORY";
            String targetId = "ALL_ORDER".equals(targetType) ? null
                    : "BOOK".equals(targetType) ? bookId : id("category", (i - 1) % categoryCount + 1);
            insert("""
                    INSERT INTO coupon_targets (id, created_at, target_id, target_type, updated_at, coupon_id)
                    VALUES (UUID_TO_BIN(?), ?, UUID_TO_BIN(?), ?, ?, UUID_TO_BIN(?))
                    """,
                    id("coupon-target", i), time(i), targetId, targetType, time(i + 1), couponId);

            insert("""
                    INSERT INTO orders (
                        id, cancelled_at, book_coupon_code, coupon_discount, created_at,
                        discount_amount, final_amount, order_code, payment_method, payment_status,
                        product_total, receiver_address, receiver_name, receiver_phone,
                        shipping_coupon_code, shipping_discount, shipping_fee, status,
                        total_amount, updated_at, book_coupon_id, shipping_coupon_id, user_id
                    ) VALUES (
                        UUID_TO_BIN(?), NULL, ?, ?, ?, ?, ?, ?, 'COD', 'PAID', ?, ?, ?, ?, ?, ?, ?,
                        'DELIVERED', ?, ?, UUID_TO_BIN(?), UUID_TO_BIN(?), UUID_TO_BIN(?))
                    """,
                    orderId, bookCoupon ? couponCode : null,
                    bookCoupon ? discount : BigDecimal.ZERO, time(i), discount, finalAmount,
                    "DH2026%05d".formatted(i), productTotal,
                    DevelopmentSeedCatalog.addressAt(i), receiver.fullName(),
                    "09%d%07d".formatted(i % 8 + 1, 3_000_000 + i * 5_219),
                    bookCoupon ? null : couponCode, bookCoupon ? BigDecimal.ZERO : discount,
                    shippingFee, finalAmount, time(i + 500),
                    bookCoupon ? couponId : null, bookCoupon ? null : couponId, userId);

            insert("""
                    INSERT INTO order_items (
                        id, book_id, book_title, line_total, quantity, unit_price,
                        order_id, item_order, item_type, digital_asset_id
                    ) VALUES (UUID_TO_BIN(?), UUID_TO_BIN(?), ?, ?, 1, ?, UUID_TO_BIN(?), 0,
                        'PHYSICAL_BOOK', NULL)
                    """,
                    orderItemId, bookId, book.title(), productTotal, productTotal, orderId);

            insert("""
                    INSERT INTO payments (
                        id, amount, created_at, gateway, merchant_id, order_id, paid_at,
                        provider, reference_code, status, transaction_id, transfer_content, updated_at
                    ) VALUES (UUID_TO_BIN(?), ?, ?, 'Thanh toán khi nhận hàng', 'BOOKSTORE', UUID_TO_BIN(?), ?,
                        'COD', ?, 'PAID', ?, ?, ?)
                    """,
                    id("payment", i), finalAmount, time(i), orderId, time(i + 400),
                    "PAY%06d".formatted(i), "COD-TXN-%06d".formatted(i),
                    "Thanh toán đơn DH2026%05d".formatted(i), time(i + 400));

            String shipperId = context.shipperIds().get((i - 1) % context.shipperIds().size());
            insert("""
                    INSERT INTO shipments (
                        id, assigned_at, delivered_at, delivering_at, failed_at, failure_reason,
                        picked_up_at, status, updated_at, order_id, shipper_id
                    ) VALUES (UUID_TO_BIN(?), ?, ?, ?, NULL, NULL, ?, 'DELIVERED', ?,
                        UUID_TO_BIN(?), UUID_TO_BIN(?))
                    """,
                    id("shipment", i), time(i + 100), time(i + 350), time(i + 250),
                    time(i + 150), time(i + 350), orderId, shipperId);

            insert("""
                    INSERT INTO reviews (
                        id, book_id, comment, created_at, deleted_at, order_item_id,
                        rating, updated_at, user_id
                    ) VALUES (UUID_TO_BIN(?), UUID_TO_BIN(?), ?, ?, NULL, UUID_TO_BIN(?), ?, ?, UUID_TO_BIN(?))
                    """,
                    id("review", i), bookId, DevelopmentSeedCatalog.reviewAt(i - 1),
                    time(i + 600), orderItemId, i % 5 + 1, time(i + 601), userId);

            insert("""
                    INSERT INTO coupon_usages (
                        id, coupon_id, order_id, used_at, user_id, discount_amount
                    ) VALUES (UUID_TO_BIN(?), UUID_TO_BIN(?), UUID_TO_BIN(?), ?, UUID_TO_BIN(?), ?)
                    """,
                    id("coupon-usage", i), couponId, orderId, time(i), userId, discount);

            insert("""
                    INSERT INTO import_receipts (
                        id, created_at, created_by, note, supplier_id, total_amount, updated_at
                    ) VALUES (UUID_TO_BIN(?), ?, UUID_TO_BIN(?), ?, UUID_TO_BIN(?), ?, ?)
                    """,
                    receiptId, time(i), context.userIds().get(0),
                    "Nhập bổ sung %s theo kế hoạch bán hàng tháng %d.".formatted(book.title(), i % 12 + 1),
                    id("supplier", (i - 1) % DevelopmentSeedCatalog.SUPPLIERS.size() + 1),
                    money(book.price() * 7), time(i + 1));

            insert("""
                    INSERT INTO import_receipt_items (
                        id, book_id, book_title, line_total, quantity, unit_cost,
                        import_receipt_id, item_order
                    ) VALUES (UUID_TO_BIN(?), UUID_TO_BIN(?), ?, ?, 10, ?, UUID_TO_BIN(?), 0)
                    """,
                    id("receipt-item", i), bookId, book.title(), money(book.price() * 7),
                    money(book.price() * 7 / 10), receiptId);

            insert("""
                    INSERT INTO stock_movements (
                        id, after_quantity, before_quantity, book_id, created_at, created_by,
                        note, quantity, reference_id, reference_type, type
                    ) VALUES (UUID_TO_BIN(?), 110, 100, UUID_TO_BIN(?), ?, UUID_TO_BIN(?), ?, 10,
                        UUID_TO_BIN(?), 'IMPORT_RECEIPT', 'IMPORT')
                    """,
                    id("stock-movement", i), bookId, time(i), context.userIds().get(0),
                    "Nhập kho %s từ phiếu nhập đã duyệt.".formatted(book.title()), receiptId);

            insert("""
                    INSERT INTO cart_items (
                        id, book_id, created_at, quantity, updated_at, cart_id,
                        item_type, digital_asset_id
                    ) VALUES (UUID_TO_BIN(?), UUID_TO_BIN(?), ?, ?, ?, UUID_TO_BIN(?),
                        'PHYSICAL_BOOK', NULL)
                    """,
                    id("cart-item", i), bookId, time(i), i % 3 + 1, time(i + 1), id("cart", i));
        }
    }

    private void seedDigitalLibrary(SeedContext context) {
        for (int i = 1; i <= seedSize; i++) {
            String userId = context.customerIds().get((i - 1) % context.customerIds().size());
            String digitalAssetId = id("digital-asset", i);

            insert("""
                    INSERT INTO user_digital_accesses (
                        id, access_type, created_at, deleted_at, expires_at, source_order_id,
                        status, updated_at, digital_asset_id, user_id
                    ) VALUES (UUID_TO_BIN(?), 'SUBSCRIPTION', ?, NULL, ?, NULL, 'ACTIVE', ?,
                        UUID_TO_BIN(?), UUID_TO_BIN(?))
                    """,
                    id("digital-access", i), time(i), time(i + 30_000), time(i + 1), digitalAssetId, userId);

            insert("""
                    INSERT INTO reading_progresses (
                        id, created_at, current_page, last_read_at, position_data,
                        progress_percent, updated_at, digital_asset_id, user_id
                    ) VALUES (UUID_TO_BIN(?), ?, ?, ?, ?, ?, ?, UUID_TO_BIN(?), UUID_TO_BIN(?))
                    """,
                    id("reading-progress", i), time(i), 5 + i, time(i + 500),
                    "{\"page\":%d,\"chapter\":%d}".formatted(5 + i, i % 12 + 1),
                    BigDecimal.valueOf((i * 1.75) % 100), time(i + 500), digitalAssetId, userId);

            insert("""
                    INSERT INTO notifications (
                        id, content, created_at, deleted_at, read_flag, read_at, title,
                        updated_at, user_id, link, target_id, target_type, notification_type
                    ) VALUES (UUID_TO_BIN(?), ?, ?, NULL, ?, ?, ?, ?, UUID_TO_BIN(?), ?,
                        UUID_TO_BIN(?), 'ORDER', 'ORDER_STATUS')
                    """,
                    id("notification", i), "Đơn hàng DH2026%05d đã được giao thành công.".formatted(i),
                    time(i + 700), i % 3 != 0, i % 3 != 0 ? time(i + 720) : null,
                    "Cập nhật đơn hàng", time(i + 720), userId,
                    "/orders/%s".formatted(id("order", i)), id("order", i));
        }
    }

    private void seedCustomerSupport(SeedContext context) {
        for (int i = 1; i <= seedSize; i++) {
            String conversationId = id("conversation", i);
            String messageId = id("message", i);
            String customerId = context.customerIds().get((i - 1) % context.customerIds().size());
            String staffId = context.staffIds().get((i - 1) % context.staffIds().size());
            String message = "Tôi cần hỗ trợ về đơn hàng DH2026%05d.".formatted(i);

            insert("""
                    INSERT INTO chat_conversations (
                        id, closed_at, created_at, deleted_at, last_message_at, last_message_id,
                        last_message_preview, priority, status, subject, target_id, target_type,
                        updated_at, assigned_staff_id, customer_id
                    ) VALUES (UUID_TO_BIN(?), NULL, ?, NULL, ?, NULL, NULL, ?, 'OPEN', ?, UUID_TO_BIN(?),
                        'ORDER', ?, UUID_TO_BIN(?), UUID_TO_BIN(?))
                    """,
                    conversationId, time(i), time(i + 10), i % 10 == 0 ? "HIGH" : "NORMAL",
                    "Hỗ trợ đơn hàng DH2026%05d".formatted(i), id("order", i), time(i + 10), staffId, customerId);

            insert("""
                    INSERT INTO chat_messages (
                        id, attachment_name, attachment_size, attachment_url, content, created_at,
                        deleted_at, message_type, sender_role, updated_at, conversation_id, sender_id
                    ) VALUES (UUID_TO_BIN(?), NULL, NULL, NULL, ?, ?, NULL, 'TEXT', 'USER', ?,
                        UUID_TO_BIN(?), UUID_TO_BIN(?))
                    """,
                    messageId, message, time(i + 10), time(i + 10), conversationId, customerId);

            insert("""
                    INSERT INTO chat_conversation_participants (
                        id, joined_at, last_read_at, last_read_message_id, left_at, role,
                        unread_count, conversation_id, user_id
                    ) VALUES (UUID_TO_BIN(?), ?, ?, UUID_TO_BIN(?), NULL, 'USER', 0,
                        UUID_TO_BIN(?), UUID_TO_BIN(?))
                    """,
                    id("participant", i * 2 - 1), time(i), time(i + 10), messageId, conversationId, customerId);

            insert("""
                    INSERT INTO chat_conversation_participants (
                        id, joined_at, last_read_at, last_read_message_id, left_at, role,
                        unread_count, conversation_id, user_id
                    ) VALUES (UUID_TO_BIN(?), ?, NULL, NULL, NULL, 'STAFF', 1,
                        UUID_TO_BIN(?), UUID_TO_BIN(?))
                    """,
                    id("participant", i * 2), time(i), conversationId, staffId);

            insert("""
                    UPDATE chat_conversations
                    SET last_message_id = UUID_TO_BIN(?), last_message_preview = ?, last_message_at = ?
                    WHERE id = UUID_TO_BIN(?)
                    """,
                    messageId, message, time(i + 10), conversationId);
        }
    }

    private void validateSeededData() {
        Map<String, Integer> minimumCounts = new LinkedHashMap<>();
        minimumCounts.put("permissions", 33);
        minimumCounts.put("roles", 4);
        minimumCounts.put("role_permissions", 50);
        minimumCounts.put("chat_conversation_participants", seedSize * 2);
        minimumCounts.put("categories", categoryCount);
        minimumCounts.put("authors", DevelopmentSeedCatalog.AUTHORS.size());
        minimumCounts.put("publishers", DevelopmentSeedCatalog.PUBLISHERS.size());
        minimumCounts.put("suppliers", DevelopmentSeedCatalog.SUPPLIERS.size());
        minimumCounts.put("file_assets", seedSize * 2);

        List.of(
                "users", "profiles", "user_roles", "user_addresses", "user_auth_identities",
                "refresh_tokens", "user_otps", "password_reset_tokens", "carts", "cart_items",
                "books", "book_details", "book_images", "digital_assets", "coupons", "coupon_targets",
                "orders", "order_items", "payments", "shipments", "reviews", "coupon_usages",
                "import_receipts", "import_receipt_items", "stock_movements", "user_digital_accesses",
                "reading_progresses", "notifications", "chat_conversations", "chat_messages"
        ).forEach(table -> minimumCounts.put(table, seedSize));

        minimumCounts.forEach((table, minimum) -> {
            Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM `" + table + "`", Integer.class);
            if (count == null || count < minimum) {
                throw new IllegalStateException(
                        "Table %s contains %s rows; expected at least %d".formatted(table, count, minimum));
            }
        });

        validateForeignKeys();
        assertNoRows("""
                SELECT COUNT(*)
                FROM chat_conversations conversation
                LEFT JOIN (
                    SELECT conversation_id, COUNT(*) participant_count
                    FROM chat_conversation_participants
                    GROUP BY conversation_id
                ) participant ON participant.conversation_id = conversation.id
                WHERE COALESCE(participant.participant_count, 0) < 2
                """, "Every chat conversation must have at least two participants");
        assertNoRows("""
                SELECT COUNT(*)
                FROM reading_progresses progress
                LEFT JOIN user_digital_accesses access
                    ON access.user_id = progress.user_id
                    AND access.digital_asset_id = progress.digital_asset_id
                    AND access.status = 'ACTIVE'
                WHERE access.id IS NULL
                """, "Reading progress must belong to an active digital access");
        assertNoRows("""
                SELECT COUNT(*)
                FROM orders current_order
                LEFT JOIN order_items item ON item.order_id = current_order.id
                LEFT JOIN payments payment ON payment.order_id = current_order.id AND payment.status = 'PAID'
                LEFT JOIN shipments shipment ON shipment.order_id = current_order.id AND shipment.status = 'DELIVERED'
                WHERE item.id IS NULL OR payment.id IS NULL OR shipment.id IS NULL
                """, "Delivered orders must have items, a paid payment, and a delivered shipment");
        assertMinimumCount(
                "SELECT COUNT(*) FROM books WHERE deleted_at IS NULL AND stock_quantity <= 10",
                DASHBOARD_LOW_STOCK_TARGET,
                "Seed must provide low-stock books for dashboard smoke"
        );
    }

    private void validateSeedConfiguration() {
        adminUsername();
        adminEmail();
        adminFullName();
        seedDefaultPassword();

        if (seedSize < 15 || seedSize > DevelopmentSeedCatalog.BOOKS.size()) {
            throw new IllegalStateException(
                    "app.seed.size must be between 15 and %d".formatted(DevelopmentSeedCatalog.BOOKS.size()));
        }
        if (seedSize - 1 > DevelopmentSeedCatalog.PEOPLE.size()) {
            throw new IllegalStateException("The person seed catalog does not contain enough unique users");
        }
        if (categoryCount < DevelopmentSeedCatalog.MIN_CATEGORY_COUNT
                || categoryCount > DevelopmentSeedCatalog.CATEGORIES.size()) {
            throw new IllegalStateException(
                    "app.seed.category-count must be between %d and %d".formatted(
                            DevelopmentSeedCatalog.MIN_CATEGORY_COUNT,
                            DevelopmentSeedCatalog.CATEGORIES.size()));
        }
    }

    private void validateForeignKeys() {
        List<Map<String, Object>> foreignKeys = jdbcTemplate.queryForList("""
                SELECT TABLE_NAME, COLUMN_NAME, REFERENCED_TABLE_NAME, REFERENCED_COLUMN_NAME
                FROM information_schema.KEY_COLUMN_USAGE
                WHERE TABLE_SCHEMA = DATABASE()
                    AND REFERENCED_TABLE_NAME IS NOT NULL
                """);

        for (Map<String, Object> foreignKey : foreignKeys) {
            String table = foreignKey.get("TABLE_NAME").toString();
            String column = foreignKey.get("COLUMN_NAME").toString();
            String referencedTable = foreignKey.get("REFERENCED_TABLE_NAME").toString();
            String referencedColumn = foreignKey.get("REFERENCED_COLUMN_NAME").toString();
            String query = """
                    SELECT COUNT(*)
                    FROM `%s` child
                    LEFT JOIN `%s` parent ON child.`%s` = parent.`%s`
                    WHERE child.`%s` IS NOT NULL AND parent.`%s` IS NULL
                    """.formatted(table, referencedTable, column, referencedColumn, column, referencedColumn);
            assertNoRows(query, "Orphan foreign key %s.%s".formatted(table, column));
        }
    }

    private boolean isFreshDatabase() {
        Integer userCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", Integer.class);
        Integer bookCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM books", Integer.class);
        return userCount != null && userCount == 1 && bookCount != null && bookCount == 0;
    }

    private void assertNoRows(String query, String message) {
        Integer count = jdbcTemplate.queryForObject(query, Integer.class);
        if (count != null && count > 0) {
            throw new IllegalStateException(message + ": " + count);
        }
    }

    private void assertMinimumCount(String query, int minimum, String message) {
        Integer count = jdbcTemplate.queryForObject(query, Integer.class);
        if (count == null || count < minimum) {
            throw new IllegalStateException(message + ": " + count + " < " + minimum);
        }
    }

    private String adminUsername() {
        return requiredProperty("app.admin.username");
    }

    private String adminEmail() {
        return requiredProperty("app.admin.email");
    }

    private String adminFullName() {
        return requiredProperty("app.admin.last-name") + " " + requiredProperty("app.admin.first-name");
    }

    private String seedDefaultPassword() {
        return requiredProperty("app.seed.default-password");
    }

    private String requiredProperty(String key) {
        String value = environment.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing required seed config: " + key);
        }
        return value.trim();
    }

    private String requiredId(String query, String value) {
        String result = jdbcTemplate.queryForObject(query, String.class, value);
        if (result == null) {
            throw new IllegalStateException("Required seed reference not found: " + value);
        }
        return result;
    }

    private void insert(String sql, Object... args) {
        jdbcTemplate.update(sql, args);
    }

    private static String id(String namespace, int index) {
        return UUID.nameUUIDFromBytes(("bookstore-seed:" + namespace + ":" + index)
                .getBytes(StandardCharsets.UTF_8)).toString();
    }

    private static String hashValue(String namespace, int index) {
        return "%064x".formatted(new java.math.BigInteger(1, (namespace + ":" + index)
                .getBytes(StandardCharsets.UTF_8)));
    }

    private Timestamp time(int minuteOffset) {
        return timestamp(seedBaseTime.plus(minuteOffset, ChronoUnit.MINUTES));
    }

    private static Timestamp timestamp(Instant instant) {
        return Timestamp.valueOf(LocalDateTime.ofInstant(instant, ZoneOffset.UTC));
    }

    private int seedStockQuantity(int index) {
        if (index <= DASHBOARD_LOW_STOCK_TARGET) {
            return index + 2;
        }

        return 24 + (index * 17) % 180;
    }

    private static BigDecimal money(long value) {
        return BigDecimal.valueOf(value).setScale(2);
    }

    private record SeedContext(
            List<String> userIds,
            List<String> customerIds,
            List<String> staffIds,
            List<String> shipperIds
    ) {
    }
}
