package com.easyterview.wingterview.board.dto.res;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Feedback {
    private Integer score;

    private String goodPoints;

    private String improvements;

    private String details;
}
