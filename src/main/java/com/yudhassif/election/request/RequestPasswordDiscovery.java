package com.yudhassif.election.request;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestPasswordDiscovery {
    @Email(message = "Invalid email format")
    @NotNull(message = "Email is required")
    private String email;
}