package com.beehat.controller.customer;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
//@RequestMapping("/customer")
public class CustomerLoginController {
//    @GetMapping("/index")
//    public String index(Authentication authentication, Model model){
//        // Kiểm tra xem người dùng đã đăng nhập hay chưa
//        if (authentication != null && authentication.isAuthenticated()) {
//            // Lấy thông tin người dùng từ SecurityContext
//            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
//            String username = userDetails.getUsername();
//            model.addAttribute("username1", username); // Thêm tên người dùng vào mô hình
//        }
//        return "customer/index";
//    }
//    @GetMapping("/login")
//    public String showLoginPage(Authentication authentication) {
//        // Kiểm tra xem người dùng đã đăng nhập hay chưa
//        if (authentication != null && authentication.isAuthenticated()) {
//            // Nếu đã đăng nhập, chuyển hướng về trang index của khách hàng
//            return "redirect:/customer/index";
//        }
//        // Nếu chưa đăng nhập, hiển thị trang đăng nhập
//        return "customer/customerLogin"; // Trả về tên template đăng nhập
//    }
}
