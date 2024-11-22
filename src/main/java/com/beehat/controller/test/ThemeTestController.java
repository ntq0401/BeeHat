package com.beehat.controller.test;

import com.beehat.DTO.ProductDTO;
import com.beehat.entity.Product;
import com.beehat.entity.ProductDetail;
import com.beehat.repository.ProductDetailRepo;
import com.beehat.repository.ProductRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class ThemeTestController {
    @Autowired
    ProductRepo productRepo;
    @Autowired
    ProductDetailRepo productDetailRepo;
    @GetMapping("/")
    public String home() {
        return "test-theme/index";
    }
    @GetMapping("/detail/{sku}")
    public String detail(@PathVariable String sku, Model model) {
        Product product = productRepo.findBySku(sku);
        ProductDTO productDTO = new ProductDTO(product);
        model.addAttribute("product", productDTO);
        return "test-theme/product-details";
    }
    @GetMapping("/shop")
    public String shop(Model model) {
        // Lấy tất cả sản phẩm từ cơ sở dữ liệu
        List<Product> products = productRepo.findAll();

        // Chuyển đổi sản phẩm thành DTO và nhóm theo từng sản phẩm
        List<ProductDTO> productDTOs = products.stream()
                .map(ProductDTO::new)  // Tạo DTO cho từng sản phẩm
                .collect(Collectors.toList());  // Collect các DTO vào danh sách
        // Truyền danh sách DTO vào model
        model.addAttribute("products", productDTOs);
        return "test-theme/shop";
    }
    @GetMapping("/shop-cart")
    public String shopCart() {
        return "test-theme/shop-cart";
    }
    @GetMapping("/blog")
    public String blog() {
        return "test-theme/blog";
    }
    @GetMapping("/blog-details")
    public String blogDetails() {
        return "test-theme/blog-details";
    }
    @GetMapping("/checkout")
    public String checkout() {
        return "test-theme/checkout";
    }
    @GetMapping("/contact")
    public String contact() {
        return "test-theme/contact";
    }
    @GetMapping("/account")
    public String account() {
        return "test-theme/account";
    }
    @GetMapping("/order")
    public String order() {
        return "test-theme/order";
    }
    @GetMapping("/orderdetail")
    public String orderdetail() {
        return "test-theme/orderdetail";
    }
}
