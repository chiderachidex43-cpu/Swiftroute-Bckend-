package com.swiftroute.backend.controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
@RestController
@RequestMapping("/api/customer")

public class CustomerController {
    @GetMapping("/test")
    public String customerTest(Principal principal) {
        return "Customer access granted to: " + principal.getName();
    }

}
