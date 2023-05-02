package com.example.wish.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.Collections;

@Configuration
@EnableSwagger2
public class SwaggerConfig {

    @Value("${server.servlet.context-path}")
    private String contextPath;

//    @Bean
//    public Docket api() {
//        return new Docket(DocumentationType.SWAGGER_2)
//                .select()
//                .apis(RequestHandlerSelectors.basePackage("com.example.wish"))
//                .paths(PathSelectors.any())
//                .build();
//    }

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.example.wish"))
                .paths(PathSelectors.any())
                .build()
                .pathMapping(contextPath)
               .apiInfo(apiInfo());
    }

    private ApiInfo apiInfo() {
        // Set your API information (title, description, version, etc.) here
        return new ApiInfo("Your API", "API description", "API version", "", null, "", "", Collections.emptyList());
    }

}
