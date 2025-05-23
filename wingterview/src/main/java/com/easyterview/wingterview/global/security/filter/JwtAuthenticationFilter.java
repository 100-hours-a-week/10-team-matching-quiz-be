package com.easyterview.wingterview.global.security.filter;

import com.easyterview.wingterview.auth.jwt.JwtUtil;
import com.easyterview.wingterview.global.security.entrypoint.JwtAuthenticationEntryPoint;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
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
                    Authentication authentication = jwtUtil.getAuthentication(jwt);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }

            }
            catch (AuthenticationException e){
                jwtAuthenticationEntryPoint.commence(request,response,e);
            }
        }

        filterChain.doFilter(request, response);
    }
}