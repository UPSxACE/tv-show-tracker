package com.upsxace.tv_show_tracker.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    @Order(-1)
    @Profile("!prod")
    public SecurityFilterChain developmentSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/graphiql")
                .authorizeHttpRequests(registry -> registry
                        .requestMatchers("/graphiql").permitAll()
                );

        return http.build();
    }

    @Bean
    @Order(0)
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        // TODO: CORS

        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        http.csrf(AbstractHttpConfigurer::disable);

        // TODO: jwt filter

        http.authorizeHttpRequests(registry -> registry
                .requestMatchers("/graphql").permitAll()
                .anyRequest().denyAll()
        );

        http.exceptionHandling(c -> {
            c.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)); // return 401 when requiring authentication
            c.accessDeniedHandler(((request, response, accessDeniedException) ->
                    response.setStatus(HttpStatus.FORBIDDEN.value())) // return 403 on access denied
            );
        });

        return http.build();
    }
}
