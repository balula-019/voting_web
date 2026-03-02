package com.yudhassif.election;
import static org.junit.jupiter.api.Assertions.*;

import com.yudhassif.election.entity.Election;
import com.yudhassif.election.entity.ElectionStatus;
import com.yudhassif.election.repository.ElectionRepository;
import com.yudhassif.election.services.ElectionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.time.*;

@SpringBootTest
class ElectionSchedulerTest {

    @Autowired
    private ElectionRepository repo;

    @Autowired
    private ElectionService service;

    @Test
    void testElectionActivationAcrossTime() {
        // 1. Setup an election starting in the future (UTC)
        Instant futureStart = Instant.parse("2026-05-01T10:00:00Z");
        Election e = new Election("Tanzania General Election", futureStart, futureStart.plusSeconds(3600));
        e.setStatus(ElectionStatus.PENDING);
        repo.save(e);

        // 2. Run service at current "real" time (should not activate)
        service.updateElectionStatuses();
        assertEquals(ElectionStatus.PENDING, repo.findById(e.getId()).get().getStatus());

        // 3. "Time Travel" - If you were using a mockable clock,
        // you would set the clock to 2026-05-01T10:01:00Z here.
    }
}
// this will be used in testing automatically