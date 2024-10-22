package com.beehat.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/voucher")
public class VoucherController {
    @GetMapping
    public String voucher() {
        return "admin/voucher";
    }
}
