package com.yudhassif.election.mapper;

import com.yudhassif.election.entity.Election;
import com.yudhassif.election.entity.Student;
import com.yudhassif.election.response.ElectionResponse;
import com.yudhassif.election.response.StudentResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Getter
@Service
public class ElectionMapper {
    public ElectionResponse toElectionResponse(Election election) {

        return new ElectionResponse
                (
                        election.getId(),
                        election.getElectionName(),
                        election.getStatus().name(),
                        election.getDescription(),
                        election.getAcademicYear(),
                        election.getVotingStartTime(),
                        election.getSemester().getCode(),
                        election.getVotingEndTime()
                );
    }

}
