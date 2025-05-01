package com.easyterview.wingterview.global.security.filter;

import com.easyterview.wingterview.auth.jwt.JwtUtil;
import com.easyterview.wingterview.global.security.entrypoint.JwtAuthenticationEntryPoint;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;


    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String jwt = request.getHeader("Authorization");

        if (jwt != null && jwt.startsWith("Bearer ")) {
            jwt = jwt.substring(7);
            try {
                if (jwtUtil.isTokenValid(jwt)) {
                    // 🔹 JWT에서 이메일 & 인증 객체 생성
                    Authentication authentication = jwtUtil.getAuthentication(jwt);

                    // 🔹 SecurityContextHolder에 인증 정보 저장
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                }
            }
            // TODO : AuthenticationEntryPoint를 사용하는 방법 모색
            catch (AuthenticationException e){
                jwtAuthenticationEntryPoint.commence(request,response,e);
            }
        }

        filterChain.doFilter(request, response);
    }
}