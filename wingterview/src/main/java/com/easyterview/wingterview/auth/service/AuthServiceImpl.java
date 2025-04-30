package com.easyterview.wingterview.auth.service;

import com.easyterview.wingterview.auth.dto.response.AuthResponseDto;
import com.easyterview.wingterview.auth.jwt.JwtUtil;
import com.easyterview.wingterview.user.entity.UserEntity;
import com.easyterview.wingterview.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService{

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
    public Map<String,Object> getAccessToken(String authorizationCode) {

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
                .body(new ParameterizedTypeReference<Map<String, Object>>() {});

        return response;
    }

    @Override
    public AuthResponseDto getOrCreateUserByToken(Map<String, Object> tokenResponse) {
        String idToken = (String) tokenResponse.get("id_token");
        String email = jwtUtil.extractEmail(idToken);
        String providerId = jwtUtil.extractSub(idToken);  // 반환형도 Long이어야 할 듯

        // TODO : KAKAO provider enum으로 관리
        Optional<UserEntity> user = userRepository.findByProviderAndProviderId("KAKAO", providerId);

        UserEntity finalUser;
        if (user.isEmpty()) {
            UserEntity newUser = UserEntity.builder()
                    .email(email)
                    .provider("KAKAO")
                    .providerId(providerId)
                    .build();

            finalUser = userRepository.save(newUser);
        } else {
            finalUser = user.get();
        }

        return AuthResponseDto.builder()
                .accessToken((String) tokenResponse.get("access_token"))
                .refreshToken((String) tokenResponse.get("refresh_token"))
                .userId(finalUser.getId().toString())
                .build();
    }
}
