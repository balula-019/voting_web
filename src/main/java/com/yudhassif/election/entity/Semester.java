package com.yudhassif.election.entity;

import lombok.Getter;

@Getter
public enum Semester {
    SEMESTER_ONE("S1"),
    SEMESTER_TWO("S2");

    private final String code;

    Semester(String code) {
        this.code = code;
    }

}

// for enum no need of any annotation