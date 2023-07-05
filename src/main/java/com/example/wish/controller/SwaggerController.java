package com.example.wish.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SwaggerController {

    @GetMapping("/swagger-ui/")
    public String showCustomSwaggerUI() {
        return "custom-swagger.html";
    }

}