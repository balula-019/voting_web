package com.yudhassif.election.response;

public record LeadersResponse
        (
                String fullName,
                String positionName,
                Long electionId,
                String imageUrl

                ) {
}
