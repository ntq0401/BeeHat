package com.beehat.controller.test;

import com.beehat.entity.Product;
import com.beehat.entity.ProductDetail;
import com.beehat.repository.ProductDetailRepo;
import com.beehat.repository.ProductRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.List;

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
    @GetMapping("/detail")
    public String detail() {
        return "test-theme/product-details";
    }
    @GetMapping("/shop")
    public String shop(Model model) {
        List<Product> products = productRepo.findAll();
        model.addAttribute("products", products);
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
}
