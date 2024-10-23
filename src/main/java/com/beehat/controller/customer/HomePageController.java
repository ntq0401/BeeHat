package com.beehat.controller.customer;

import com.beehat.repository.ProductDetailRepo;
import com.beehat.repository.ProductImageRepo;
import com.beehat.repository.ProductRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class HomePageController {
    @Autowired
    ProductRepo productRepo;
    @Autowired
    ProductDetailRepo productDetailRepo;
    @Autowired
    ProductImageRepo productImageRepo;

    @GetMapping("/home")
    public String home(Model model) {
        model.addAttribute("products", productRepo.findAll());
        return "customer/index";
    }

    @GetMapping("/product-detail/{id}")
    public String productDetail(@PathVariable int id, Model model) {
        model.addAttribute("products", productRepo.findById(id).orElse(null));
        model.addAttribute("listImg", productImageRepo.findByProductId(id));
        model.addAttribute("productDetail", productDetailRepo.findByProductId(id));
        return "customer/product-detail/index";
    }
    @GetMapping("/cart")
    public String cart(Model model) {
        return "customer/cart";
    }
}
