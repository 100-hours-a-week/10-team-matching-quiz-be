package com.easyterview.wingterview.common.util;

import com.easyterview.wingterview.interview.enums.ParticipantRole;
import com.easyterview.wingterview.interview.enums.Phase;

public class InterviewUtil {
    public static InterviewStatus nextPhase(int round, Phase phase, boolean isAiInterview){
        switch (phase.getPhase()) {
            case "feedback" -> {
                if (round == 4) {
                    return InterviewStatus.builder()
                            .phase(Phase.COMPLETE)
                            .round(round)
                            .build();
                } else {
                    return InterviewStatus.builder()
                            .phase(Phase.PENDING)
                            .round(round + 1)
                            .build();
                }
            }
            case "pending" -> {
                return InterviewStatus.builder()
                        .phase(Phase.PROGRESS)
                        .round(round)
                        .build();
            }
            case "progress" -> {
                if (isAiInterview) {
                    return InterviewStatus.builder()
                            .phase(Phase.COMPLETE)
                            .round(round)
                            .build();
                } else {
                    return InterviewStatus.builder()
                            .phase(Phase.FEEDBACK)
                            .round(round)
                            .build();
                }
            }
            default -> {
                return InterviewStatus.builder()
                        .phase(Phase.PENDING)
                        .round(1)
                        .build();
            }
        }
    }

    public static boolean checkInterviewer(ParticipantRole role, Integer round) {
        return (role == ParticipantRole.FIRST_INTERVIEWER) ^ (round % 2 == 0);
    }
}
