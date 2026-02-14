package com.yudhassif.election.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticationRequest {
    @NotNull(message = "email is required")
    @Email(message = "Invalid email format")
    private  String email;
    @NotNull(message = "Password is required")
//    @Pattern(
//            regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=]).{8,}$",
//            message = "Password must contain uppercase, lowercase, number, special character and be at least 8 characters"
//    )

    private  String password;
}

