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

/**
 * Service for generating, validating, and refreshing JWT tokens.
 * Supports both access and refresh tokens.
 */
@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtConfig jwtConfig;
    private final UserRepository userRepository;

    /**
     * Returns the secret key used for signing JWTs.
     *
     * @return SecretKey
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtConfig.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Parses the JWT and returns its claims.
     *
     * @param token JWT token
     * @return Claims object
     */
    public Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Creates a UserContext from a valid access token.
     *
     * @param token JWT access token
     * @return Optional UserContext, empty if token invalid
     */
    public Optional<UserContext> createUserContextFromToken(String token) {
        try {
            var claims = getClaims(token);

            // token must be of type access
            if(!claims.get("token_type", String.class).equals("access"))
                throw new BadRequestException();

            var id = claims.getSubject();
            var authorities = AuthorityUtils.commaSeparatedStringToAuthorityList(
                    claims.get("authorities", String.class)
            );
            return Optional.of(new UserContext(UUID.fromString(id), authorities));
        } catch (JwtException ex) {
            return Optional.empty();
        }
    }

    /**
     * Generates a refresh token for a given user.
     *
     * @param user user entity
     * @param amr authentication method references
     * @return JWT refresh token string
     */
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

    /**
     * Generates a refresh token by user ID.
     *
     * @param id user UUID
     * @param amr authentication method references
     * @return JWT refresh token string
     */
    public String generateRefreshToken(UUID id, List<String> amr) {
        var user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found."));
        return generateRefreshToken(user, amr);
    }

    /**
     * Generates an access token for a given user.
     *
     * @param user user entity
     * @return JWT access token string
     */
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

    /**
     * Generates an access token by user ID.
     *
     * @param id user UUID
     * @return JWT access token string
     */
    public String generateAccessToken(UUID id) {
        var user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found."));
        return generateAccessToken(user);
    }

    /**
     * Refreshes an access token using a valid refresh token.
     *
     * @param currentRefreshToken current refresh token
     * @return Optional new access token, empty if refresh token invalid
     */
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
