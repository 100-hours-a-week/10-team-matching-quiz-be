package com.easyterview.wingterview.interview.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiInterviewInfoDto {
    private String InterviewId;
    private Integer timeRemain;
    private String currentPhase;
    private Boolean isAiInterview;
    private Integer questionIdx;
    private String question;
}
