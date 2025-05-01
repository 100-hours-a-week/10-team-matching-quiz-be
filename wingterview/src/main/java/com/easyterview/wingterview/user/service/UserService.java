package com.easyterview.wingterview.user.service;

import com.easyterview.wingterview.user.dto.request.UserBasicInfoDto;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;

public interface UserService {
    void saveUserInfo(UserBasicInfoDto userBasicInfo);
}
