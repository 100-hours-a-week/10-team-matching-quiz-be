package com.easyterview.wingterview.auth.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JwtUtil {

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


    // 토큰 만료 확인
    public boolean isTokenExpired(String accessToken) {
        Claims claims = parseToken(accessToken);
        return claims.getExpiration().before(new java.util.Date());
    }

    // JWT 파싱 메서드
    private Claims parseToken(String token) {
        return Jwts.parser()
                .build().parseUnsecuredClaims(token.split("\\.")[0] + "." + token.split("\\.")[1] + ".").getPayload();
    }
}
