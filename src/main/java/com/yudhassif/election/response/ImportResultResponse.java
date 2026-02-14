package com.yudhassif.election.response;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ImportResultResponse {

    private int totalRows;
    private int imported;
    private int skipped;

}

