package io.methodra.backend.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.UUID;

@Service
public class AuthService {
    private final UserRepository users;
    private final ApiTokenRepository tokens;
    private final PasswordEncoder encoder;
    private final long tokenDays;

    public AuthService(UserRepository users, ApiTokenRepository tokens, PasswordEncoder encoder,
                       @Value("${methodra.token-days:30}") long tokenDays) {
        this.users = users;
        this.tokens = tokens;
        this.encoder = encoder;
        this.tokenDays = tokenDays;
    }

    @Transactional
    public AuthResult register(String email, String password) {
        String normalized = normalizeEmail(email);
        if (users.findByEmail(normalized).isPresent()) throw new IllegalArgumentException("Email is already registered");
        UserEntity user = users.save(new UserEntity(UUID.randomUUID(), normalized, encoder.encode(password), Instant.now()));
        return issue(user);
    }

    @Transactional
    public AuthResult login(String email, String password) {
        UserEntity user = users.findByEmail(normalizeEmail(email)).orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));
        if (!encoder.matches(password, user.getPasswordHash())) throw new IllegalArgumentException("Invalid credentials");
        return issue(user);
    }

    private AuthResult issue(UserEntity user) {
        String token = TokenCodec.newToken();
        Instant expiresAt = Instant.now().plus(tokenDays, ChronoUnit.DAYS);
        tokens.save(new ApiTokenEntity(UUID.randomUUID(), user.getId(), TokenCodec.hash(token), Instant.now(), expiresAt));
        return new AuthResult(user.getId(), user.getEmail(), token, expiresAt);
    }

    private String normalizeEmail(String email) { return email.trim().toLowerCase(Locale.ROOT); }

    public record AuthResult(UUID userId, String email, String token, Instant expiresAt) {}
}
