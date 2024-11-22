package com.beehat.controller.test;

import com.beehat.DTO.ProductDTO;
import com.beehat.entity.*;
import com.beehat.repository.*;
import com.beehat.service.CartService;
import com.beehat.service.CurrencyUtil;
import com.beehat.service.InvoiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    @Autowired
    InvoiceRepo invoiceRepo;
    @Autowired
    InvoiceDetailRepo invoiceDetailRepo;
    @Autowired
    InvoiceService invoiceService;
//    @ModelAttribute("cartSum")
//    public int getSum(Principal principal) {
//        if (principal == null) {
//            // Handle null principal (e.g., user not logged in)
//            return 0;
//        }
//
//        Customer customer = customerRepo.findByUsername(principal.getName());

    /// /        String name = principal.getName();
    /// /        if (name.equals("user")){
    /// /            return 0;
    /// /        }
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
        //kiểm tra xem có đăng nhập hay không
        //nếu có thì lấy giỏ hàng từ db và update giỏ hàng tạm thời để có dữ liệu checkout cho hoá đơn tạm thời
        if (principal != null) {
            Customer customer = customerRepo.findByUsername(principal.getName());
            List<CartDetail> cartDetail = cartDetailRepo.findByCustomerIdAndStatus(customer.getId(), (byte) 1);
            cartService.addAllCart(cartDetail);
            model.addAttribute("cartDetail", cartDetail);
            model.addAttribute("totalPrice", cartDetail.stream().mapToInt(cart -> cart.getQuantity() * cart.getProductDetail().getPrice()).sum());
            return "test-theme/shop-cart";
        } else {
            model.addAttribute("cartDetail", cartService.getCartDetails());
            model.addAttribute("totalPrice", cartService.getCartDetails().stream().mapToInt(cart -> cart.getQuantity() * cart.getProductDetail().getPrice()).sum());
            return "test-theme/shop-cart";
        }

    }

    @GetMapping("/blog")
    public String blog() {
        return "test-theme/blog";
    }

    @GetMapping("/blog-details")
    public String blogDetails() {
        return "test-theme/blog-details";
    }

    @PostMapping("/proceed-to-checkout")
    public String proceedToCheckout(Principal principal) {
        if (principal != null) {
            // Khách hàng đã đăng nhập
            Customer customer = customerRepo.findByUsername(principal.getName());
            cartService.createTemporaryInvoice(customer);
        } else {
            // Khách hàng chưa đăng nhập
            cartService.createTemporaryInvoice(null);
        }
        return "redirect:/checkout";
    }

    @GetMapping("/checkout")
    public String checkout( Model model) {
        Invoice temporaryInvoice = cartService.getTemporaryInvoice();
        if (temporaryInvoice == null) {
            return "redirect:/shop-cart"; // Nếu không có hóa đơn tạm thời, quay lại giỏ hàng
        }
        model.addAttribute("invoice", temporaryInvoice);
        model.addAttribute("cartDetails", cartService.getCartDetails());
        return "test-theme/checkout";
    }

    @GetMapping("/contact")
    public String contact() {
        return "test-theme/contact";
    }
    //tạm thời thêm nhanh ở trang shop
    @PostMapping("/add-product-to-cart/{id}")
    public String addProductToCart(@PathVariable Integer id, @RequestParam(value = "username", defaultValue = "") String username) {
        ProductDetail productDetail = productDetailRepo.findById(id).orElse(null);
        Customer customer = customerRepo.findByUsername(username);
        if (customer != null) {
            List<CartDetail> cartDetail = cartDetailRepo.findByCustomerIdAndStatus(customer.getId(), (byte) 1);
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
        } else {
            cartService.add(id);
            return "redirect:/shop";
        }
    }

    @GetMapping("/getProductPrice")
    @ResponseBody
    public Map<String, Object> getProductPrice(@RequestParam Integer product, @RequestParam Integer color, @RequestParam Integer size) {
        Map<String, Object> response = new HashMap<>();
        try {
            ProductDetail productDetail = productDetailRepo.findByProductIdAndColorIdAndSizeId(product, color, size);
            if (productDetail != null) {
                response.put("id", productDetail.getId());
                response.put("price", CurrencyUtil.formatCurrency(productDetail.getPrice()));
            } else {
                throw new RuntimeException("ProductDetail not found for color: " + color + " and size: " + size);
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.put("id", -1);  // Giá trị id lỗi
            response.put("price", "-1");  // Giá trị price lỗi
        }
        return response;
    }

    @PostMapping("/add-product-detail-to-cart")
    public String addProductDetailToCart(@RequestParam(value = "username", defaultValue = "") String username,
                                         @RequestParam("quantity") Integer quantity,
                                         @RequestParam("productId") Integer id) {
        ProductDetail productDetail = productDetailRepo.findById(id).orElse(null);
        Customer customer = customerRepo.findByUsername(username);
        if (customer != null) {
            List<CartDetail> cartDetail = cartDetailRepo.findByCustomerIdAndStatus(customer.getId(), (byte) 1);
            boolean isNone = true;
            for (CartDetail cart : cartDetail) {
                if (productDetail.getId() == cart.getProductDetail().getId()) {
                    isNone = false;
                    cart.setQuantity(cart.getQuantity() + quantity);
                    cartDetailRepo.save(cart);
                }
            }
            if (isNone) {
                CartDetail cartDetail1 = new CartDetail();
                cartDetail1.setProductDetail(productDetail);
                cartDetail1.setQuantity(quantity);
                cartDetail1.setCustomer(customer);
                cartDetailRepo.save(cartDetail1);
            }
        } else {
            cartService.add(id);
            return "redirect:/shop";
        }
        return "redirect:/shop";
    }

    @PostMapping("/check-out")
    public String checkOut(@RequestParam("countryInv") String countryInv,
                           @RequestParam("addressInv") String addressInv,
                           @RequestParam("cityInv") String cityInv,
                           @RequestParam("districtInv") String districtInv,
                           @RequestParam("wardInv") String wardInv,
                           @RequestParam("phoneInv") String phoneInv)
            {
        Invoice temporaryInvoice = cartService.getTemporaryInvoice();
                temporaryInvoice.setShippingCountry(countryInv);
                temporaryInvoice.setShippingAddress(addressInv);
                temporaryInvoice.setShippingCity(cityInv);
                temporaryInvoice.setShippingDistrict(districtInv);
                temporaryInvoice.setShippingWard(wardInv);
                temporaryInvoice.setPhone(phoneInv);
                if (temporaryInvoice != null) {
                    // Lưu hóa đơn vào cơ sở dữ liệu (logic lưu vào database)
                    Invoice savedInvoice = saveInvoice(temporaryInvoice, cartService.getCartDetails());
                    // Xóa hóa đơn tạm thời và giỏ hàng
                    cartService.clearTemporaryInvoice();
                    return "redirect:/confirmation?invoiceId=" + savedInvoice.getId();
                }
                return "redirect:/cart"; // Quay lại giỏ hàng nếu không có hóa đơn tạm thời
    }
    private Invoice saveInvoice(Invoice invoice, List<CartDetail> cartDetails) {
        // Lưu hóa đơn và các chi tiết hóa đơn vào database
        Invoice savedInvoice = invoiceRepo.save(invoice);
        for (CartDetail cartDetail : cartDetails) {
            InvoiceDetail invoiceDetail = new InvoiceDetail();
            invoiceDetail.setInvoice(savedInvoice);
            invoiceDetail.setProductDetail(cartDetail.getProductDetail());
            invoiceDetail.setQuantity(cartDetail.getQuantity());
            invoiceDetail.setUnitPrice(cartDetail.getProductDetail().getPrice());
            invoiceDetail.setFinalPrice(cartDetail.getProductDetail().getPrice() * cartDetail.getQuantity());
            invoiceDetailRepo.save(invoiceDetail);
        }
        savedInvoice.setStatus((byte) 3);
        invoiceRepo.save(savedInvoice);
        return savedInvoice;
    }
    @GetMapping("/confirmation")
    public String confirmation(@RequestParam("invoiceId") Integer id, Model model) {
        model.addAttribute("invoice", invoiceRepo.findById(id).orElse(null));
        return "test-theme/confirmation";
    }
}
