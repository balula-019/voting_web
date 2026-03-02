package com.yudhassif.election.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class PositionResponse {

    private Long id;
    private String name;
    private Long electionId;
//    private List<LeaderResponse> leaders;  // todo  later i will implement after complete the admin approval and admin elect leader
    private String message;
    private LocalDateTime createdAt;
}
