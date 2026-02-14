package com.yudhassif.election.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class MessageNotFoundException extends RuntimeException {
    private final String msg;
}
