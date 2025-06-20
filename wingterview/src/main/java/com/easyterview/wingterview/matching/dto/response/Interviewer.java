package com.easyterview.wingterview.matching.dto.response;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class Interviewer extends InterviewParticipant {
    private final String seatCode;
}
