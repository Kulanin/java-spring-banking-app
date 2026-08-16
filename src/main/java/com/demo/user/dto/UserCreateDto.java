package com.demo.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserCreateDto {
    @NotBlank(message = "Name is required")
    @Size(min = 3, max = 50, message = "Name must be between 3 and 50 characters")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    // @NotBlank(message = "Password is required")
    // @Size(min = 8, message = "Password must be at least 8 characters")
    // @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$",
    // message = "Password must contain at least one letter and one number")
    // private String password;
}
