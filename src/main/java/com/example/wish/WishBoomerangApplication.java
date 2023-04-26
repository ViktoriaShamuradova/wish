package com.example.wish;

import org.modelmapper.ModelMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
//if need to run something before main method -
//@Bean CommandLineRunner
@SpringBootApplication
public class WishBoomerangApplication {

	public static void main(String[] args) {
		SpringApplication.run(WishBoomerangApplication.class, args);
	}

	@Bean
	public ModelMapper modelMapper() {
		return new ModelMapper();
	}
}
