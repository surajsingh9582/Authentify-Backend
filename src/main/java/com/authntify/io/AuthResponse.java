package com.authntify.io;


import lombok.*;

@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
@Data
public class AuthResponse {
    private String email;
    private String token;
}
