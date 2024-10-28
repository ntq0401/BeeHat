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
@RequestMapping("/home")
public class HomePageController {
   @GetMapping("/index")
    public String home(Model model) {
       return "online_store/trangchu";
   }
   @GetMapping("/cart")
    public String cart(Model model) {
       return "online_store/giohang";
   }
   @GetMapping("/product-detail")
    public String productDetail(Model model) {
       return "online_store/productdetail";
   }
}
