package com.beehat.controller.test;

import com.beehat.DTO.ProductDTO;
import com.beehat.entity.*;
import com.beehat.repository.CartDetailRepo;
import com.beehat.repository.CustomerRepo;
import com.beehat.repository.ProductDetailRepo;
import com.beehat.repository.ProductRepo;
import com.beehat.service.CartService;
import com.beehat.service.CurrencyUtil;
import jakarta.servlet.http.HttpSession;
import org.bouncycastle.est.CACertsResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class ThemeTestController {
    @Autowired
    ProductRepo productRepo;
    @Autowired
    ProductDetailRepo productDetailRepo;
    @Autowired
    CartService cartService;
    @Autowired
    private CartDetailRepo cartDetailRepo;
    @Autowired
    private CustomerRepo customerRepo;
//    @ModelAttribute("cartSum")
//    public int getSum(Principal principal) {
//        if (principal == null) {
//            // Handle null principal (e.g., user not logged in)
//            return 0;
//        }
//
//        Customer customer = customerRepo.findByUsername(principal.getName());
//        if (customer == null) {
//            // Handle case where customer is not found
//            return 0;
//        }
//
//        List<CartDetail> cartDetails = cartDetailRepo.findByCustomerId(customer.getId());
//        if (cartDetails == null || cartDetails.isEmpty()) {
//            // Handle case where no cart details are found
//            return 0;
//        }
//
//        // Sum up quantities from cart details
//        int sum = cartDetails.stream().mapToInt(CartDetail::getQuantity).sum();
//        System.out.println(sum);
//        return sum;
//    }
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
    public String shopCart(Principal principal, Model model) {
        Customer customer = customerRepo.findByUsername(principal.getName());
        model.addAttribute("cartDetail", cartDetailRepo.findByCustomerId(customer.getId()));
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
    @PostMapping("/add-product-to-cart/{id}")
    public String addProductToCart(@PathVariable Integer id, @RequestParam("username") String username, Model model) {
        ProductDetail productDetail = productDetailRepo.findById(id).orElse(null);
        Customer customer = customerRepo.findByUsername(username);
        List<CartDetail> cartDetail = cartDetailRepo.findByCustomerId(customer.getId());
        boolean isNone = true;
        for (CartDetail cart : cartDetail) {
            if (productDetail.getId() == cart.getProductDetail().getId()) {
                isNone = false;
                cart.setQuantity(cart.getQuantity() + 1);
                cartDetailRepo.save(cart);
            }
        }
        if (isNone) {
            CartDetail cartDetail1 = new CartDetail();
            cartDetail1.setProductDetail(productDetail);
            cartDetail1.setCustomer(customer);
            cartDetailRepo.save(cartDetail1);
        }
        return "redirect:/shop";
    }
    @GetMapping("/getProductPrice")
    @ResponseBody
    public String getProductPrice(@RequestParam Integer product, @RequestParam Integer color, @RequestParam Integer size) {
        try {
            ProductDetail productDetail = productDetailRepo.findByProductIdAndColorIdAndSizeId(product,color, size);
            if (productDetail != null) {
                return CurrencyUtil.formatCurrency(productDetail.getPrice());
            } else {
                throw new RuntimeException("ProductDetail not found for color: " + color + " and size: " + size);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "-1";  // Trả về mã lỗi nếu có lỗi xảy ra
        }
    }

}
