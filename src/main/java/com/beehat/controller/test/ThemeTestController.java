package com.beehat.controller.test;

import com.beehat.DTO.ProductDTO;
import com.beehat.entity.*;
import com.beehat.repository.*;
import com.beehat.service.CartService;
import com.beehat.service.CurrencyUtil;
import com.beehat.service.InvoiceService;
import com.beehat.service.ProvincesService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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
    @Autowired
    private ProvincesService provincesService;
    @Autowired
    PaymentHistoryRepo paymentHistoryRepo;
    @Autowired
    PaymentMethodRepo paymentMethodRepo;
    @Autowired
    InvoiceHistoryStatusRepo invoiceHistoryStatusRepo;
    @ModelAttribute("cartSum")
    public long getSum(Principal principal) {
        if (principal == null) {
            return cartService.getTotal();
        }

        Customer customer = customerRepo.findByUsername(principal.getName());

        //trường hợp dùng tài khoản admin employ đăng nhập vào thì return -1
        if (customer == null) {
            return -1;
        }

        // trường hợp giỏ hàng của user không có sản phẩm
        List<CartDetail> cartDetails = cartDetailRepo.findByCustomerIdAndStatus(customer.getId(),(byte) 1);
        if (cartDetails == null || cartDetails.isEmpty()) {
            // Handle case where no cart details are found
            return 0;
        }

        // trường hợp có sản phẩm trong giỏ tài khoản user
        long sum = cartDetails.stream().map(CartDetail::getProductDetail).distinct().count();
        return sum;
    }

    @GetMapping("/")
    public String home(HttpServletRequest request, HttpServletResponse response,Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        // Kiểm tra nếu người dùng đã đăng nhập và có vai trò là ADMIN hoặc EMPLOYEE
        if (auth != null && (auth.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"))
                || auth.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_EMPLOYEE")))) {

            // Đăng xuất nếu có quyền ADMIN hoặc EMPLOYEE
            new SecurityContextLogoutHandler().logout(request, response, auth);

            // Sau khi đăng xuất, chuyển hướng về trang login hoặc trang khác nếu cần
            return "redirect:/";  // Điều chỉnh URL chuyển hướng nếu cần
        }
        List<Product> productList = productRepo.findTop12ByOrderByCreatedDateDesc();
        List<Product> productListPromotion = productRepo.findByPromotionIdNotNull();
        model.addAttribute("productListPromotion",productListPromotion);
        model.addAttribute("productList",productList);
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
            List<CartDetail> cartDetail = cartDetailRepo.findByCustomerIdAndStatus(customer.getId(), (byte) 1);
            cartService.createTemporaryInvoice(customer, cartDetail);
        } else {
            // Khách hàng chưa đăng nhập
            cartService.createTemporaryInvoice(null, cartService.getCartDetails());
        }
        return "redirect:/checkout";
    }

    @GetMapping("/checkout")
    public String checkout(Model model,Principal principal) {
        model.addAttribute("provinces", provincesService.getProvinces());
        Invoice temporaryInvoice = cartService.getTemporaryInvoice();
        if (temporaryInvoice == null) {
            return "redirect:/shop-cart"; // Nếu không có hóa đơn tạm thời, quay lại giỏ hàng
        }
        model.addAttribute("invoice", temporaryInvoice);
        if (principal != null) {
            Customer customer = customerRepo.findByUsername(principal.getName());
            model.addAttribute("cartDetails", cartDetailRepo.findByCustomerIdAndStatus(customer.getId(), (byte) 1));
        }else{
            model.addAttribute("cartDetails", cartService.getCartDetails());
        }
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
                cartDetail1.setQuantity(1);
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
                           @RequestParam("phoneInv") String phoneInv,
                           @RequestParam("paymentInv") Integer idPayment) {
        PaymentMethod paymentMethod = paymentMethodRepo.findById(idPayment).orElse(null);
        Invoice temporaryInvoice = cartService.getTemporaryInvoice();
        temporaryInvoice.setShippingCountry(countryInv);
        temporaryInvoice.setShippingAddress(addressInv);
        temporaryInvoice.setShippingCity(cityInv);
        temporaryInvoice.setShippingDistrict(districtInv);
        temporaryInvoice.setShippingWard(wardInv);
        temporaryInvoice.setPhone(phoneInv);
        temporaryInvoice.setPaymentMethod(paymentMethod);
        if (temporaryInvoice != null) {
            // Lưu hóa đơn vào cơ sở dữ liệu (logic lưu vào database)
            Invoice savedInvoice = new Invoice();
            //trường hợp giỏ hàng này có user đã đăng nhập thì phải chuyển trạng thái giỏ hàng thành đã thanh toán
            //còn với giỏ chưa đăng nhập thì dùng clear là xong
            if (temporaryInvoice.getCustomer() != null) {
                List<CartDetail> listCart = cartDetailRepo.findByCustomerIdAndStatus(temporaryInvoice.getCustomer().getId(), (byte) 1);
                 savedInvoice = saveInvoice(temporaryInvoice, listCart);
                for (CartDetail cart : listCart) {
                    cart.setStatus((byte) 2);
                    cartDetailRepo.save(cart);
                }
            }else{
                savedInvoice = saveInvoice(temporaryInvoice,cartService.getCartDetails());
                cartService.clear();
            }
            // Xóa hóa đơn tạm thời và giỏ hàng
            cartService.clearTemporaryInvoice();
            return "redirect:/confirmation?invoiceId=" + savedInvoice.getId();
        }
        return "redirect:/cart"; // Quay lại giỏ hàng nếu không có hóa đơn tạm thời
    }

    private Invoice saveInvoice(Invoice invoice, List<CartDetail> cartDetails) {
        // Lưu hóa đơn và các chi tiết hóa đơn vào database
        String randomUUID = UUID.randomUUID().toString().substring(0, 5).toUpperCase();
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
        InvoiceStatusHistory invoiceStatusHistory = new InvoiceStatusHistory();
        PaymentHistory paymentHistory = new PaymentHistory();
        invoiceStatusHistory.setInvoice(savedInvoice);
        invoiceStatusHistory.setNewStatus((byte) 3);
        invoiceStatusHistory.setUpdatedAt(LocalDateTime.now());
        paymentHistory.setInvoice(savedInvoice);
        paymentHistory.setPaymentDate(LocalDateTime.now());
        paymentHistory.setTransactionCode(randomUUID + "-" + savedInvoice.getInvoiceTrackingNumber());
        paymentHistory.setAmountPaid(savedInvoice.getTotalPrice());
        paymentHistory.setPaymentMethod(savedInvoice.getPaymentMethod());
        paymentHistoryRepo.save(paymentHistory);
        invoiceHistoryStatusRepo.save(invoiceStatusHistory);
        return savedInvoice;
    }

    @GetMapping("/confirmation")
    public String confirmation(@RequestParam("invoiceId") Integer id, Model model) {
        model.addAttribute("invoice", invoiceRepo.findById(id).orElse(null));
        return "test-theme/confirmation";
    }

    @GetMapping("/districts")
    @ResponseBody
    public List<Map<String, Object>> getDistricts(@RequestParam("provinceId") int provinceId) {
        return provincesService.getDistricts(provinceId);
    }

    @GetMapping("/wards")
    @ResponseBody
    public List<Map<String, Object>> getWards(@RequestParam("districtId") int districtId) {
        return provincesService.getWards(districtId);
    }
}
