package com.upsxace.tv_show_tracker.common.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

/**
 * Filter that authenticates incoming HTTP requests using JWT tokens.
 * It extracts the JWT either from the Authorization header or from cookies,
 * validates it, and sets the SecurityContext accordingly.
 */
@Component
@AllArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {
    private final JwtService jwtService;

    /**
     * Extracts the JWT token from the request header or cookies.
     *
     * @param request the incoming HTTP request
     * @return the extracted token, or null if not found
     */
    private String extractToken(HttpServletRequest request){
        var authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.replace("Bearer ", "");
        }

        if(request.getCookies() == null)
            return null;

        var cookie = Arrays.stream(request.getCookies())
                .filter(c -> c.getName().equals("accessToken"))
                .findFirst();

        return cookie.map(Cookie::getValue).orElse(null);
    }

    /**
     * Filters each request, performing JWT authentication if a token is present.
     *
     * @param request the HTTP request
     * @param response the HTTP response
     * @param filterChain the filter chain
     * @throws ServletException in case of servlet errors
     * @throws IOException in case of I/O errors
     */
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        var token = extractToken(request);
        if(token == null){
            filterChain.doFilter(request, response);
            return;
        }

        var userContext = jwtService.createUserContextFromToken(token).orElse(null);
        if (userContext == null) {
            filterChain.doFilter(request, response);
            return;
        }

        var authentication = new UsernamePasswordAuthenticationToken(
                userContext,
                null,
                userContext.getAuthorities()
        );

        // Adds request metadata (e.g., IP address) to the authentication object
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }
}
