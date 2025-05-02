package com.easyterview.wingterview.user.service;

import com.easyterview.wingterview.user.dto.request.UserBasicInfoDto;
import com.easyterview.wingterview.user.dto.response.CheckSeatDto;
import com.easyterview.wingterview.user.dto.response.SeatPositionDto;
import com.easyterview.wingterview.user.dto.response.UserInfoDto;

public interface UserService {
    void saveUserInfo(UserBasicInfoDto userBasicInfo);
    SeatPositionDto getBlockedSeats();
    CheckSeatDto checkSeatBlocked(String seatPositionId);

    UserInfoDto getMyInfo();
}
