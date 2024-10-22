package com.beehat.controller.admin;

import com.beehat.entity.Invoice;
import com.beehat.entity.InvoiceDetail;
import com.beehat.entity.Product;
import com.beehat.entity.ProductDetail;
import com.beehat.repository.InvoiceDetailRepo;
import com.beehat.repository.InvoiceRepo;
import com.beehat.repository.ProductDetailRepo;
import com.beehat.repository.ProductRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/index")
    public String store(Model model) {
        model.addAttribute("i",new Invoice());
        return "admin/store/index";
    }

    @GetMapping("/invoice-detail/{id}")
    public String invoiceDetail(@PathVariable int id, Model model) {
        model.addAttribute("infoInvoice", invoiceRepo.findById(id).orElse(null));
        List<InvoiceDetail> listInvoiceDetail = invoiceDetailRepo.findByInvoiceId(id);
        model.addAttribute("listInvoiceDetail", listInvoiceDetail);
        return "admin/store/index";
    }
    @PostMapping("/index")
    public String addInvoice(@ModelAttribute("i") Invoice invoice) {
        invoiceRepo.save(invoice);
        return "redirect:/admin/store/index";
    }
}
