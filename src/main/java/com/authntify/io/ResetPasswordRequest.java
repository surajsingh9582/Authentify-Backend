package com.authntify.io;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResetPasswordRequest {
    @NotNull(message = "Email is required")
    private String email;
    @NotNull(message = "otp is required")
    private String otp;
    @NotNull(message = "password is required")
    private String password;
}
