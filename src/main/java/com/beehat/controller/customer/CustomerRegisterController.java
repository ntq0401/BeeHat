package com.beehat.controller.customer;

import com.beehat.entity.Customer;
import com.beehat.repository.CustomerRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;

@Controller
@RequestMapping("/customer/register")
public class CustomerRegisterController {
    @Autowired
    CustomerRepo customerRepo;
    @Autowired
    PasswordEncoder passwordEncoder;
    @GetMapping
    public String register(Model model){
        model.addAttribute("customer", new Customer());
        return "/customer/register";
    }
    @PostMapping("/add")
    public String registerCustomer(@ModelAttribute("customer") Customer customer, BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        boolean usernameExists = customerRepo.existsByUsername(customer.getUsername());
        boolean emailExists = customerRepo.existsByEmail(customer.getEmail());
        boolean phoneExists = customerRepo.existsByPhone(customer.getPhone());

        if (usernameExists) {
            result.rejectValue("username", "error.customer", "Username đã tồn tại");
        }

        if (emailExists) {
            result.rejectValue("email", "error.customer", "Email đã tồn tại");
        }

        if (phoneExists) {
            result.rejectValue("phone", "error.customer", "Số điện thoại đã tồn tại");
        }

        if (result.hasErrors()) {
            return "customer/register"; // Trả về trang đăng ký với thông báo lỗi
        }

        customer.setCreatedDate(LocalDateTime.now());
        customer.setStatus(Byte.valueOf("1"));
        customer.setPassword(passwordEncoder.encode(customer.getPassword()));
        customerRepo.save(customer);
        // Thêm thông báo thành công
        redirectAttributes.addFlashAttribute("successMessage", "Đăng ký thành công! Vui lòng đăng nhập.");


        return "redirect:/customer/login";
    }
}
