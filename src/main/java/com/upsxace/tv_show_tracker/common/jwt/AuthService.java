package com.upsxace.tv_show_tracker.common.jwt;

import com.upsxace.tv_show_tracker.common.exceptions.BadRequestException;
import com.upsxace.tv_show_tracker.common.exceptions.NotFoundException;
import com.upsxace.tv_show_tracker.common.jwt.utils.RefreshResult;
import com.upsxace.tv_show_tracker.user.entity.User;
import com.upsxace.tv_show_tracker.user.repository.UserRepository;
import com.upsxace.tv_show_tracker.user.graphql.LoginUserInput;
import com.upsxace.tv_show_tracker.user.graphql.RegisterUserInput;
import com.upsxace.tv_show_tracker.common.jwt.utils.LoginResult;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service responsible for user authentication, registration, and token management.
 */
@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    /**
     * Generates refresh and access tokens for a user.
     *
     * @param user the user to generate tokens for
     * @param amr  the authentication method reference
     * @return a LoginResult containing refresh and access tokens
     */
    private LoginResult generateTokens(User user, List<String> amr){
        return new LoginResult(
                jwtService.generateRefreshToken(user, amr),
                jwtService.generateAccessToken(user)
        );
    }

    /**
     * Registers a new user in the system.
     *
     * @param input the registration input containing username, email, and password
     * @return true if registration was successful
     * @throws BadRequestException if the email or username is already taken
     */
    public boolean register(RegisterUserInput input){
        if(userRepository.existsByEmail(input.getEmail()))
            throw new BadRequestException("Email is already taken.");
        if(userRepository.existsByUsername(input.getUsername()))
            throw new BadRequestException("Username is already taken.");

        var newUser = User.builder()
                .username(input.getUsername())
                .email(input.getEmail())
                .password(passwordEncoder.encode(input.getPassword()))
                .emailNotifications(true)
                .build();

        userRepository.save(newUser);

        return true;
    }

    /**
     * Authenticates a user and generates tokens.
     *
     * @param input the login input containing username/email and password
     * @return a LoginResult containing refresh and access tokens
     * @throws NotFoundException if no user is found with the provided identifier
     */
    public LoginResult login(LoginUserInput input){
        var user = userRepository.findByUsernameOrEmail(input.getIdentifier(), input.getIdentifier())
                .orElseThrow(() -> new NotFoundException("No account found with the provided username or email."));

        // authenticate user credentials
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        user.getId().toString(),
                        input.getPassword()
                )
        );

        return generateTokens(user, List.of("pwd"));
    }

    /**
     * Refreshes an access token using a valid refresh token.
     *
     * @param currentRefreshToken the current refresh token
     * @return Optional containing a RefreshResult if token is valid
     */
    public Optional<RefreshResult> refreshToken(String currentRefreshToken){
        return jwtService.refreshAccessToken(currentRefreshToken).map(RefreshResult::new);
    }
}
