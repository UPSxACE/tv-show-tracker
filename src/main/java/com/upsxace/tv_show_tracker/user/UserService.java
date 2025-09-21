package com.upsxace.tv_show_tracker.user;

import com.upsxace.tv_show_tracker.common.exceptions.BadRequestException;
import com.upsxace.tv_show_tracker.user.graphql.RegisterUserInput;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

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
}
