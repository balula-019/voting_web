package com.yudhassif.election.generator;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class VoterIdGenerator {

    private static final String CHAR_POOL =
            "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // no confusing chars

    private static final int LENGTH = 12;
    private final SecureRandom random = new SecureRandom();

    public String generate() {

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < LENGTH; i++) {
            sb.append(CHAR_POOL.charAt(random.nextInt(CHAR_POOL.length())));
        }

        // format XXXX-XXXX-XXXX
        return sb.substring(0,4) + "-" +
                sb.substring(4,8) + "-" +
                sb.substring(8,12);
    }
}

