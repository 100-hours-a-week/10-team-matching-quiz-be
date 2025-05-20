package com.easyterview.wingterview;

import com.easyterview.wingterview.config.properties.RabbitMqProperties;
import com.easyterview.wingterview.config.properties.SpringRabbitMqProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({
		SpringRabbitMqProperties.class,
		RabbitMqProperties.class
})// 👈 이게 바인딩을 활성화
public class WingterviewApplication {

	public static void main(String[] args) {

		SpringApplication.run(WingterviewApplication.class, args);
	}

}
