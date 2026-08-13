package io.methodra.backend.sync;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "sync_documents")
public class SyncDocumentEntity {
    @Id
    @Column(name = "user_id")
    private UUID userId;
    @Column(name = "sync_version", nullable = false)
    private long syncVersion;
    @Column(name = "payload", nullable = false, columnDefinition = "text")
    private String payload;
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
    @Version
    @Column(name = "row_version", nullable = false)
    private long rowVersion;

    protected SyncDocumentEntity() {}

    public SyncDocumentEntity(UUID userId, long syncVersion, String payload, Instant updatedAt) {
        this.userId = userId;
        this.syncVersion = syncVersion;
        this.payload = payload;
        this.updatedAt = updatedAt;
    }

    public UUID getUserId() { return userId; }
    public long getSyncVersion() { return syncVersion; }
    public String getPayload() { return payload; }
    public Instant getUpdatedAt() { return updatedAt; }

    public void replace(long newVersion, String newPayload, Instant now) {
        this.syncVersion = newVersion;
        this.payload = newPayload;
        this.updatedAt = now;
    }
}
