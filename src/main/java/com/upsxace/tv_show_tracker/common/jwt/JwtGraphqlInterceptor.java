package com.upsxace.tv_show_tracker.common.jwt;

import org.jetbrains.annotations.NotNull;
import org.springframework.graphql.server.WebGraphQlInterceptor;
import org.springframework.graphql.server.WebGraphQlRequest;
import org.springframework.graphql.server.WebGraphQlResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Interceptor for GraphQL requests that injects JWT user context and handles
 * access/refresh tokens via cookies.
 */
@Component
public class JwtGraphqlInterceptor implements WebGraphQlInterceptor {

    /**
     * Retrieves the current authenticated user context from Spring Security.
     *
     * @return UserContext if authenticated, otherwise null
     */
    private UserContext getUserContext(){
        var authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            return null;
        }

        return (UserContext) authentication.getPrincipal();
    }

    /**
     * Intercepts GraphQL requests to inject user context and handle JWT tokens in cookies.
     *
     * @param request the incoming GraphQL request
     * @param chain   interceptor chain
     * @return Mono of GraphQL response
     */
    @NotNull
    @Override
    public Mono<WebGraphQlResponse> intercept(@NotNull WebGraphQlRequest request, Chain chain) {
        Map<String, Object> ctx = new HashMap<>();

        // Inject user context from Spring Security into GraphQL context
        var userCtx = getUserContext();
        if(userCtx != null)
            ctx.put("userCtx", userCtx);

        // Inject current refresh token from cookies into context
        var currentRefreshToken = Optional.of(request.getCookies())
                .map(c -> c.get("refreshToken"))
                .orElse(null);
        if(currentRefreshToken != null && currentRefreshToken.getFirst() != null){
            ctx.put("currentRefreshToken", currentRefreshToken.getFirst().getValue());
        }

        // Configure GraphQL context for this request
        request.configureExecutionInput((executionInput, builder) ->
                builder.graphQLContext(ctx).build());

        // After execution, set any new access/refresh tokens in response cookies
        return chain.next(request).doOnNext((response) -> {
            String refreshToken = response.getExecutionInput().getGraphQLContext().get("refreshToken");
            if(refreshToken != null)
                response.getResponseHeaders().add(HttpHeaders.SET_COOKIE, refreshToken);
            String accessToken = response.getExecutionInput().getGraphQLContext().get("accessToken");
            if(accessToken != null)
                response.getResponseHeaders().add(HttpHeaders.SET_COOKIE, accessToken);
        });
    }
}
