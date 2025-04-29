package com.easyterview.wingterview;

import org.springframework.boot.SpringApplication;

public class TestWingterviewApplication {

	public static void main(String[] args) {
		SpringApplication.from(WingterviewApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
