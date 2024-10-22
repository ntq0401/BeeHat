package com.beehat.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/banhang")
public class BanHangController {
    @GetMapping
    public String store() {
        return "admin/banhang";
    }
}