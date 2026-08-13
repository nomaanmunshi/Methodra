package io.methodra.backend.security;

import io.methodra.backend.auth.ApiTokenRepository;
import io.methodra.backend.auth.TokenCodec;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

@Component
public class BearerTokenFilter extends OncePerRequestFilter {
    private final ApiTokenRepository tokens;

    public BearerTokenFilter(ApiTokenRepository tokens) { this.tokens = tokens; }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ") && SecurityContextHolder.getContext().getAuthentication() == null) {
            String raw = header.substring(7).trim();
            if (!raw.isEmpty()) {
                tokens.findByTokenHash(TokenCodec.hash(raw))
                        .filter(token -> token.getExpiresAt().isAfter(Instant.now()))
                        .ifPresent(token -> {
                            var auth = new UsernamePasswordAuthenticationToken(
                                    token.getUserId().toString(), raw, List.of(new SimpleGrantedAuthority("ROLE_USER"))
                            );
                            SecurityContextHolder.getContext().setAuthentication(auth);
                        });
            }
        }
        filterChain.doFilter(request, response);
    }
}
