package com.easyterview.wingterview.common.util.mapper.dto;

import com.easyterview.wingterview.interview.dto.response.Partner;
import com.easyterview.wingterview.user.entity.UserEntity;

public class PartnerMapper {
    public static Partner of(UserEntity partnerEntity){
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
