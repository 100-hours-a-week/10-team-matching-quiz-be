package com.easyterview.wingterview.matching.dto.request;

import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@SuperBuilder
public class Interviewer extends InterviewParticipent{
    private final String seatCode;
    private final List<Integer> seatPosition;
}
