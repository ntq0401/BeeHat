package com.beehat.controller.admin;

import com.beehat.entity.*;
import com.beehat.repository.*;
import com.beehat.service.InvoiceService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.io.OutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/store")
public class StoreController {
    @Autowired
    InvoiceRepo invoiceRepo;
    @Autowired
    InvoiceDetailRepo invoiceDetailRepo;
    @Autowired
    ProductRepo productRepo;
    @Autowired
    ProductDetailRepo productDetailRepo;
    @Autowired
    PaymentMethodRepo paymentMethodRepo;
    @Autowired
    CustomerRepo customerRepo;
    @Autowired
    VoucherRepo voucherRepo;
    @Autowired
    InvoiceService invoiceService;
    @Autowired
    EmployeeRepo employeeRepo;

    @ModelAttribute("productDetails")
    public Page<ProductDetail> detail(@RequestParam(defaultValue = "0") int page,
                                      Model model) {
        Pageable pageable = PageRequest.of(page, 10);
        Page<ProductDetail> productDetailsPage = productDetailRepo.findAll(pageable);

        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productDetailsPage.getTotalPages());
        model.addAttribute("totalItems", productDetailsPage.getTotalElements());
        return productDetailsPage;
    }

    @ModelAttribute("listInvoice")
    List<Invoice> listOrder() {
        return invoiceRepo.findByStatus(Byte.valueOf("0"));
    }

    @ModelAttribute("invoiceDetail")
    List<InvoiceDetail> listOrderDetail() {
        return invoiceDetailRepo.findAll();
    }

    @ModelAttribute("listPayment")
    List<PaymentMethod> listPayment() {
        return paymentMethodRepo.findAll();
    }

    @ModelAttribute("listCustomer")
    List<Customer> listCustomer() {
        return customerRepo.findAll();
    }

    @ModelAttribute("listVoucher")
    List<Voucher> listVoucher() {
        return voucherRepo.findAll();
    }

    @GetMapping("/index")
    public String store(Model model) {
        model.addAttribute("i", new Invoice());
        model.addAttribute("pay",new Invoice());
        model.addAttribute("kh", new Customer());
        return "admin/store/index";
    }

    @PostMapping("/add-invoice")
    public String addInvoice(@ModelAttribute("i") Invoice invoice) {
//        ,@RequestParam(name = "username") String username
//        Employee employee = employeeRepo.findByUsername(username);
        Employee employee = employeeRepo.findByUsername("user");
        invoice.setEmployee(employee);
        invoiceRepo.save(invoice);
        return "redirect:/admin/store/invoice-detail/" + invoice.getId();
    }

    @GetMapping("/invoice-detail/{id}")
    public String invoiceDetail(@PathVariable int id, Model model) {
        model.addAttribute("activeInvoiceId", id);
        model.addAttribute("infoInvoice", invoiceRepo.findById(id).orElseThrow(() -> new NullPointerException("Invoice not found")));
        model.addAttribute("totalAmount", invoiceRepo.findById(id).get().getTotalPrice());
        LocalDateTime createdDate = invoiceRepo.findById(id).get().getCreatedDate(); // Lấy giá trị LocalDateTime
        model.addAttribute("kh", new Customer());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
        String formattedDate = createdDate.format(formatter); // Format thành chuỗi
        model.addAttribute("formattedDate", formattedDate); // Truyền vào model
        List<InvoiceDetail> listInvoiceDetail = invoiceDetailRepo.findByInvoiceId(id);
        model.addAttribute("listInvoiceDetail", listInvoiceDetail);
        model.addAttribute("invoiceId", id);
//        String qrUrl = "https://img.vietqr.io/image/MB-2234686869-compact2.png?amount=" + invoiceRepo.findById(id).get().getTotalPrice() + "&addInfo=Thanh toan hoa don : " + invoiceRepo.findById(id).get().getInvoiceTrackingNumber() + "&accountName=NGUYEN THE QUANG";
//        model.addAttribute("qrUrl", qrUrl);
        return "admin/store/index";
    }

    @PostMapping("/add-product-detail-to-invoice")
    public String addInvoice(@RequestParam(value = "invoiceId") Integer idInvoice,
                             @RequestParam("productDetailId") int idProductDetail, Model model) {

        ProductDetail productDetail = productDetailRepo.findById(idProductDetail).orElse(null);
        Invoice invoice = invoiceRepo.findById(idInvoice).orElse(null);
        if (productDetail == null || invoice == null) {
            return "redirect:/admin/store/invoice-detail/" + idInvoice; // xử lý lỗi nếu không tìm thấy
        }

        List<InvoiceDetail> listInvoiceDetail = invoiceDetailRepo.findByInvoiceId(idInvoice);

        boolean isExistingProduct = false;
        for (InvoiceDetail invoiceDetail : listInvoiceDetail) {
            if (invoiceDetail.getProductDetail().getId() == idProductDetail) {
                isExistingProduct = true;
                // Cập nhật số lượng và giá tiền cuối cùng của sản phẩm trong hóa đơn
                invoiceDetail.setQuantity(invoiceDetail.getQuantity() + 1);
                invoiceDetail.setFinalPrice(invoiceDetail.getUnitPrice() * invoiceDetail.getQuantity());
                invoiceDetailRepo.save(invoiceDetail);

                // Giảm số lượng tồn kho của sản phẩm
                productDetail.setStock(productDetail.getStock() - 1);
                productDetailRepo.save(productDetail);
                break;
            }
        }

        if (!isExistingProduct) {
            // Thêm sản phẩm chi tiết mới vào hóa đơn nếu chưa có trong danh sách
            InvoiceDetail invoiceDetail = new InvoiceDetail();
            invoiceDetail.setInvoice(invoice);
            invoiceDetail.setProductDetail(productDetail);
            invoiceDetail.setQuantity(1);
            invoiceDetail.setUnitPrice(productDetail.getPrice());
            invoiceDetail.setFinalPrice(productDetail.getPrice());
            invoiceDetailRepo.save(invoiceDetail);

            // Giảm số lượng tồn kho của sản phẩm
            productDetail.setStock(productDetail.getStock() - 1);
            productDetailRepo.save(productDetail);
            listInvoiceDetail.add(invoiceDetail);
        }

        // Cập nhật tổng tiền của hóa đơn sau khi đã thêm sản phẩm chi tiết
        int totalInvoicePrice = listInvoiceDetail.stream().mapToInt(InvoiceDetail::getFinalPrice).sum();
        invoice.setTotalPrice(totalInvoicePrice);
        invoice.setFinalPrice(totalInvoicePrice); // Nếu có giảm giá hoặc phí thêm, xử lý tại đây
        invoiceRepo.save(invoice);

        return "redirect:/admin/store/invoice-detail/" + idInvoice;
    }

    @GetMapping("/delete-invoice/{id}")
    public String deleteInvoice(@PathVariable int id, Model model) {
        // Tìm và cộng lại số lượng sản phẩm trong kho
        List<InvoiceDetail> invoiceDetails = invoiceDetailRepo.findByInvoiceId(id);
        for (InvoiceDetail invoiceDetail : invoiceDetails) {
            ProductDetail productDetail = invoiceDetail.getProductDetail();
            int updatedStock = productDetail.getStock() + invoiceDetail.getQuantity();
            productDetail.setStock(updatedStock);
        }
        productDetailRepo.saveAll(invoiceDetails.stream().map(InvoiceDetail::getProductDetail).collect(Collectors.toList()));

        Invoice invoice = invoiceRepo.findById(id).orElseThrow(() -> new NullPointerException("Invoice not found"));
        invoice.setStatus(Byte.valueOf("1"));
        invoiceRepo.save(invoice);
        return "redirect:/admin/store/index";
    }

    @PostMapping("/add-new-customer")
    public String addCustomer(@ModelAttribute("kh") Customer customer,
                              @RequestParam("invoiceId") int invoiceId) {
        customerRepo.save(customer);
        return "redirect:/admin/store/invoice-detail/" + invoiceId;
    }

    @GetMapping("/{id}/pdf")
    public void generateInvoicePdf(@PathVariable Integer id, HttpServletResponse response) throws Exception {
        Invoice invoice = invoiceRepo.findById(id).orElse(null);
        byte[] pdfData = invoiceService.generateInvoicePdf(invoice);

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "inline; filename=invoice_" + id + ".pdf");
        response.setContentLength(pdfData.length);

        try (OutputStream os = response.getOutputStream()) {
            os.write(pdfData);
            os.flush();
        }
    }

    @GetMapping("/delete-product-from-invoice/{id}")
    public String deleteProductFromInvoice(@PathVariable int id) {
        InvoiceDetail invoiceDetail = invoiceDetailRepo.findById(id).orElse(null);
        if (invoiceDetail != null) {
            int invoiceId = invoiceDetail.getInvoice().getId();

            ProductDetail productDetail = productDetailRepo.findById(invoiceDetail.getProductDetail().getId()).orElse(null);
            if (productDetail != null) {
                productDetail.setStock(productDetail.getStock() + invoiceDetail.getQuantity());
                productDetailRepo.save(productDetail);
            }
            invoiceDetailRepo.deleteById(id);
            Invoice invoice = invoiceDetail.getInvoice();
            int totalInvoicePrice = invoice.getInvoiceDetails().stream().mapToInt(InvoiceDetail::getFinalPrice).sum();
            invoice.setTotalPrice(totalInvoicePrice);
            invoice.setFinalPrice(totalInvoicePrice);
            invoiceRepo.save(invoice);
            return "redirect:/admin/store/invoice-detail/" + invoiceId;
        }

// Xử lý nếu không tìm thấy invoiceDetail
        return "redirect:/admin/store/invoice-detail";  // Có thể thay đổi đường dẫn nếu cần
    }

    @PostMapping("/update-quantity")
    public String updateQuantity(@RequestParam("ids") List<Integer> ids, @RequestParam("quantities") List<Integer> quantities) {
        int sl = 0;
        for (int i = 0; i < ids.size(); i++) {
            int id = ids.get(i);
            //số lượng sản phẩm khi thay đổi trên form
            int quantity = quantities.get(i);
            InvoiceDetail invoiceDetail = invoiceDetailRepo.findById(id).orElse(null);
            ProductDetail productDetail = invoiceDetail.getProductDetail();
            if (id == invoiceDetail.getId()) {
                //kiểm tra xem số lượng trên form bị trừ hay cộng
                //trừ
                if (invoiceDetail.getQuantity() > quantity) {
                    sl = invoiceDetail.getQuantity() - quantity;
                    invoiceDetail.setQuantity(quantity);
                    invoiceDetail.setFinalPrice(invoiceDetail.getUnitPrice() * quantity);
                    invoiceDetailRepo.save(invoiceDetail);
                    productDetail.setStock(productDetail.getStock() + sl);
                    productDetailRepo.save(productDetail);
                    Invoice invoice = invoiceDetail.getInvoice();
                    int totalPrice = invoice.getInvoiceDetails().stream().mapToInt(InvoiceDetail::getFinalPrice).sum();
                    invoice.setTotalPrice(totalPrice);
                    invoice.setFinalPrice(totalPrice);
                    invoiceRepo.save(invoice);
                    return "redirect:/admin/store/invoice-detail/" + invoiceDetail.getInvoice().getId();
                }
                //cộng
                if (invoiceDetail.getQuantity() < quantity) {
                    sl = quantity - invoiceDetail.getQuantity();
                    invoiceDetail.setQuantity(quantity);
                    invoiceDetail.setFinalPrice(invoiceDetail.getUnitPrice() * quantity);
                    invoiceDetailRepo.save(invoiceDetail);
                    productDetail.setStock(productDetail.getStock() - sl);
                    productDetailRepo.save(productDetail);
                    Invoice invoice = invoiceDetail.getInvoice();
                    int totalPrice = invoice.getInvoiceDetails().stream().mapToInt(InvoiceDetail::getFinalPrice).sum();
                    invoice.setTotalPrice(totalPrice);
                    invoice.setFinalPrice(totalPrice);
                    invoiceRepo.save(invoice);
                    return "redirect:/admin/store/invoice-detail/" + invoiceDetail.getInvoice().getId();
                }
            }
        }
        return "redirect:/admin/store/index";
    }
    @PostMapping("/add-customer-to-invoice")
    public String addCustomerToInvoice(@RequestParam("customerId") int customerId, @RequestParam("invoiceId") int invoiceId) {
        Invoice invoice = invoiceRepo.findById(invoiceId).orElse(null);
        Customer customer = customerRepo.findById(customerId).orElse(null);

        if (invoice != null && customer != null) {
            invoice.setCustomer(customer);
            invoiceRepo.save(invoice);
        }

        return "redirect:/admin/store/invoice-detail/" + invoiceId;
    }
    @PostMapping("/updateCustomer")
    public ResponseEntity<String> updateCustomer(
            @RequestParam("invoiceId") Integer invoiceId,
            @RequestParam("customerId") Integer customerId) {
        try {
            Invoice invoice = invoiceRepo.findById(invoiceId).orElse(null);
            Customer customer = customerRepo.findById(customerId).orElse(null);

            if (invoice != null && customer != null) {
                invoice.setCustomer(customer);
                invoiceRepo.save(invoice);
                return ResponseEntity.ok("Cập nhật thành công");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Không tìm thấy hóa đơn hoặc khách hàng");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Cập nhật thất bại");
        }
    }
    @PostMapping("/pay")
    public String pay(@RequestParam(name = "idPayment") int id,
                      @RequestParam(name = "totalPayment") int totalPayment,
                      @RequestParam(name = "voucherPayment",defaultValue = "") Voucher voucherPayment,
                      @RequestParam(name = "methodPayment") PaymentMethod paymentMethod) {
        Invoice invoice = invoiceRepo.findById(id).orElse(null);
        if (voucherPayment != null) {
            Voucher voucher = voucherRepo.findById(voucherPayment.getId()).orElse(null);
            invoice.setVoucher(voucher);
        }
        PaymentMethod paymentMethod1 = paymentMethodRepo.findById(paymentMethod.getId()).orElse(null);
        invoice.setFinalPrice(totalPayment);
        invoice.setPaymentMethod(paymentMethod1);
        invoice.setStatus(Byte.valueOf("2"));
        invoiceRepo.save(invoice);
        return "redirect:/admin/store/index";
    }
}
