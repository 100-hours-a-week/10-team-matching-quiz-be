package com.easyterview.wingterview.user.service;

import com.easyterview.wingterview.user.dto.request.UserBasicInfoDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    @Override
    public void saveUserInfo(UserBasicInfoDto userBasicInfo) {

        List<String> jobInterests = userBasicInfo.getJobInterest();
        List<String> techStacks = userBasicInfo.getTechStack();
    }
}
