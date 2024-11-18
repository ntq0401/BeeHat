package com.beehat.controller.admin;

import com.beehat.entity.CartDetail;
import com.beehat.entity.Customer;
import com.beehat.entity.Employee;
import com.beehat.repository.CartDetailRepo;
import com.beehat.repository.CustomerRepo;
import com.beehat.repository.EmployeeRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

@ControllerAdvice
public class UsernameControllerAdvice {
    @Autowired
    EmployeeRepo employeeRepo;
    @Autowired
    CustomerRepo customerRepo;
    @Autowired
    CartDetailRepo cartDetailRepo;
    @ModelAttribute("username")
    public String getUserName(){
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        } else {
            return principal.toString();
        }
    }
    @ModelAttribute("userImageURL")
    public String addUserImageURLToModel() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            // Lấy Employee từ username
            Employee employee = employeeRepo.findByUsername(username);
            // Kiểm tra xem employee có tồn tại và có ảnh đại diện không
            if (employee != null) {
                String userImageURL = "/images/employees/" + employee.getPhoto();
                // Trả về URL ảnh đại diện nếu có, ngược lại trả về ảnh mặc định
                return (userImageURL != null && !userImageURL.isEmpty()) ? userImageURL : "/account.png";
            }
        }
        return "/account.png"; // Ảnh mặc định cho trường hợp không đăng nhập
    }
}
