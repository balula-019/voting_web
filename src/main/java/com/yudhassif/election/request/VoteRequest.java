package com.yudhassif.election.request;
public record VoteRequest(
        Long LeaderId,
        Long credentialId
) {}
