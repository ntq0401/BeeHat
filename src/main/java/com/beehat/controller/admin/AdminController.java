package com.beehat.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;


@Controller
@RequestMapping("/admin")
public class AdminController {

    @GetMapping("/index")
    public String index() {
        return "admin/index";
    }
    @GetMapping("/login")
    public String showLoginPage() {
        return "admin/loginadmin";
    }

}
