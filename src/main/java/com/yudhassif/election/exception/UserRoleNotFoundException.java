package com.yudhassif.election.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserRoleNotFoundException extends RuntimeException{
    private final String msg;
}
