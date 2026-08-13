package io.methodra.backend.api;

import io.methodra.backend.sync.SyncService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/sync")
public class SyncController {
    private final SyncService sync;

    public SyncController(SyncService sync) { this.sync = sync; }

    @GetMapping("/state")
    public SyncService.SyncState get(Authentication authentication) {
        return sync.get(UUID.fromString(authentication.getName()));
    }

    @PutMapping("/state")
    public SyncService.SyncState put(Authentication authentication, @Valid @RequestBody SyncRequest request) {
        return sync.put(UUID.fromString(authentication.getName()), request.baseVersion(), request.payload());
    }

    public record SyncRequest(@Min(0) long baseVersion, @NotBlank String payload) {}
}
