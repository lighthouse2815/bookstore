package com.bookstore.bookstore.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "book_images")
@Getter
@Setter
@NoArgsConstructor
public class BookImageJpaEntity {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id", nullable = false)
    private BookJpaEntity book;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_asset_id")
    private FileAssetJpaEntity fileAsset;

    @Column(name = "image_url", columnDefinition = "TEXT")
    private String imageUrl;

    @Column(name = "primary_image", nullable = false)
    private Boolean primaryImage;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @Column(name = "alt_text", columnDefinition = "TEXT")
    private String altText;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
