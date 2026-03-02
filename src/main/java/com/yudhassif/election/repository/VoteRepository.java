package com.yudhassif.election.repository;

import com.yudhassif.election.candidates.Position;
import com.yudhassif.election.candidates.Vote;
import com.yudhassif.election.entity.Election;
import com.yudhassif.election.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VoteRepository extends JpaRepository<Vote, Long> {
    boolean existsByStudentAndElectionAndPosition(Student student, Election election, Position position);

}
