package com.bookstore.bookstore.infrastructure.persistence.entity;

import com.bookstore.bookstore.domain.enums.PurchaseItemType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "cart_items",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_cart_items_cart_book_type", columnNames = {"cart_id", "book_id", "item_type"}),
                @UniqueConstraint(name = "uk_cart_items_cart_digital_type", columnNames = {"cart_id", "digital_asset_id", "item_type"})
        },
        indexes = {
                @Index(name = "idx_cart_items_cart_id", columnList = "cart_id"),
                @Index(name = "idx_cart_items_digital_asset_id", columnList = "digital_asset_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
public class CartItemJpaEntity {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cart_id", nullable = false)
    private CartJpaEntity cart;

    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", length = 32)
    private PurchaseItemType itemType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id")
    private BookJpaEntity book;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "digital_asset_id")
    private DigitalAssetJpaEntity digitalAsset;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
