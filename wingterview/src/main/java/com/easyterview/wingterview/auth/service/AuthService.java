package com.easyterview.wingterview.auth.service;

import com.easyterview.wingterview.auth.dto.response.AuthResponseDto;
import com.easyterview.wingterview.auth.dto.response.RefreshResponseDto;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public interface AuthService {
    Map<String,Object> getAccessToken(String code);
    AuthResponseDto getOrCreateUserByToken(Map<String, Object> tokenResponse);
    RefreshResponseDto reissue();
}
