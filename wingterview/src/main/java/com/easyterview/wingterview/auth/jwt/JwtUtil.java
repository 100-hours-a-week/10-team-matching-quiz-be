package com.easyterview.wingterview.auth.jwt;

import com.easyterview.wingterview.user.entity.UserEntity;
import com.easyterview.wingterview.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtUtil {

    private final RestClient restClient;
    private final UserRepository userRepository;

    @Value("${oauth.kakao.token-info-uri}")
    private String kakaoTokenInfoUri;

    @Value("${oauth.kakao.token-request-uri}")
    private String kakaoTokenUri;

    @Value("${oauth.kakao.client-id}")
    private String kakaoClientId;

    // Kakao에서 받은 id_token을 디코드하고 이메일을 추출
    // TODO : 진짜 보안 적용하려면?
    // 나중엔 꼭 nimbus-jose-jwt 같은 라이브러리로 Kakao의 공개키 (https://kauth.kakao.com/.well-known/jwks.json)를 사용해서 서명 검증도 해줘야 해.
    public String extractEmail(String idToken) {
        try {
            String[] parts = idToken.split("\\.");
            if (parts.length < 2) throw new IllegalArgumentException("Invalid JWT");

            String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]));
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> payload = mapper.readValue(payloadJson, Map.class);

            return (String) payload.get("email");
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse id_token", e);
        }
    }

    public String extractSub(String idToken) {
        try {
            String[] parts = idToken.split("\\.");
            if (parts.length < 2) throw new IllegalArgumentException("Invalid JWT");

            String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]));
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> payload = mapper.readValue(payloadJson, Map.class);

            return (String) payload.get("sub");
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse id_token", e);
        }
    }


    // access_token 재발급
    public Map<String, Object> refreshAccessToken(String refreshToken) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "refresh_token");
        formData.add("client_id", kakaoClientId);
        formData.add("refresh_token", refreshToken);

        return restClient.post()
                .uri(kakaoTokenUri)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(formData)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    // JWT 파싱 메서드
    private Claims parseToken(String token) {
        return Jwts.parser()
                .build().parseUnsecuredClaims(token.split("\\.")[0] + "." + token.split("\\.")[1] + ".").getPayload();
    }

    public Authentication getAuthentication(String accessToken) {
        String userId = getUserIdFromKakao(accessToken);  // Kakao API 호출

        Optional<UserEntity> user = userRepository.findByProviderAndProviderId("KAKAO",userId);
        if (user.isEmpty()) {
            throw new RuntimeException("인증된 사용자가 존재하지 않습니다.");
        }

        return new UsernamePasswordAuthenticationToken(
                user.get().getId(),
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }

    // 토큰 정보를 가져오는 공통 메서드
    private Map<String, Object> requestTokenInfo(String accessToken) {
        return restClient.get()
                .uri(kakaoTokenInfoUri)
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    // access_token이 유효한지 확인
    public boolean isTokenValid(String accessToken) {
        System.out.println(accessToken);
        try {
            requestTokenInfo(accessToken);  // 유효하지 않으면 예외 발생
            return true;
        } catch (Exception e) {
            log.warn("토큰이 유효하지 않음: {}", e.getMessage());
            return false;
        }
    }

    // Kakao access_token으로부터 providerId(=Kakao user id)를 가져옴
    private String getUserIdFromKakao(String accessToken) {
        try {
            Map<String, Object> response = requestTokenInfo(accessToken);
            return String.valueOf(response.get("id")); // Kakao user ID는 Long인데 String으로 변환
        } catch (Exception e) {
            throw new RuntimeException("카카오 사용자 정보 조회 실패", e);
        }
    }

}
