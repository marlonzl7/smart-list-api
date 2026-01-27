package com.smartlist.api;

import com.smartlist.api.infra.config.CorsProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@EnableConfigurationProperties(CorsProperties.class)
@SpringBootApplication
public class SmartlistApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(SmartlistApiApplication.class, args);
	}

}
