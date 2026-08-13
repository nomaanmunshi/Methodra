package io.methodra.backend.sync;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SyncServiceTest {
    @Test
    void staleBaseVersionRaisesConflictAndDoesNotOverwriteServerState() {
        var repository = mock(SyncDocumentRepository.class);
        var userId = UUID.randomUUID();
        var existing = new SyncDocumentEntity(userId, 4, "{\"server\":true}", Instant.now());
        when(repository.findById(userId)).thenReturn(Optional.of(existing));
        var service = new SyncService(repository);

        var conflict = assertThrows(SyncService.SyncConflictException.class,
                () -> service.put(userId, 3, "{\"client\":true}"));

        assertEquals(4, conflict.currentVersion());
        verify(repository, never()).save(any());
        assertEquals("{\"server\":true}", existing.getPayload());
    }

    @Test
    void matchingBaseVersionAdvancesExactlyOnce() {
        var repository = mock(SyncDocumentRepository.class);
        var userId = UUID.randomUUID();
        var existing = new SyncDocumentEntity(userId, 2, "{}", Instant.now());
        when(repository.findById(userId)).thenReturn(Optional.of(existing));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        var service = new SyncService(repository);

        var state = service.put(userId, 2, "{\"ok\":true}");

        assertEquals(3, state.version());
        assertEquals("{\"ok\":true}", state.payload());
        verify(repository).save(existing);
    }
}
