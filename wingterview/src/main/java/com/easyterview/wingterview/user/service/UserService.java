package com.easyterview.wingterview.user.service;

import com.easyterview.wingterview.user.dto.request.UserBasicInfoDto;
import com.easyterview.wingterview.user.dto.response.*;

public interface UserService {
    void saveUserInfo(UserBasicInfoDto userBasicInfo);
    SeatPositionDto getBlockedSeats();
    CheckSeatDto checkSeatBlocked(String seatPositionId);

    UserInfoDto getMyInfo();

    void blockSeatPosition(String seatPositionId);

    InterviewHistoryDto getInterviewList(String userId, String cursor, Integer limit);

    InterviewDetailDto getInterviewDetail(String userId, String interviewId);
}
