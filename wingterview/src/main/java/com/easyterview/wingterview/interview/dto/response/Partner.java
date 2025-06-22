package com.easyterview.wingterview.interview.dto.response;

import com.easyterview.wingterview.user.entity.UserEntity;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class Partner {
    private final String nickname;
    private final String name;
    private final String curriculum;
    private final String profileImageUrl;
    private final List<String> jobInterest;
    private final List<String> techStack;

    public static Partner fromEntity(UserEntity partnerEntity){
        return Partner.builder()
                .name(partnerEntity.getName())
                .nickname(partnerEntity.getNickname())
                .profileImageUrl(partnerEntity.getProfileImageUrl())
                .techStack(partnerEntity.getUserTechStack().stream().map(t -> t.getTechStack().getLabel()).toList())
                .jobInterest(partnerEntity.getUserJobInterest().stream().map(j -> j.getJobInterest().getLabel()).toList())
                .curriculum(partnerEntity.getCurriculum())
                .build();
    }
}
