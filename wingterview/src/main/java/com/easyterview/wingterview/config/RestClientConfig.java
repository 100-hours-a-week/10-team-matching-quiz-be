package com.easyterview.wingterview.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestClient;

import java.util.List;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient restClient() {
        // ObjectMapper 설정: snake_case → camelCase 자동 변환
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

        // JSON 메시지 컨버터 설정
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(objectMapper);

        // RestClient 빌더 반환
        return RestClient.builder()
                .messageConverters(converters -> {
                    converters.add(new FormHttpMessageConverter());  // 🔥 핵심
                    converters.add(new MappingJackson2HttpMessageConverter());
                })
                .build();
    }
}
