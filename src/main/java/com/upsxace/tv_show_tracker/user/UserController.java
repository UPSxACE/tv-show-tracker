package com.upsxace.tv_show_tracker.user;

import com.upsxace.tv_show_tracker.user.graphql.RegisterUserInput;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @MutationMapping
    public boolean registerUser(@Argument @Valid RegisterUserInput input){
        return userService.register(input);
    }
}
