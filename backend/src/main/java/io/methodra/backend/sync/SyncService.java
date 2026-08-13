package io.methodra.backend.sync;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class SyncService {
    private final SyncDocumentRepository repository;

    public SyncService(SyncDocumentRepository repository) { this.repository = repository; }

    @Transactional(readOnly = true)
    public SyncState get(UUID userId) {
        return repository.findById(userId)
                .map(entity -> new SyncState(entity.getSyncVersion(), entity.getPayload(), entity.getUpdatedAt()))
                .orElseGet(() -> new SyncState(0, "{}", null));
    }

    @Transactional
    public SyncState put(UUID userId, long baseVersion, String payload) {
        if (payload == null || payload.isBlank()) throw new IllegalArgumentException("payload must not be blank");
        if (payload.length() > 1_000_000) throw new IllegalArgumentException("payload exceeds 1 MB sync limit");

        SyncDocumentEntity entity = repository.findById(userId).orElse(null);
        long current = entity == null ? 0 : entity.getSyncVersion();
        if (baseVersion != current) throw new SyncConflictException(current);
        Instant now = Instant.now();
        long next = current + 1;
        if (entity == null) entity = new SyncDocumentEntity(userId, next, payload, now);
        else entity.replace(next, payload, now);
        repository.save(entity);
        return new SyncState(next, payload, now);
    }

    public record SyncState(long version, String payload, Instant updatedAt) {}

    public static class SyncConflictException extends RuntimeException {
        private final long currentVersion;
        public SyncConflictException(long currentVersion) {
            super("Sync conflict: client base version is stale");
            this.currentVersion = currentVersion;
        }
        public long currentVersion() { return currentVersion; }
    }
}
