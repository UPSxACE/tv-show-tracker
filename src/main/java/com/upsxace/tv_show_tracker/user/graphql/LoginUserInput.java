package com.upsxace.tv_show_tracker.user.graphql;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginUserInput {
    @NotBlank(message = "Username or email is required.")
    private String identifier;

    @NotBlank(message = "Password is required.")
    private String password;
}
