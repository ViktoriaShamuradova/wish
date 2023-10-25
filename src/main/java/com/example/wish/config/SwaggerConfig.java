//package com.example.wish.config;
//
//import com.google.common.base.Predicate;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.servlet.config.annotation.EnableWebMvc;
//import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
//import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
//import springfox.documentation.RequestHandler;
//import springfox.documentation.builders.ApiInfoBuilder;
//import springfox.documentation.builders.PathSelectors;
//import springfox.documentation.builders.RequestHandlerSelectors;
//import springfox.documentation.service.ApiInfo;
//import springfox.documentation.spi.DocumentationType;
//import springfox.documentation.spring.web.plugins.Docket;
//import springfox.documentation.swagger.web.UiConfiguration;
//import springfox.documentation.swagger.web.UiConfigurationBuilder;
//import springfox.documentation.swagger2.annotations.EnableSwagger2;
//
//@Configuration
//@EnableSwagger2
//@EnableWebMvc
//public class SwaggerConfig {
//    @Value("${server.servlet.context-path}")
//    private String contextPath;
//    @Bean
//    public Docket api() {
//        return new Docket(DocumentationType.SWAGGER_2)
//                .select()
//                .apis( RequestHandlerSelectors.basePackage("com.example.wish")) //basePackage("com.example.wish.controller")controller
//                .build()
//                .apiInfo(apiInfo());
//               // .pathMapping(contextPath);
//    }
//
////        @Override
////    public void addResourceHandlers(ResourceHandlerRegistry registry) {
////
////        registry
////                .addResourceHandler("swagger-ui.html")
////                .addResourceLocations("classpath:/META-INF/resources/");
////
////        registry
////                .addResourceHandler("/webjars/**")
////                .addResourceLocations("classpath:/META-INF/resources/webjars/");
////    }
//
//    @Bean
//    public ApiInfo apiInfo() {
//        return new ApiInfoBuilder()
//                .title("Your API Title")
//                .description("Your API Description")
//                .version("1.0.0")
//                .build();
//    }
//
//    @Bean
//    public UiConfiguration uiConfiguration() {
//        return UiConfigurationBuilder.builder()
//                .displayRequestDuration(true)
//                .build();
//    }
//
//}
