package com.beehat.controller.admin;

import com.beehat.entity.*;
import com.beehat.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

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
    @ModelAttribute("productDetails")
    public Page<ProductDetail> detail(@RequestParam(defaultValue = "0") int page,
                                      Model model) {
        Pageable pageable = PageRequest.of(page,10);
        Page<ProductDetail> productDetailsPage = productDetailRepo.findAll(pageable);

        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productDetailsPage.getTotalPages());
        model.addAttribute("totalItems", productDetailsPage.getTotalElements());
        return productDetailsPage;
    }
    @ModelAttribute("listInvoice")
    List<Invoice> listOrder() {
        return invoiceRepo.findAll();
    }
    @ModelAttribute("invoiceDetail")
    List<InvoiceDetail> listOrderDetail() { return invoiceDetailRepo.findAll(); }
    @ModelAttribute("listPayment")
    List<PaymentMethod> listPayment() { return paymentMethodRepo.findAll(); }
    @ModelAttribute("listCustomer")
    List<Customer> listCustomer() { return customerRepo.findAll();}
    @GetMapping("/index")
    public String store(Model model) {
        model.addAttribute("i",new Invoice());
        model.addAttribute("kh",new Customer());
        return "admin/store/index";
    }
    @PostMapping("/add-invoice")
    public String addInvoice(@ModelAttribute("i") Invoice invoice) {
        invoiceRepo.save(invoice);
        return "redirect:/admin/store/invoice-detail/"+invoice.getId();
    }

    @GetMapping("/invoice-detail/{id}")
    public String invoiceDetail(@PathVariable int id, Model model) {
        model.addAttribute("infoInvoice", invoiceRepo.findById(id).orElseThrow(() -> new NullPointerException("Invoice not found")));
        LocalDateTime createdDate =invoiceRepo.findById(id).get().getCreatedDate(); // Lấy giá trị LocalDateTime
        model.addAttribute("kh",new Customer());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
        String formattedDate = createdDate.format(formatter); // Format thành chuỗi
        model.addAttribute("formattedDate", formattedDate); // Truyền vào model
        List<InvoiceDetail> listInvoiceDetail = invoiceDetailRepo.findByInvoiceId(id);
        model.addAttribute("listInvoiceDetail", listInvoiceDetail);
        model.addAttribute("invoiceId",id);
        return "admin/store/index";
    }
    @PostMapping("/add-product-detail-to-invoice")
    public String addInvoice(@RequestParam("invoiceId") int idInvoice,
                             @RequestParam("productDetailId") int idProductDetail) {
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
    public String deleteInvoice(@PathVariable int id) {
        invoiceRepo.deleteById(id);
        return "redirect:/admin/store/index";
    }
    @PostMapping("/add-new-customer")
    public String addCustomer(@ModelAttribute("kh") Customer customer,
                              @RequestParam("invoiceId") int invoiceId) {
        customerRepo.save(customer);
        return "redirect:/admin/store/invoice-detail/"+invoiceId;
    }
}
