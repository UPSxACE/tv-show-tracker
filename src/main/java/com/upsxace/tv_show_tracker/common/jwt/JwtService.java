package com.upsxace.tv_show_tracker.common.jwt;

import com.upsxace.tv_show_tracker.common.exceptions.BadRequestException;
import com.upsxace.tv_show_tracker.common.exceptions.NotFoundException;
import com.upsxace.tv_show_tracker.user.entity.User;
import com.upsxace.tv_show_tracker.user.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JwtService {
    private final JwtConfig jwtConfig;
    private final UserRepository userRepository;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtConfig.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Optional<UserContext> createUserContextFromToken(String token) {
        try {
            var claims = getClaims(token);

            // token must be of type access
            if(!claims.get("token_type", String.class).equals("access"))
                throw new BadRequestException();

            var id = claims.getSubject();
            var authorities = AuthorityUtils.commaSeparatedStringToAuthorityList(claims.get("authorities", String.class));
            return Optional.of(new UserContext(UUID.fromString(id), authorities));
        } catch (JwtException ex) {
            return Optional.empty();
        }
    }

    public String generateRefreshToken(User user, List<String> amr) {
        final long TOKEN_EXPIRATION_MS = jwtConfig.getRefreshToken().getDuration() * 60 * 1000;

        return Jwts.builder()
                .subject(user.getId().toString())
                .issuer("tekker-tv-api")
                .claim("token_type", "refresh")
                .claim("amr", amr)
                .issuedAt(new Date())
                .expiration(new Date(new Date().getTime() + TOKEN_EXPIRATION_MS))
                .signWith(getSigningKey())
                .compact();
    }

    public String generateRefreshToken(UUID id, List<String> amr) {
        var user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found."));

        return generateRefreshToken(user, amr);
    }

    public String generateAccessToken(User user) {
        final long TOKEN_EXPIRATION_MS = jwtConfig.getAccessToken().getDuration() * 60 * 1000;

        return Jwts.builder()
                .subject(user.getId().toString())
                .issuer("tekker-tv-api")
                .claim("token_type", "access")
                .claim("authorities", String.join(",", List.of("ROLE_USER")))
                .issuedAt(new Date())
                .expiration(new Date(new Date().getTime() + TOKEN_EXPIRATION_MS))
                .signWith(getSigningKey())
                .compact();
    }

    public String generateAccessToken(UUID id) {
        var user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found."));

        return generateAccessToken(user);
    }

    public Optional<String> refreshAccessToken(String currentRefreshToken){
        try {
            var claims = getClaims(currentRefreshToken);

            // token must be of type refresh
            if(!claims.get("token_type", String.class).equals("refresh"))
                throw new BadRequestException();

            var id = claims.getSubject();

            return Optional.of(generateAccessToken(UUID.fromString(id)));
        } catch (JwtException ex) {
            return Optional.empty();
        }
    }
}
