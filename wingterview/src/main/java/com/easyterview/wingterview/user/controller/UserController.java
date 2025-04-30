package com.easyterview.wingterview.user.controller;

import com.easyterview.wingterview.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    @PutMapping
    public ResponseEntity<ApiResponse> saveUserInfo(@RequestBody UserBasicInfoDto userBasicInfo){

    }

}
