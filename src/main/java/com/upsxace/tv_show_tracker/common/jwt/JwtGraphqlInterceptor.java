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

@Component
public class JwtGraphqlInterceptor implements WebGraphQlInterceptor {
    private UserContext getUserContext(){
        var authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            return null;
        }

        return (UserContext) authentication.getPrincipal();
    }

    @NotNull
    @Override
    public Mono<WebGraphQlResponse> intercept(@NotNull WebGraphQlRequest request, Chain chain) {
        Map<String, Object> ctx = new HashMap<>();

        // inject user context from spring security in graphql context before request
        var userCtx = getUserContext();
        if(userCtx != null)
            ctx.put("userCtx", userCtx);

        // inject current refresh token in context in case it needs to be verified to refresh a token
        var currentRefreshToken = Optional.of(request.getCookies())
                .map(c -> c.get("refreshToken"))
                .orElse(null);
        if(currentRefreshToken != null && currentRefreshToken.getFirst() != null){
            ctx.put("currentRefreshToken", currentRefreshToken.getFirst().getValue());
        }

        // set context
        request.configureExecutionInput((executionInput, builder) ->
                builder.graphQLContext(ctx).build());

        // check graphql context before response searching for new access/refresh tokens to be injected in the cookies
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