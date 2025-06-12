package com.easyterview.wingterview.user.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class InterviewHistoryDto {
    private List<InterviewItem> history;
    private Boolean hasNext;
    private String nextCursor;
}
