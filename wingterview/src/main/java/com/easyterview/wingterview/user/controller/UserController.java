package com.easyterview.wingterview.user.controller;

import com.easyterview.wingterview.common.constants.UserResponseMessage;
import com.easyterview.wingterview.global.response.ApiResponse;
import com.easyterview.wingterview.user.dto.request.UserBasicInfoDto;
import com.easyterview.wingterview.user.dto.response.CheckSeatDto;
import com.easyterview.wingterview.user.dto.response.SeatPositionDto;
import com.easyterview.wingterview.user.dto.response.UserInfoDto;
import com.easyterview.wingterview.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PutMapping
    public ResponseEntity<ApiResponse> saveUserInfo(@Valid @RequestBody UserBasicInfoDto userBasicInfo){
        userService.saveUserInfo(userBasicInfo);
        return ApiResponse.response(UserResponseMessage.USER_INFO_SAVE_DONE);
    }

    @GetMapping("/seats")
    public ResponseEntity<ApiResponse> getSeatInfo(){
        SeatPositionDto response = userService.getBlockedSeats();
        return ApiResponse.response(UserResponseMessage.SEAT_FETCH_DONE,response);
    }

    @PutMapping("/seats/{seatPositionId}")
    public ResponseEntity<ApiResponse> blockSeatPosition(@PathVariable String seatPositionId){
        userService.blockSeatPosition(seatPositionId);
        return ApiResponse.response(UserResponseMessage.SEAT_BLOCK_DONE);
    }

    @GetMapping("/seats/{seatPositionId}")
    public ResponseEntity<ApiResponse> checkSeatBlocked(@PathVariable String seatPositionId){
        CheckSeatDto response = userService.checkSeatBlocked(seatPositionId);
        return ApiResponse.response(UserResponseMessage.SEAT_CHECK_DONE,response);
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse> getMyInfo(){
        UserInfoDto response = userService.getMyInfo();
        log.info(response.toString());
        return ApiResponse.response(UserResponseMessage.USER_INFO_FETCH_DONE, response);
    }

//    @GetMapping("/test")
//    public ResponseEntity<?> test() {
//        var auth = SecurityContextHolder.getContext().getAuthentication();
//        return ResponseEntity.ok("ok");
//    }


}
