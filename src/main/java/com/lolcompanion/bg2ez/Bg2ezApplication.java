package com.lolcompanion.bg2ez;

import com.lolcompanion.bg2ez.riot.config.RiotProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(RiotProperties.class)
public class Bg2ezApplication {

	public static void main(String[] args) {
		SpringApplication.run(Bg2ezApplication.class, args);
	}

}
