package com.easyterview.wingterview.user.service;

import com.easyterview.wingterview.common.util.SeatPositionUtil;
import com.easyterview.wingterview.common.util.UUIDUtil;
import com.easyterview.wingterview.global.exception.InvalidTokenException;
import com.easyterview.wingterview.user.dto.request.UserBasicInfoDto;
import com.easyterview.wingterview.user.entity.UserEntity;
import com.easyterview.wingterview.user.entity.UserJobInterestEntity;
import com.easyterview.wingterview.user.entity.UserTechStackEntity;
import com.easyterview.wingterview.user.enums.JobInterest;
import com.easyterview.wingterview.user.enums.TechStack;
import com.easyterview.wingterview.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public void saveUserInfo(UserBasicInfoDto userBasicInfo) {
        UserEntity user = userRepository.findById(UUIDUtil.getUserIdFromToken())
                .orElseThrow(() -> new InvalidTokenException("잘못된 토큰"));

        user.setName(userBasicInfo.getName());
        user.setNickname(userBasicInfo.getNickName());
        user.setCurriculum(userBasicInfo.getCurriculum());
        user.setProfileImageUrl(userBasicInfo.getProfileImageUrl());
        user.setSeat(SeatPositionUtil.seatPosToInt(userBasicInfo));

        List<UserTechStackEntity> techStacks = userBasicInfo.getTechStack().stream()
                .map(TechStack::from) // 문자열 → enum
                .map(stack -> UserTechStackEntity.builder()
                        .user(user)
                        .techStack(stack)
                        .build())
                .toList();

        List<UserJobInterestEntity> jobInterests = userBasicInfo.getJobInterest().stream()
                .map(JobInterest::from)
                .map(interest -> UserJobInterestEntity.builder()
                        .user(user)
                        .jobInterest(interest)
                        .build())
                .toList();

        // 기존 것들 clear하고 새로 설정
        user.getUserJobInterest().clear();
        user.getUserJobInterest().addAll(jobInterests);

        user.getUserTechStack().clear();
        user.getUserTechStack().addAll(techStacks);

        userRepository.save(user); // cascade 덕분에 연관 Entity도 저장됨

    }


}
