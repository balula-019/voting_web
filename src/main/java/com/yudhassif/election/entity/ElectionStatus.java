package com.yudhassif.election.entity;

public enum ElectionStatus {
    ACTIVE,       // it means the election is ongoing so the student can vote
    PENDING,     // it means the election is created but not started or active
    CLOSED,      // it means the election is ended
    SUSPENDED   // it means the election is active but may be suddenly admin stop may be due to different problem like server being down
}
