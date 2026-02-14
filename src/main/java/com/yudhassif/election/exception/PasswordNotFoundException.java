package com.yudhassif.election.exception;


import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class PasswordNotFoundException extends RuntimeException {
    private final String message;
}
