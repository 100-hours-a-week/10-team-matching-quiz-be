package com.easyterview.wingterview.user.controller;

import com.easyterview.wingterview.common.constants.UserResponseMessage;
import com.easyterview.wingterview.global.response.BaseResponse;
import com.easyterview.wingterview.user.dto.request.UserBasicInfoDto;
import com.easyterview.wingterview.user.dto.response.*;
import com.easyterview.wingterview.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@SecurityRequirement(name = "BearerAuth")
@Slf4j
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Tag(name = "User API", description = "사용자 정보, 좌석 정보 관련 API")
public class UserController {

    private final UserService userService;

    @Operation(summary = "사용자 기본 정보 저장", description = "닉네임, 희망 직무 등 사용자 정보를 저장합니다.")
    @PutMapping
    public ResponseEntity<BaseResponse> saveUserInfo(@Valid @RequestBody UserBasicInfoDto userBasicInfo){
        userService.saveUserInfo(userBasicInfo);
        return BaseResponse.response(UserResponseMessage.USER_INFO_SAVE_DONE);
    }

    @Operation(summary = "전체 좌석 정보 조회", description = "전체 좌석의 사용 가능 여부를 조회합니다.")
    @GetMapping("/seats")
    public ResponseEntity<BaseResponse> getSeatInfo(){
        SeatPositionDto response = userService.getBlockedSeats();
        return BaseResponse.response(UserResponseMessage.SEAT_FETCH_DONE,response);
    }

    @Operation(summary = "좌석 블록", description = "해당 좌석을 블록 처리하여 예약합니다.")
    @PutMapping("/seats/{seatPositionId}")
    public ResponseEntity<BaseResponse> blockSeatPosition(@PathVariable String seatPositionId){
        userService.blockSeatPosition(seatPositionId);
        return BaseResponse.response(UserResponseMessage.SEAT_BLOCK_DONE);
    }

    @Operation(summary = "좌석 블록 여부 확인", description = "해당 좌석이 사용 중인지 확인합니다.")
    @GetMapping("/seats/{seatPositionId}")
    public ResponseEntity<BaseResponse> checkSeatBlocked(@PathVariable String seatPositionId){
        CheckSeatDto response = userService.checkSeatBlocked(seatPositionId);
        return BaseResponse.response(UserResponseMessage.SEAT_CHECK_DONE,response);
    }

    @Operation(summary = "내 정보 조회", description = "현재 로그인된 사용자의 정보를 조회합니다.")
    @GetMapping("/me")
    public ResponseEntity<BaseResponse> getMyInfo(){
        UserInfoDto response = userService.getMyInfo();
        return BaseResponse.response(UserResponseMessage.USER_INFO_FETCH_DONE, response);
    }

    @GetMapping("/{userId}/interview")
    public ResponseEntity<BaseResponse> getInterviewList(@PathVariable String userId, @RequestParam(required = false) String cursor, @RequestParam(defaultValue = "5") Integer limit){
        InterviewHistoryDto response = userService.getInterviewList(userId, cursor, limit);
        return BaseResponse.response(UserResponseMessage.USER_INTERVIEW_HISTORY_FETCH_DONE, response);
    }

    @GetMapping("/{userId}/interview/{interviewId}")
    public ResponseEntity<BaseResponse> getInterviewDetail(@PathVariable String userId, @PathVariable String interviewId){
        InterviewDetailDto response = userService.getInterviewDetail(userId, interviewId);
        return BaseResponse.response(UserResponseMessage.USER_INTERVIEW_DETAIL_FETCH_DONE, response);
    }
}
