package com.upsxace.tv_show_tracker.common.jwt;

import com.upsxace.tv_show_tracker.common.exceptions.BadRequestException;
import com.upsxace.tv_show_tracker.common.exceptions.NotFoundException;
import com.upsxace.tv_show_tracker.common.jwt.utils.RefreshResult;
import com.upsxace.tv_show_tracker.user.User;
import com.upsxace.tv_show_tracker.user.UserRepository;
import com.upsxace.tv_show_tracker.user.graphql.LoginUserInput;
import com.upsxace.tv_show_tracker.user.graphql.RegisterUserInput;
import com.upsxace.tv_show_tracker.common.jwt.utils.LoginResult;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.sql.Ref;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    private LoginResult generateTokens(User user, List<String> amr){
        return new LoginResult(
                jwtService.generateRefreshToken(user, amr),
                jwtService.generateAccessToken(user)
        );
    }

    public boolean register(RegisterUserInput input){
        if(userRepository.existsByEmail(input.getEmail()))
            throw new BadRequestException("Email is already taken.");
        if(userRepository.existsByUsername(input.getUsername()))
            throw new BadRequestException("Username is already taken.");

        var newUser = User.builder()
                .username(input.getUsername())
                .email(input.getEmail())
                .password(passwordEncoder.encode(input.getPassword()))
                .build();

        userRepository.save(newUser);

        return true;
    }

    public LoginResult login(LoginUserInput input){
        var user = userRepository.findByUsernameOrEmail(input.getIdentifier(), input.getIdentifier())
                .orElseThrow(() -> new NotFoundException("No account found with the provided username or email."));

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        user.getId().toString(),
                        input.getPassword()
                )
        );

        return generateTokens(user, List.of("pwd"));
    }

    public Optional<RefreshResult> refreshToken(String currentRefreshToken){
        return jwtService.refreshAccessToken(currentRefreshToken).map(RefreshResult::new);
    }
}
