package com.thecheatschool.thecheatschool.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;


@SpringBootApplication
@EnableCaching
public class ThecheatschoolserverApplication {

	public static void main(String[] args) {
		SpringApplication.run(ThecheatschoolserverApplication.class, args);
	}

}
