package com.example.Royal_Blueberry;

import com.example.Royal_Blueberry.client.MerriamWebsterClient;
import com.example.Royal_Blueberry.service.impl.EmbeddingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@EnableMongoAuditing
public class RoyalBlueberryApplication {


	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}

	static String profile ;
	public static void main(String[] args) {
		SpringApplication.run(RoyalBlueberryApplication.class, args);
	}

}
