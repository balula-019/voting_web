package com.yudhassif.election.request;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class ResetPasswordRequest {

    @NotBlank
    private String rawResetToken;

    @NotBlank(message = "New password is required")
    @Size(min = 8, max = 64)
    private String newPassword;

    @NotBlank(message = " password is required")
    @Size(min = 8, max = 64)
    private String ConfirmPassword;
}
