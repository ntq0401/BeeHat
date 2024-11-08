package com.beehat.controller.customer;

import com.beehat.entity.Customer;
import com.beehat.entity.Product;
import com.beehat.repository.CustomerRepo;
import com.beehat.repository.ProductRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
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
import java.util.List;

@Controller
@RequestMapping("/customer")
public class CustomerRegisterController {
    @Autowired
    CustomerRepo customerRepo;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    private ProductRepo productRepo;
    @GetMapping("/index")
    public String index(Authentication authentication, Model model){
        List<Product> products = productRepo.findAll();
        model.addAttribute("products",products);
        // Kiểm tra xem người dùng đã đăng nhập hay chưa
        if (authentication != null && authentication.isAuthenticated()) {
            // Lấy thông tin người dùng từ SecurityContext
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String username = userDetails.getUsername();
            System.out.println(username);
            model.addAttribute("username1", username); // Thêm tên người dùng vào mô hình
        }
        return "customer/index";
    }
    @GetMapping("/login")
    public String showLoginPage(Authentication authentication,Model model) {
        model.addAttribute("customer", new Customer());
        // Kiểm tra xem người dùng đã đăng nhập hay chưa
        if (authentication != null && authentication.isAuthenticated()) {
            // Nếu đã đăng nhập, chuyển hướng về trang index của khách hàng
            return "redirect:/home/index";
        }
        // Nếu chưa đăng nhập, hiển thị trang đăng nhập
        return "customer/customerLogin"; // Trả về tên template đăng nhập
    }

    @PostMapping("/register")
    public String registerCustomer(@ModelAttribute("customer") Customer customer,
                                   BindingResult result,
                                   Model model,
                                   RedirectAttributes redirectAttributes) {
        boolean usernameExists = customerRepo.existsByUsername(customer.getUsername());
        boolean emailExists = customerRepo.existsByEmail(customer.getEmail());

        if (usernameExists) {
            result.rejectValue("username", "error.customer", "Username đã tồn tại");
        }

        if (emailExists) {
            result.rejectValue("email", "error.customer", "Email đã tồn tại");
        }

        if (result.hasErrors()) {
            return "customer/customerLogin";
        }

        customer.setCreatedDate(LocalDateTime.now());
        customer.setStatus(Byte.valueOf("1"));
        customer.setPassword(passwordEncoder.encode(customer.getPassword()));
        customerRepo.save(customer);
        redirectAttributes.addFlashAttribute("successMessage", "Đăng ký thành công! Vui lòng đăng nhập.");

        return "redirect:/customer/login";
    }
}
