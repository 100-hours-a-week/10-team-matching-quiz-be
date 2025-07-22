package com.easyterview.wingterview.interview.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class Feedback {
    @JsonProperty("overall_score")
    private Integer overallScore;

    @JsonProperty("good_points")
    private String goodPoints;

    @JsonProperty("areas_for_improvement")
    private String areasForImprovement;

    @JsonProperty("detailed_analysis")
    private String detailedAnalysis;
}
