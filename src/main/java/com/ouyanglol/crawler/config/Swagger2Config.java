package com.ouyanglol.crawler.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.ArrayList;

@Configuration
@EnableSwagger2
public class Swagger2Config {

	
	@Bean
	public Docket docket() {
		return new Docket(DocumentationType.SWAGGER_2)
					 .apiInfo(apiInfo())
					 .select()
					 .build();
					 
					 
	}

	
	private ApiInfo apiInfo() {
		return new ApiInfo(
				"restapi 接口", 
				"restapi 接口",
				"1.0", 
				"",
				null,
				"",
				"",
				new ArrayList<>()
		);
	}
}