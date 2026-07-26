package com.bookstore.bookstore.application.service;

import com.bookstore.bookstore.application.command.EnqueueOutboxEventCommand;
import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import com.bookstore.bookstore.application.port.in.ITransactionalOutboxService;
import com.bookstore.bookstore.application.port.out.IOutboxEventRepository;
import com.bookstore.bookstore.application.result.OutboxEventResult;
import com.bookstore.bookstore.application.result.PageSliceResult;
import com.bookstore.bookstore.domain.enums.OutboxStatus;
import com.bookstore.bookstore.domain.model.OutboxEvent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Iterator;
import java.util.Locale;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TransactionalOutboxService implements ITransactionalOutboxService {
    private final IOutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OutboxEventResult enqueue(EnqueueOutboxEventCommand command) {
        if (command == null || command.aggregateId() == null || blank(command.aggregateType())
                || blank(command.eventType()) || blank(command.deduplicationSeed())) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "outbox event");
        }
        JsonNode payloadNode = objectMapper.valueToTree(command.payload());
        if (payloadNode == null || payloadNode.isNull() || containsSensitiveField(payloadNode)) {
            throw new ApplicationException(ApplicationErrorCode.OUTBOX_PAYLOAD_INVALID);
        }
        String key = digest(command.deduplicationSeed());
        OutboxEvent existing = outboxEventRepository.findByDeduplicationKey(key).orElse(null);
        if (existing != null) return toResult(existing);
        Instant now = Instant.now();
        OutboxEvent event = new OutboxEvent(UUID.randomUUID(), command.aggregateType().trim(), command.aggregateId(),
                command.eventType().trim(), serialize(payloadNode), key, OutboxStatus.PENDING, 0, now,
                null, null, null, now, null, now, 0);
        return toResult(outboxEventRepository.save(event));
    }

    @Override
    @Transactional(readOnly = true)
    public PageSliceResult<OutboxEventResult> getPage(int page, int size, OutboxStatus status) {
        if (page < 0 || size < 1 || size > 200) throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "page/size");
        return outboxEventRepository.findPage(page, size, status).map(this::toResult);
    }

    @Override
    @Transactional(readOnly = true)
    public OutboxEventResult getById(UUID id) { return toResult(find(id)); }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OutboxEventResult retry(UUID id) {
        OutboxEvent event = outboxEventRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.OUTBOX_EVENT_NOT_FOUND));
        if (event.getStatus() == OutboxStatus.SUCCEEDED || event.getStatus() == OutboxStatus.PROCESSING) {
            throw new ApplicationException(ApplicationErrorCode.OUTBOX_EVENT_RETRY_NOT_ALLOWED);
        }
        event.retry(Instant.now());
        return toResult(outboxEventRepository.save(event));
    }

    private OutboxEvent find(UUID id) {
        if (id == null) throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "id");
        return outboxEventRepository.findById(id).orElseThrow(() -> new ApplicationException(ApplicationErrorCode.OUTBOX_EVENT_NOT_FOUND));
    }
    private String serialize(JsonNode payload) {
        try { return objectMapper.writeValueAsString(payload); }
        catch (Exception exception) { throw new ApplicationException(ApplicationErrorCode.OUTBOX_PAYLOAD_INVALID); }
    }
    private boolean containsSensitiveField(JsonNode node) {
        if (node.isObject()) {
            Iterator<String> names = node.fieldNames();
            while (names.hasNext()) {
                String name = names.next();
                String normalized = name.replace("-", "").replace("_", "").toLowerCase(Locale.ROOT);
                if (normalized.contains("password") || normalized.contains("token") || normalized.contains("secret")
                        || normalized.contains("authorization") || normalized.contains("otp")) return true;
                if (containsSensitiveField(node.get(name))) return true;
            }
        } else if (node.isArray()) {
            for (JsonNode child : node) if (containsSensitiveField(child)) return true;
        }
        return false;
    }
    private boolean blank(String value) { return value == null || value.trim().isEmpty(); }
    private String digest(String value) {
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8))); }
        catch (Exception exception) { throw new IllegalStateException("SHA-256 unavailable", exception); }
    }
    private OutboxEventResult toResult(OutboxEvent event) {
        return new OutboxEventResult(event.getId(), event.getAggregateType(), event.getAggregateId(), event.getEventType(), event.getStatus(),
                event.getAttemptCount(), event.getNextAttemptAt(), event.getLockedAt(), event.getLockedBy(), event.getLastError(), event.getCreatedAt(), event.getProcessedAt());
    }
}
