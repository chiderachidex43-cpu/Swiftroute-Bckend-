package com.swiftroute.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class Hellocontroller {

    @GetMapping("/hello")
    public String hello() {
        return "SwiftRoute Backend is alive 🚚";
    }
}