package com.bookstore.bookstore.domain.model;

import com.bookstore.bookstore.domain.exception.DomainErrorCode;
import com.bookstore.bookstore.domain.exception.DomainException;
import com.bookstore.bookstore.domain.validation.Guard;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;

@Getter
public class ReadingProgress {

    private UUID id;
    private UUID userId;
    private UUID digitalAssetId;
    private Integer currentPage;
    private BigDecimal progressPercent;
    private String positionData;
    private Instant lastReadAt;
    private Instant createdAt;
    private Instant updatedAt;

    public ReadingProgress(
            UUID id,
            UUID userId,
            UUID digitalAssetId,
            Integer currentPage,
            BigDecimal progressPercent,
            String positionData,
            Instant lastReadAt,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = Guard.notNull(id, DomainErrorCode.INVALID_READING_PROGRESS_ID, "id");
        setUserId(userId);
        setDigitalAssetId(digitalAssetId);
        setCurrentPage(currentPage);
        setProgressPercent(progressPercent);
        setPositionData(positionData);
        setCreatedAt(createdAt);
        setUpdatedAt(updatedAt);
        setLastReadAt(lastReadAt);
    }

    public void updateProgress(
            Integer currentPage,
            BigDecimal progressPercent,
            String positionData,
            Instant lastReadAt
    ) {
        setCurrentPage(currentPage);
        setProgressPercent(progressPercent);
        setPositionData(positionData);
        setUpdatedAt(lastReadAt);
        setLastReadAt(lastReadAt);
    }

    private void setUserId(UUID userId) {
        this.userId = Guard.notNull(userId, DomainErrorCode.INVALID_READING_PROGRESS_USER_ID, "userId");
    }

    private void setDigitalAssetId(UUID digitalAssetId) {
        this.digitalAssetId = Guard.notNull(
                digitalAssetId,
                DomainErrorCode.INVALID_READING_PROGRESS_DIGITAL_ASSET_ID,
                "digitalAssetId"
        );
    }

    private void setCurrentPage(Integer currentPage) {
        if (currentPage != null && currentPage < 0) {
            throw new DomainException(DomainErrorCode.INVALID_READING_PROGRESS_CURRENT_PAGE, "currentPage");
        }
        this.currentPage = currentPage;
    }

    private void setProgressPercent(BigDecimal progressPercent) {
        if (progressPercent != null
                && (progressPercent.compareTo(BigDecimal.ZERO) < 0
                || progressPercent.compareTo(BigDecimal.valueOf(100)) > 0)) {
            throw new DomainException(DomainErrorCode.INVALID_READING_PROGRESS_PERCENT, "progressPercent");
        }
        this.progressPercent = progressPercent;
    }

    private void setPositionData(String positionData) {
        this.positionData = Guard.notBlankOrNull(
                positionData,
                DomainErrorCode.INVALID_READING_PROGRESS_POSITION_DATA,
                "positionData"
        );
    }

    private void setLastReadAt(Instant lastReadAt) {
        Instant validLastReadAt = Guard.notInFuture(
                lastReadAt,
                DomainErrorCode.INVALID_READING_PROGRESS_LAST_READ_AT,
                "lastReadAt"
        );
        Guard.notBefore(
                validLastReadAt,
                this.createdAt,
                DomainErrorCode.INVALID_READING_PROGRESS_AUDIT_ORDER,
                "lastReadAt",
                "createdAt"
        );
        this.lastReadAt = validLastReadAt;
    }

    private void setCreatedAt(Instant createdAt) {
        this.createdAt = Guard.notInFuture(
                createdAt,
                DomainErrorCode.INVALID_READING_PROGRESS_CREATED_AT,
                "createdAt"
        );
    }

    private void setUpdatedAt(Instant updatedAt) {
        Instant validUpdatedAt = Guard.notInFutureOrNull(
                updatedAt,
                DomainErrorCode.INVALID_READING_PROGRESS_UPDATED_AT,
                "updatedAt"
        );
        Guard.notBefore(
                validUpdatedAt,
                this.createdAt,
                DomainErrorCode.INVALID_READING_PROGRESS_AUDIT_ORDER,
                "updatedAt",
                "createdAt"
        );
        this.updatedAt = validUpdatedAt;
    }
}
