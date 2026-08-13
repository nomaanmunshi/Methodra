package io.methodra.backend.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface ApiTokenRepository extends JpaRepository<ApiTokenEntity, UUID> {
    Optional<ApiTokenEntity> findByTokenHash(String tokenHash);
}
