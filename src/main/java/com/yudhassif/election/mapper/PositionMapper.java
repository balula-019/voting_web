package com.yudhassif.election.mapper;

import com.yudhassif.election.candidates.Position;
import com.yudhassif.election.response.PositionResponse;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class PositionMapper {

    public PositionResponse toPositionResponse(Position position) {
        return PositionResponse.builder()
                .id(position.getId())
                .name(position.getName())
                .electionId(position.getElection().getId())
//                .leaders(Collections.emptyList()) // initially empty
                .message("Position created successfully")
                .createdAt(LocalDateTime.now())
                .build();
    }
}
