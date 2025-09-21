package com.upsxace.tv_show_tracker.user;

import com.upsxace.tv_show_tracker.common.jwt.AuthService;
import com.upsxace.tv_show_tracker.common.jwt.JwtConfig;
import com.upsxace.tv_show_tracker.user.graphql.JwtResponse;
import com.upsxace.tv_show_tracker.user.graphql.LoginUserInput;
import com.upsxace.tv_show_tracker.user.graphql.RegisterUserInput;
import graphql.GraphQLContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.ContextValue;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;

import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class UserController {
    private final AuthService authService;
    private final JwtConfig jwtConfig;

    @MutationMapping
    public boolean registerUser(@Argument @Valid RegisterUserInput input){
        return authService.register(input);
    }

    @MutationMapping
    public JwtResponse loginUser(@Argument @Valid LoginUserInput input, GraphQLContext context){
        var result = authService.login(input);
        context.put("refreshToken", result.getRefreshTokenOookie(jwtConfig));
        context.put("accessToken", result.getAccessTokenCookie(jwtConfig));
        return new JwtResponse(result.getAccessToken());
    }

    @MutationMapping
    public JwtResponse refreshToken(@ContextValue Optional<String> currentRefreshToken, GraphQLContext context){
        if(currentRefreshToken.isEmpty())
            return null;

        var result = authService.refreshToken(currentRefreshToken.get()).orElse(null);
        if(result == null)
            return null;

        context.put("accessToken", result.getAccessTokenCookie(jwtConfig));
        return new JwtResponse(result.getAccessToken());
    }
}
