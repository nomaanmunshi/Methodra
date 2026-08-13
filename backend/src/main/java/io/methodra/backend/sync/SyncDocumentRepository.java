package io.methodra.backend.sync;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface SyncDocumentRepository extends JpaRepository<SyncDocumentEntity, UUID> {}
