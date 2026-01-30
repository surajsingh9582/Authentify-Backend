package com.authntify.io;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class ProfileRequest {
    @NotNull(message = "Name should not be empty")
    private String name;
    @Email(message = "Enter Valid Email id")
    @NotNull(message = "Email should not be empty")
    private String email;
    @Size(min = 6,message = "Password should be more than 6")
    private String password;
}
