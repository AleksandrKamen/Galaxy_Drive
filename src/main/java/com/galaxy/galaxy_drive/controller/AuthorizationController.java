package com.galaxy.galaxy_drive.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/login")
public class AuthorizationController {
    @GetMapping
    public String loginPage() {
        return "authorization";
    }
}
