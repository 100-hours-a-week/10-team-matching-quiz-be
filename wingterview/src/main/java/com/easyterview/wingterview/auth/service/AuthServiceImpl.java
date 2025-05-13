package com.easyterview.wingterview.auth.service;

import com.easyterview.wingterview.auth.dto.response.AuthResponseDto;
import com.easyterview.wingterview.auth.dto.response.RefreshResponseDto;
import com.easyterview.wingterview.auth.jwt.JwtUtil;
import com.easyterview.wingterview.user.entity.RefreshTokenEntity;
import com.easyterview.wingterview.user.entity.UserEntity;
import com.easyterview.wingterview.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final RestClient restClient;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    @Value("${oauth.kakao.token-request-uri}")
    private String kakaoTokenUri;

    @Value("${oauth.kakao.client-id}")
    private String kakaoClientId;

    @Value("${oauth.kakao.redirect-uri}")
    private String kakaoRedirectUri;

    @Override
    public Map<String, Object> getAccessToken(String authorizationCode) {

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "authorization_code");
        formData.add("client_id", kakaoClientId);
        formData.add("redirect_uri", kakaoRedirectUri);
        formData.add("code", authorizationCode);

        Map<String, Object> response = restClient.post()
                .uri(kakaoTokenUri)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(formData)
                .retrieve()
                .body(new ParameterizedTypeReference<Map<String, Object>>() {
                });

        return response;
    }

    @Transactional
    @Override
    public AuthResponseDto getOrCreateUserByToken(Map<String, Object> tokenResponse) {
        String idToken = (String) tokenResponse.get("id_token");
        String email = jwtUtil.extractEmail(idToken);
        String providerId = jwtUtil.extractSub(idToken);
        String refreshTokenStr = (String) tokenResponse.get("refresh_token");

        // TODO : KAKAO provider enum으로 관리
        Optional<UserEntity> user = userRepository.findByProviderAndProviderId("KAKAO", providerId);
        boolean isNewUser;

        if (user.isEmpty() || user.get().getCurriculum().equals("temp")) {
            // 새 유저 + 리프레시 토큰 생성
            RefreshTokenEntity refreshToken = RefreshTokenEntity.builder()
                    .refreshToken(refreshTokenStr)
                    .build();

            UserEntity newUser = UserEntity.builder()
                    .email(email)
                    .provider("KAKAO")
                    .providerId(providerId)
                    .refreshToken(refreshToken) // 연관관계 설정
                    .build();

            refreshToken.setUser(newUser); // 양방향 연관관계 설정
            userRepository.save(newUser);
            isNewUser = true;
        } else {
            // 기존 유저 → 토큰 갱신
            UserEntity existingUser = user.get();
            RefreshTokenEntity existingToken = existingUser.getRefreshToken(); // 연관관계로부터 직접 접근 가능!

            if (existingToken != null) {
                existingToken.setRefreshToken(refreshTokenStr);
            } else {
                // 만약 null이면 새로 생성해서 연관관계 설정
                RefreshTokenEntity newToken = RefreshTokenEntity.builder()
                        .refreshToken(refreshTokenStr)
                        .user(existingUser)
                        .build();
                existingUser.setRefreshToken(newToken);
            }

            isNewUser = false;
        }

        AuthResponseDto authResponseDto = AuthResponseDto.builder()
                .accessToken((String) tokenResponse.get("access_token"))
                .isNewUser(isNewUser)
                .build();
        log.info(authResponseDto.toString());

        return authResponseDto;
    }

    @Override
    public RefreshResponseDto reissue() {
        UUID userId = (UUID) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));

        String oldRefreshToken = user.getRefreshToken().getRefreshToken();

        Map<String, Object> response = jwtUtil.refreshAccessToken(oldRefreshToken);
        String newRefreshToken = (String) response.getOrDefault("refresh_token", null);

        // 이게 null이면 refreshToken을 재발급받지 않았다는 의미(expire까지 시간이 충분해서)
        if(newRefreshToken != null) {
            user.setRefreshToken(
                    RefreshTokenEntity.builder()
                            .refreshToken(newRefreshToken)
                            .user(user)
                            .build()
            );
        }

        return RefreshResponseDto.builder()
                .accessToken((String) response.get("access_token"))
                .build();
    }
}
