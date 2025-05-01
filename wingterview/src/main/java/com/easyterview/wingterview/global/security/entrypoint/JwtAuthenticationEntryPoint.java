package com.easyterview.wingterview.global.security.entrypoint;

import com.easyterview.wingterview.global.security.exception.JwtAuthenticationException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        int statusCode = (authException instanceof JwtAuthenticationException)
                ? ((JwtAuthenticationException) authException).getStatusCode()
                : HttpServletResponse.SC_UNAUTHORIZED;

        response.setStatus(statusCode);
        response.setContentType("application/json;charset=UTF-8");

        String jsonResponse = String.format(
                "{\"message\": \"%s\", \"data\": null}",
                authException.getMessage()
        );

        response.getWriter().write(jsonResponse);
        response.getWriter().flush();
    }
}
