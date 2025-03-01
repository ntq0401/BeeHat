package com.beehat.controller.test;

import com.beehat.DTO.ProductDTO;
import com.beehat.DTO.ProductResponse;
import com.beehat.entity.*;
import com.beehat.repository.*;
import com.beehat.service.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
    AddressService addressService;
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
    private ProvincesService provincesService;
    @Autowired
    PaymentHistoryRepo paymentHistoryRepo;
    @Autowired
    PaymentMethodRepo paymentMethodRepo;
    @Autowired
    InvoiceHistoryStatusRepo invoiceHistoryStatusRepo;
    @Autowired
    private VoucherRepo voucherRepo;
    @Autowired
    private CategoryRepo categoryRepo;
    @Autowired
    private BeltRepo beltRepo;
    @Autowired
    private LiningRepo liningRepo;
    @Autowired
    private MaterialRepo materialRepo;
    @Autowired
    private StyleRepo styleRepo;
    @Autowired
    private SizeRepo sizeRepo;
    @Autowired
    private ColorRepo colorRepo;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    private JavaMailSender mailSender;
    @Autowired
    private VNPayService vnPayService;
    @Autowired
    private InvoiceDetailService invoiceDetailService;
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
    @ModelAttribute("listCate")
    List<Category> listCategory() {
        return categoryRepo.findByStatus(Byte.valueOf("1"));
    }
    @ModelAttribute("top3hottrend")
    List<ProductDTO> top3hottrend(Pageable pageable) {
        List<Product> list1 = productRepo.findAll();
        List<ProductDTO> dtoList = list1.stream()
                .map(product -> new ProductDTO(product, product.getTotalStock())) // Giả sử ProductDTO có một constructor nhận totalStock
                .sorted((dto1, dto2) -> Integer.compare(dto2.getTotalStock(), dto1.getTotalStock())) // Sắp xếp theo stock giảm dần
                .limit(3) // Chỉ lấy top 3 sản phẩm
                .collect(Collectors.toList());
        return dtoList;
    }
    @ModelAttribute("top3feature")
    List<ProductDTO> top3feature(Pageable pageable) {
        List<Product> list1 = productRepo.findAll();
        List<ProductDTO> dtoList = list1.stream()
                .map(ProductDTO::new) // Giả sử ProductDTO có một constructor nhận totalStock
                .limit(3) // Chỉ lấy top 3 sản phẩm
                .collect(Collectors.toList());
        return dtoList;
    }
    @ModelAttribute("top3bestsell")
    List<ProductResponse> top3bestsell(){
        return invoiceDetailService.getTop3BestSell();
    }
    @ModelAttribute("listProductShop")
    public Page<ProductDTO> products(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) Integer materialId,
            @RequestParam(required = false) Integer styleId,
            @RequestParam(required = false) Integer liningId,
            @RequestParam(required = false) Integer beltId,
            @RequestParam(required = false) String name,
            Model model) {
        if (page < 0) {
            page = 0; // Đảm bảo phân trang hợp lệ
        }
        Pageable pageable = PageRequest.of(page, 10, Sort.by("createdDate").descending());
        Page<Product> productsPage = productRepo.findByCriteria(
                name,categoryId, materialId, styleId, liningId, beltId, pageable);
        Page<ProductDTO> productDTOs = productsPage.map(ProductDTO::new);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productDTOs.getTotalPages());
        model.addAttribute("totalItems", productDTOs.getTotalElements());
        return productDTOs;
    }

    @ModelAttribute("listBelt")
    List<Belt> listBelt() {
        return beltRepo.findAll();
    }

    @ModelAttribute("listLining")
    List<Lining> listLining() {
        return liningRepo.findAll();
    }

    @ModelAttribute("listStyle")
    List<Style> listStyle() {
        return styleRepo.findAll();
    }

    @ModelAttribute("listMater")
    List<Material> listMaterial() {
        return materialRepo.findAll();
    }

    @ModelAttribute("listColor")
    List<Color> listColor() {
        return colorRepo.findAll();
    }

    @ModelAttribute("listSize")
    List<Size> listSize() {
        return sizeRepo.findAll();
    }
    @ModelAttribute("slmuvanh")
    Integer countmuvanh(){
        Integer count = 0;
        List<Product> list = productRepo.findByCategoryId(1);
        List<ProductDTO> dtoList = list.stream().map(ProductDTO::new).collect(Collectors.toList());
        for (ProductDTO dto : dtoList) {
            count+=dto.getTotalStock();
        }
        return count;
    }
    @ModelAttribute("slmuket")
    Integer countmuket(){
        Integer count = 0;
        List<Product> list = productRepo.findByCategoryId(4);
        List<ProductDTO> dtoList = list.stream().map(ProductDTO::new).collect(Collectors.toList());
        for (ProductDTO dto : dtoList) {
            count+=dto.getTotalStock();
        }
        return count;
    }
    @ModelAttribute("slmujacket")
    Integer countmujacket(){
        Integer count = 0;
        List<Product> list = productRepo.findByCategoryId(6);
        List<ProductDTO> dtoList = list.stream().map(ProductDTO::new).collect(Collectors.toList());
        for (ProductDTO dto : dtoList) {
            count+=dto.getTotalStock();
        }
        return count;
    }
    @ModelAttribute("slmusnapback")
    Integer countmusnapback(){
        Integer count = 0;
        List<Product> list = productRepo.findByCategoryId(7);
        List<ProductDTO> dtoList = list.stream().map(ProductDTO::new).collect(Collectors.toList());
        for (ProductDTO dto : dtoList) {
            count+=dto.getTotalStock();
        }
        return count;
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
        List<ProductDTO> productDTOs = productList.stream()
                .map(ProductDTO::new)  // Tạo DTO cho từng sản phẩm
                .collect(Collectors.toList());  // Collect các DTO vào danh sách
        model.addAttribute("productList",productDTOs);
        return "test-theme/index";
    }

    @GetMapping("/detail/{sku}")
    public String detail(@PathVariable String sku, Model model) {
        Product product = productRepo.findBySku(sku);
        ProductDTO productDTO = new ProductDTO(product);
        model.addAttribute("product", productDTO);
        Pageable pageable = PageRequest.of(0, 4);
        List<Product> productList = productRepo.findTop4(pageable);
        model.addAttribute("productList",productList);
        return "test-theme/product-details";
    }

    @GetMapping("/shop")
    public String shop() {
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
            int totalPrice = cartDetail.stream().mapToInt(cart -> cart.getProductDetail().getPrice() * cart.getQuantity()).sum();
            int finalPrice = cartDetail.stream().mapToInt(cart -> {
                int price = cart.getProductDetail().getPrice();
                if (cart.getProductDetail().getProduct().getPromotion() != null) {
                    Promotion promotion = cart.getProductDetail().getProduct().getPromotion();

                    if (promotion.getDiscountPercentage() != null && promotion.getDiscountPercentage() != 0) {
                        // Giảm giá theo tỷ lệ phần trăm
                        int discountAmount = (promotion.getDiscountPercentage() * price) / 100;
                        price = price - discountAmount;
                    } else if (promotion.getDiscountAmount() != null && promotion.getDiscountAmount() != 0) {
                        // Giảm giá theo số tiền cố định
                        price = price - promotion.getDiscountAmount();
                    }

                    // Đảm bảo giá không âm
                    if (price < 0) price = 0;
                }
                return cart.getQuantity() * price;
            }).sum();
            model.addAttribute("totalPrice", totalPrice);
            model.addAttribute("finalPrice", finalPrice);
            return "test-theme/shop-cart";
        } else {
            List<CartDetail> cartDetails = cartService.getCartDetails();
            for (CartDetail cartDetail : cartDetails) {
                ProductDetail latestProductDetail = productDetailRepo.findById(cartDetail.getProductDetail().getId()).orElse(null);
                if (latestProductDetail != null) {
                    cartDetail.setProductDetail(latestProductDetail);
                }
            }
            model.addAttribute("cartDetail", cartService.getCartDetails());
            int totalPrice = cartDetails.stream().mapToInt(cart -> cart.getProductDetail().getPrice() * cart.getQuantity()).sum();
            int finalPrice = cartDetails.stream().mapToInt(cart -> {
                int price = cart.getProductDetail().getPrice();
                if (cart.getProductDetail().getProduct().getPromotion() != null) {
                    Promotion promotion = cart.getProductDetail().getProduct().getPromotion();

                    if (promotion.getDiscountPercentage() != null && promotion.getDiscountPercentage() != 0) {
                        // Giảm giá theo tỷ lệ phần trăm
                        int discountAmount = (promotion.getDiscountPercentage() * price) / 100;
                        price = price - discountAmount;
                    } else if (promotion.getDiscountAmount() != null && promotion.getDiscountAmount() != 0) {
                        // Giảm giá theo số tiền cố định
                        price = price - promotion.getDiscountAmount();
                    }

                    // Đảm bảo giá không âm
                    if (price < 0) price = 0;
                }
                return cart.getQuantity() * price;
            }).sum();
            model.addAttribute("totalPrice", totalPrice)
            ;model.addAttribute("finalPrice", finalPrice);
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
    public String proceedToCheckout(Principal principal,RedirectAttributes redirectAttributes) {
        if (principal != null) {
            // Khách hàng đã đăng nhập
            Customer customer = customerRepo.findByUsername(principal.getName());
            List<CartDetail> cartDetail = cartDetailRepo.findByCustomerIdAndStatus(customer.getId(), (byte) 1);
            String productError = checkProductAvailability(cartDetail);
            if (productError != null) {
                redirectAttributes.addFlashAttribute("error", productError);
                return "redirect:/shop-cart";
            }
            cartService.createTemporaryInvoice(customer, cartDetail);
        } else {
            List<CartDetail> cartDetails = cartService.getCartDetails();
            String productError = checkProductAvailability(cartDetails);
            if (productError != null) {
                redirectAttributes.addFlashAttribute("error", productError);
                return "redirect:/shop-cart";
            }
            // Khách hàng chưa đăng nhập
            cartService.createTemporaryInvoice(null, cartDetails);
        }
        return "redirect:/checkout";
    }
    @GetMapping("/checkout")
    public String checkout(Model model,Principal principal) {
        model.addAttribute("voucherList",voucherRepo.findAvailableVouchers(LocalDateTime.now()));
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

    @GetMapping("/getProductPrice")
    @ResponseBody
    public Map<String, Object> getProductPrice(@RequestParam Integer product, @RequestParam Integer color, @RequestParam Integer size,Principal principal) {
        Map<String, Object> response = new HashMap<>();
        List<CartDetail> cartDetails;
        if (principal != null) {
            Customer customer = customerRepo.findByUsername(principal.getName());
            cartDetails = cartDetailRepo.findByCustomerIdAndStatus(customer.getId(), (byte) 1);
        }else {
            cartDetails = cartService.getCartDetails();
            for (CartDetail cartDetail : cartDetails) {
                ProductDetail latestProductDetail = productDetailRepo.findById(cartDetail.getProductDetail().getId()).orElse(null);
                if (latestProductDetail != null) {
                    cartDetail.setProductDetail(latestProductDetail);
                }
            }
        }
        try {
            ProductDetail productDetail = productDetailRepo.findByProductIdAndColorIdAndSizeIdAndStatus(product, color, size,(byte) 1);
            if (productDetail != null) {
                response.put("id", productDetail.getId());
                response.put("price", CurrencyUtil.formatCurrency(productDetail.getPrice()));
                response.put("stock", productDetail.getStock()); // Thêm số lượng tồn kho
                if(productDetail.getProduct().getPromotion() != null) {
                        int sale = productDetail.getProduct().getPromotion().getDiscountAmount() != null && productDetail.getProduct().getPromotion().getDiscountAmount() > 0
                                ? productDetail.getPrice() - productDetail.getProduct().getPromotion().getDiscountAmount()
                                : productDetail.getPrice() - (productDetail.getPrice() * productDetail.getProduct().getPromotion().getDiscountPercentage()) / 100;
                    response.put("sale", CurrencyUtil.formatCurrency(sale));
                }
                response.put("stockInCart", cartDetails.stream()
                        .filter(cartDetail -> cartDetail.getProductDetail().getId().equals(productDetail.getId()))
                        .mapToInt(CartDetail::getQuantity)
                        .findFirst()
                        .orElse(0));
            } else {
                throw new RuntimeException("ProductDetail not found for color: " + color + " and size: " + size);
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.put("id", -1);   // Giá trị id lỗi
            response.put("price", "Sản phẩm không tồn tại"); // Giá trị price lỗi
            response.put("stock", "Sản phẩm không tồn tại"); // Giá trị stock lỗi
            response.put("sale", "Sản phẩm không tồn tại");
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
                    cart.setUnitPrice(productDetail.getPrice());
                    cartDetailRepo.save(cart);
                }
            }
            if (isNone) {
                CartDetail cartDetail1 = new CartDetail();
                cartDetail1.setProductDetail(productDetail);
                cartDetail1.setQuantity(quantity);
                cartDetail1.setUnitPrice(productDetail.getPrice());
                cartDetail1.setCustomer(customer);
                cartDetailRepo.save(cartDetail1);
            }
        } else {
            cartService.add(id,quantity);
            return "redirect:/shop-cart";
        }
        return "redirect:/shop-cart";
    }
    // Hàm tính giá sau khi giảm
    int getDiscountedPrice(ProductDetail productDetail, Product product) {
        if (product.getPromotion() != null) {
            if (product.getPromotion().getDiscountAmount() != null && product.getPromotion().getDiscountAmount() > 0) {
                return productDetail.getPrice() - product.getPromotion().getDiscountAmount();
            } else if (product.getPromotion().getDiscountPercentage() != null) {
                return productDetail.getPrice() - (productDetail.getPrice() * product.getPromotion().getDiscountPercentage()) / 100;
            }
        }
        return productDetail.getPrice();
    }
    @PostMapping("/check-out")
    public String checkOut(@RequestParam("addressInv") String addressInv,
                           @RequestParam("cityInv") String cityInv,
                           @RequestParam("districtInv") String districtInv,
                           @RequestParam("wardInv") String wardInv,
                           @RequestParam("phoneInv") String phoneInv,
                           @RequestParam("emailInv") String emailInv,
                           @RequestParam("paymentInv") Integer idPayment,
                           //thêm voucher
                           @RequestParam(value = "code", required = false) String code, HttpServletRequest request,
                           HttpSession session, RedirectAttributes redirectAttributes
    ) {
        // Lấy phương thức thanh toán
        PaymentMethod paymentMethod = paymentMethodRepo.findById(idPayment).orElse(null);
        if (paymentMethod == null) {
            redirectAttributes.addFlashAttribute("error", "Phương thức thanh toán không tồn tại.");
            return "redirect:/checkout";
        }
        // Lấy hóa đơn tạm thời
        Invoice temporaryInvoice = cartService.getTemporaryInvoice();
        if (temporaryInvoice == null) {
            return "redirect:/cart";
        }
        // Kiểm tra voucher nếu mã voucher được nhập
        if (code != null && !code.trim().isEmpty()) {
            Voucher voucher = voucherRepo.findByCode(code);
            if (voucher == null || voucher.getQuantity() <= 0) {
                redirectAttributes.addFlashAttribute("error", "Mã giảm giá không hợp lệ hoặc đã hết.");
                return "redirect:/checkout";
            }

            // Kiểm tra thời hạn sử dụng của voucher
            if (voucher.getEndDate().isBefore(LocalDateTime.now())) {
                redirectAttributes.addFlashAttribute("error", "Mã giảm giá này đã hết hạn.");
                return "redirect:/checkout";
            }
            if (voucher.getMinOrderValue() > temporaryInvoice.getFinalPrice()) {
                redirectAttributes.addFlashAttribute("error", "Giá trị đơn hàng của bạn chưa đủ để sử dụng voucher này.");
                return "redirect:/checkout";
            }
            temporaryInvoice.setVoucher(voucher);
        }


        // Cập nhật thông tin vận chuyển và thanh toán cho hóa đơn tạm
        temporaryInvoice.setShippingAddress(addressInv);
        temporaryInvoice.setShippingCity(cityInv);
        temporaryInvoice.setShippingDistrict(districtInv);
        temporaryInvoice.setShippingWard(wardInv);
        temporaryInvoice.setPhone(phoneInv);
        temporaryInvoice.setPaymentMethod(paymentMethod);

        // Kiểm tra giỏ hàng
        List<CartDetail> cartDetails;
        if (temporaryInvoice.getCustomer() != null) {
            cartDetails = cartDetailRepo.findByCustomerIdAndStatus(temporaryInvoice.getCustomer().getId(), (byte) 1);
        } else {
            cartDetails = cartService.getCartDetails();
        }

        if (cartDetails == null || cartDetails.isEmpty()) {
            return "redirect:/cart";
        }
        String productError = checkProductAvailability(cartDetails);
        if (productError != null) {
            redirectAttributes.addFlashAttribute("error", productError);
            return "redirect:/checkout";
        }

        session.setAttribute("emailInv", emailInv);
        if(paymentMethod.getId()==3){
            Invoice savedInvoice = saveInvoice(temporaryInvoice, cartDetails,null);
            // Cập nhật trạng thái giỏ hàng
            if (temporaryInvoice.getCustomer() != null) {
                for (CartDetail cart : cartDetails) {
                    cart.setStatus((byte) 2); // Đánh dấu là đã thanh toán
                    cartDetailRepo.save(cart);
                }
            } else {
                cartService.clear(); // Xóa giỏ hàng nếu người dùng không đăng nhập
            }
            // Xóa hóa đơn tạm thời
            cartService.clearTemporaryInvoice();
            String trackingNumber = savedInvoice.getInvoiceTrackingNumber();
            String trackingLink = "http://localhost:8080/look-up?orderId="+trackingNumber;
            sendEmail(emailInv,"Thông tin đơn hàng của bạn",trackingNumber,trackingLink);
            // Chuyển đến trang xác nhận
            return "redirect:/confirmation?invoiceId=" + savedInvoice.getId();
        }
        else {
            try {
                // Tạo URL thanh toán VNPay
                String paymentUrl = vnPayService.createPaymentUrl(saveInvoiceVnPay(temporaryInvoice,cartDetails),request);
                return "redirect:" + paymentUrl; // Chuyển hướng đến VNPay
            } catch (Exception e) {
                e.printStackTrace();
                return "redirect:/checkout?error=payment_error"; // Xử lý lỗi tạo thanh toán
            }
        }
    }
    private String checkProductAvailability(List<CartDetail> cartDetails) {
        for (CartDetail cartDetail : cartDetails) {
            ProductDetail productDetail = productDetailRepo.findById(cartDetail.getProductDetail().getId()).orElse(null);

            // Kiểm tra nếu không tìm thấy sản phẩm hoặc sản phẩm không còn khả dụng (status = 2)
            if (productDetail == null || productDetail.getStatus() == (byte) 2) {
                return "Sản phẩm " + (productDetail != null ? productDetail.getProduct().getName() : "không xác định") + " không còn khả dụng! Vui lòng xóa khỏi giỏ hàng.";
            }

            // Kiểm tra số lượng trong kho so với số lượng yêu cầu
            int stockQuantity = productDetail.getStock(); // Số lượng trong kho
            int requestedQuantity = cartDetail.getQuantity(); // Số lượng yêu cầu

            if (requestedQuantity > stockQuantity) {
                return "Sản phẩm " + productDetail.getProduct().getName() + " không đủ số lượng trong kho! Chỉ còn " + stockQuantity + " sản phẩm.";
            }
        }
        return null; // Tất cả đều hợp lệ
    }
    @GetMapping("/checkout/vnpay/return")
    public String vnpayReturn(@RequestParam Map<String, String> params, Model model, HttpSession session, Principal principal) {
        try {
            System.out.println("Received VNPay callback: " + params);

//             Kiểm tra chữ ký
//            boolean isValid = vnPayService.validateReturnUrl(params);
//            System.out.println("Signature validation result: " + isValid);
//
//            if (!isValid) {
//                return "redirect:/checkout?error=invalid_signature";
//            }

            String txnRef = params.get("vnp_TxnRef");
            String responseCode = params.get("vnp_ResponseCode");
            System.out.println("Transaction Ref: " + txnRef);
            System.out.println("Response Code: " + responseCode);

            Invoice temporaryInvoice = cartService.getTemporaryInvoice();
            System.out.println(temporaryInvoice);
            if (temporaryInvoice == null) {
                System.out.println("Invoice not found for transaction reference: " + txnRef);
                return "redirect:/checkout?error=transaction_not_found";
            }

            // Giao dịch thành công
            if ("00".equals(responseCode)) {

                System.out.println("Invoice updated to status 2");
                List<CartDetail> cartDetails;
                if(principal != null){
                    Customer customer = customerRepo.findByUsername(principal.getName());
                    cartDetails = cartDetailRepo.findByCustomerIdAndStatus(customer.getId(),(byte) 1);;
                }else{
                    cartDetails = cartService.getCartDetails();
                }

                Invoice invoice = saveInvoice(temporaryInvoice,cartDetails,txnRef);
                // Lấy thông tin giỏ hàng
                if (temporaryInvoice.getCustomer() != null) {
                    for (CartDetail cart : cartDetails) {
                        cart.setStatus((byte) 2);
                        cartDetailRepo.save(cart);
                    }
                    System.out.println("Cart details updated to status 2");
                } else {
                    cartService.clear();
                    System.out.println("Cart details not found in session");
                }
                cartService.clearTemporaryInvoice();

                String trackingNumber = invoice.getInvoiceTrackingNumber();
                String trackingLink = "http://localhost:8080/look-up?orderId=" + trackingNumber;
                sendEmail(session.getAttribute("emailInv").toString(), "Thông tin đơn hàng của bạn", trackingNumber, trackingLink);

                return "redirect:/confirmation?invoiceId=" + invoice.getId();
            } else {
                System.out.println("Transaction failed with response code: " + responseCode);
                return "redirect:/checkout?error=payment_failed";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/checkout?error=payment_error";
        }
    }

    private Invoice saveInvoice(Invoice invoice, List<CartDetail> cartDetails, String txnRef) {
        // Tạo mã giao dịch duy nhất
        String randomUUID = UUID.randomUUID().toString().substring(0, 5).toUpperCase();
        // Tính tổng giá hóa đơn sau khi áp dụng chương trình giảm giá
        int totalPriceAfterPromotion = 0;
        for (CartDetail cartDetail : cartDetails) {
            ProductDetail productDetail = cartDetail.getProductDetail();
            Product product = productDetail.getProduct();

            // Tính giá sau giảm giá từ chương trình khuyến mãi
            int discountedPrice = getDiscountedPrice(productDetail, product);
            totalPriceAfterPromotion += discountedPrice * cartDetail.getQuantity();
        }
        int totalPrice = cartDetails.stream()
                .mapToInt(carts -> carts.getProductDetail().getPrice() * carts.getQuantity())
                .sum();
        invoice.setTotalPrice(totalPrice);

        // Áp dụng voucher nếu có
        if (invoice.getVoucher() != null) {
            Voucher voucher = invoice.getVoucher();
            // Tính giảm giá từ voucher
            int voucherDiscount = (totalPriceAfterPromotion * voucher.getDiscountPercentage()) / 100;
            if (voucherDiscount > voucher.getDiscountMax()) {
                voucherDiscount = voucher.getDiscountMax();
            }

            // Cập nhật giá trị cuối cùng sau khi áp dụng voucher
            invoice.setPromotionDiscount(totalPrice - totalPriceAfterPromotion);
            invoice.setVoucherDiscount(voucherDiscount);
            invoice.setFinalPrice((totalPriceAfterPromotion - voucherDiscount) + 30000);
            voucher.setQuantity(voucher.getQuantity() - 1);
            voucherRepo.save(voucher);
        } else {
            // Nếu không có voucher, finalPrice = totalPrice sau khuyến mãi
            invoice.setPromotionDiscount(totalPrice - totalPriceAfterPromotion);
            invoice.setFinalPrice(totalPriceAfterPromotion + 30000);
        }

        // Lưu hóa đơn vào cơ sở dữ liệu
        Invoice savedInvoice = invoiceRepo.save(invoice);
        // Lưu chi tiết hóa đơn và cập nhật số lượng sản phẩm
        for (CartDetail cartDetail : cartDetails) {
            if (cartDetail.getProductDetail().getStock() < cartDetail.getQuantity()) {
                throw new IllegalArgumentException("Không đủ hàng cho sản phẩm  : "
                        + cartDetail.getProductDetail().getId());
            }

            // Tạo chi tiết hóa đơn
            InvoiceDetail invoiceDetail = new InvoiceDetail();
            invoiceDetail.setInvoice(savedInvoice);
            invoiceDetail.setProductDetail(cartDetail.getProductDetail());
            invoiceDetail.setQuantity(cartDetail.getQuantity());

            // Lấy giá sau giảm giá từ chương trình khuyến mãi
            int discountedPrice = getDiscountedPrice(cartDetail.getProductDetail(), cartDetail.getProductDetail().getProduct());
            invoiceDetail.setUnitPrice(cartDetail.getProductDetail().getPrice()); // Giá gốc
            invoiceDetail.setDiscountAmount(discountedPrice == cartDetail.getProductDetail().getPrice() ? 0 : discountedPrice);
            invoiceDetail.setFinalPrice(discountedPrice * cartDetail.getQuantity()); // Giá sau giảm giá

            invoiceDetailRepo.save(invoiceDetail);
        }
        //Set trạng thái hoá đơn đã thanh toán
        savedInvoice.setStatus((byte) 3);
        invoiceRepo.save(savedInvoice);

        // Lưu lịch sử trạng thái hóa đơn
        InvoiceStatusHistory invoiceStatusHistory = new InvoiceStatusHistory();
        invoiceStatusHistory.setInvoice(savedInvoice);
        invoiceStatusHistory.setNewStatus((byte) 3);
        invoiceStatusHistory.setUpdatedAt(LocalDateTime.now());
        invoiceHistoryStatusRepo.save(invoiceStatusHistory);

        // Lưu lịch sử thanh toán
        PaymentHistory paymentHistory = new PaymentHistory();
        paymentHistory.setInvoice(savedInvoice);
        paymentHistory.setPaymentDate(LocalDateTime.now());
        paymentHistory.setTransactionCode(txnRef != null ? txnRef : randomUUID);
        paymentHistory.setAmountPaid(savedInvoice.getFinalPrice());
        paymentHistory.setPaymentMethod(savedInvoice.getPaymentMethod());
        paymentHistoryRepo.save(paymentHistory);
        return savedInvoice;
    }
    public Invoice saveInvoiceVnPay(Invoice invoice, List<CartDetail> cartDetails){
        // Tạo một bản sao của hóa đơn
        Invoice invoiceVnPay = new Invoice();
        invoiceVnPay.setShippingAddress(invoice.getShippingAddress());
        invoiceVnPay.setShippingCity(invoice.getShippingCity());
        invoiceVnPay.setShippingDistrict(invoice.getShippingDistrict());
        invoiceVnPay.setShippingWard(invoice.getShippingWard());
        invoiceVnPay.setPhone(invoice.getPhone());
        invoiceVnPay.setPaymentMethod(invoice.getPaymentMethod());
        invoiceVnPay.setVoucher(invoice.getVoucher());
        invoiceVnPay.setCustomer(invoice.getCustomer());
        invoiceVnPay.setInvoiceTrackingNumber(invoice.getInvoiceTrackingNumber());

        int totalPriceAfterPromotion = 0;
        for (CartDetail cartDetail : cartDetails) {
            ProductDetail productDetail = cartDetail.getProductDetail();
            Product product = productDetail.getProduct();

            // Tính giá sau giảm giá từ chương trình khuyến mãi
            int discountedPrice = getDiscountedPrice(productDetail, product);
            totalPriceAfterPromotion += discountedPrice * cartDetail.getQuantity();
        }

        int totalPrice = cartDetails.stream()
                .mapToInt(carts -> carts.getProductDetail().getPrice() * carts.getQuantity())
                .sum();
        invoiceVnPay.setTotalPrice(totalPrice);

        // Áp dụng voucher nếu có
        if (invoiceVnPay.getVoucher() != null) {
            Voucher voucher = invoiceVnPay.getVoucher();

            // Tính giảm giá từ voucher
            int voucherDiscount = (totalPriceAfterPromotion * voucher.getDiscountPercentage()) / 100;
            if (voucherDiscount > voucher.getDiscountMax()) {
                voucherDiscount = voucher.getDiscountMax();
            }

            invoiceVnPay.setVoucherDiscount(voucherDiscount);
            invoiceVnPay.setPromotionDiscount(totalPrice - totalPriceAfterPromotion);
            invoiceVnPay.setFinalPrice((totalPriceAfterPromotion - voucherDiscount) + 30000);
        } else {
            invoiceVnPay.setPromotionDiscount(totalPrice - totalPriceAfterPromotion);
            invoiceVnPay.setFinalPrice(totalPriceAfterPromotion + 30000);
        }

        return invoiceVnPay;
    }

    @GetMapping("/confirmation")
    public String confirmation(@RequestParam("invoiceId") Integer id, Model model) {
        model.addAttribute("invoice", invoiceRepo.findById(id).orElse(null));
        return "test-theme/confirmation";
    }
    private void sendEmail(String to, String subject, String trackingNumber, String trackingLink) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText("Mã theo dõi của bạn là: " + trackingNumber + "\n"
                + "Bạn có thể theo dõi đơn hàng của mình tại: " + trackingLink);
        mailSender.send(message);
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
    @GetMapping("/account")
    public String account(Principal principal,Model model) {
        String username= principal.getName();
        Customer customer = customerRepo.findByUsername(username);
        model.addAttribute("customer",customer);
        return "test-theme/account";
    }
    @PostMapping("/account/update")
    public String updateCustomer(@Valid  @ModelAttribute Customer customer, BindingResult bindingResult) {
        Customer existingCustomer = customerRepo.findById(customer.getId()).orElse(null);
        if (existingCustomer != null) {
            // Kiểm tra số điện thoại đã tồn tại, nhưng bỏ qua nếu không thay đổi
            if(existingCustomer.getPhone()!=null) {
                if (!existingCustomer.getPhone().equals(customer.getPhone()) && customerRepo.existsByPhone(customer.getPhone())) {
                    bindingResult.rejectValue("phone", "error.customer", "Số điện thoại đã tồn tại");
                }
            }
            // Kiểm tra email đã tồn tại, nhưng bỏ qua nếu không thay đổi
            if (!existingCustomer.getEmail().equals(customer.getEmail()) && customerRepo.existsByEmail(customer.getEmail())) {
                bindingResult.rejectValue("email", "error.customer", "Email đã tồn tại");
            }
        }

        // Kiểm tra có lỗi không
        if (bindingResult.hasErrors()) {
            return "test-theme/account"; // Trả về trang cập nhật nếu có lỗi
        }
        if(customer.getPhoto().equals("")){
            customer.setPhoto(existingCustomer.getPhoto());
        }
        // Sao chép các trường không có trong form\
        customer.setCountry("Việt Nam");
        customer.setStatus(existingCustomer.getStatus());
        customer.setUpdatedDate(LocalDateTime.now());
        customer.setCreatedDate(existingCustomer.getCreatedDate());
        customerRepo.save(customer);
        return "redirect:/account";
    }
    @GetMapping("/order")
    public String order(Principal principal,Model model) {
        String username= principal.getName();
        Customer customer = customerRepo.findByUsername(username);
        List<Invoice> invoiceList = invoiceRepo.findByCustomerId(customer.getId());
        model.addAttribute("customer",customer);
        model.addAttribute("invoiceList",invoiceList);
        return "test-theme/order";
    }
    @GetMapping("/orderDetail/{id}")
    public String orderdetail(@PathVariable("id") Integer id, Model model) {
        Invoice invoice = invoiceRepo.findById(id).orElse(null);
        Customer customer = customerRepo.findeById(invoice.getCustomer().getId());
        String addressC = addressService.getWardNameByCode(invoice.getShippingWard())+ ' '+ addressService.getDistrictNameByCode(invoice.getShippingDistrict())+' '+ addressService.getProvinceNameByCode(invoice.getShippingCity());
        model.addAttribute("addressC",addressC);
        model.addAttribute("customer",customer);
        model.addAttribute("invoice",invoice);
        List<InvoiceDetail> listInvoiceDetail = invoiceDetailRepo.findByInvoiceId(id);
        Integer totalPrice = invoice.getInvoiceDetails().stream()
                .mapToInt(InvoiceDetail::getFinalPrice)
                .sum();
        Integer totalDiscount = totalPrice - invoice.getFinalPrice();
        model.addAttribute("totalDiscount", totalDiscount);
        model.addAttribute("listInvoiceDetail",listInvoiceDetail);
        return "test-theme/orderdetail";
    }

    @GetMapping("/look-up")
    public String lookUp(@RequestParam(value = "orderId", required = false) String orderId, Model model) {
        if (orderId == null || orderId.isEmpty()) {
            return "test-theme/look-up"; // Trả về trang tra cứu
        }
        Invoice invoice = invoiceRepo.findByInvoiceTrackingNumber(orderId);
        if(invoice==null){
            model.addAttribute("error","Đơn hàng không tồn tại, vui lòng thử lại");
        }else{
            model.addAttribute("invoice",invoice);
            String addressC = addressService.getWardNameByCode(invoice.getShippingWard())+ ' '+ addressService.getDistrictNameByCode(invoice.getShippingDistrict())+' '+ addressService.getProvinceNameByCode(invoice.getShippingCity());
            model.addAttribute("addressC",addressC);
            List<InvoiceDetail> listInvoiceDetail = invoiceDetailRepo.findByInvoiceId(invoice.getId());
            model.addAttribute("listInvoiceDetail",listInvoiceDetail);
            model.addAttribute("listInvoiceDetail",listInvoiceDetail);
        }

        return "test-theme/look-up";
    }
    @PostMapping("/update-cart")
    public String updateCart(@RequestParam("productId") int id,
                             @RequestParam("quantity") int quantity,
                             Principal principal) {
        if (principal != null) {
            Customer customer = customerRepo.findByUsername(principal.getName());
            CartDetail cartDetails = cartDetailRepo.findByCustomerIdAndStatusAndProductDetailId(customer.getId(),(byte) 1,id);
            if (cartDetails != null) {
                if (cartDetails.getQuantity() > quantity) {
                    cartDetails.setQuantity(quantity);
                    cartDetailRepo.save(cartDetails);
                }
                if (cartDetails.getQuantity() < quantity) {
                    cartDetails.setQuantity(quantity);
                    cartDetailRepo.save(cartDetails);
                }
            }
        }else{
            cartService.update(id, quantity);
        }
            return "redirect:/shop-cart";
    }
    @Transactional
    @GetMapping("/delete-cart/{id}")
    public String deleteCart(@PathVariable("id") Integer id,Principal principal) {
        if (principal != null) {
            cartDetailRepo.deleteCartDetailByProductDetailId(id);
        } else {
            cartService.remove(id);
        }
        return "redirect:/shop-cart";
    }
    @GetMapping("/change-pass")
    public String changePass(Model model,Principal principal){
        String username = principal.getName();
        Customer customer = customerRepo.findByUsername(username);
        model.addAttribute("customer",customer);
        return "test-theme/change-pass";
    }
    @PostMapping("/change-pass")
    public String changePassword(@RequestParam String currentPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmPassword,
                                 Principal principal,
                                 RedirectAttributes redirectAttributes){
        String username = principal.getName();
        Customer customer = customerRepo.findByUsername(username);
        System.out.println("pass:"+currentPassword+"passnew:"+newPassword+":"+confirmPassword+":"+username);
        if(!passwordEncoder.matches(currentPassword,customer.getPassword())){
            redirectAttributes.addFlashAttribute("error","Mật khẩu hiện tại không chính xác !");
            return "redirect:/change-pass";
        }
        if(!newPassword.equals(confirmPassword)){
            redirectAttributes.addFlashAttribute("error","Mật khẩu xác nhận không khớp với mật khẩu mới !");
            return "redirect:/change-pass";
        }
        customer.setPassword(passwordEncoder.encode(newPassword));
        customerRepo.save(customer);
        redirectAttributes.addFlashAttribute("message","Thay đổi thành công !");
        return "redirect:/change-pass";
    }
}
