package com.beehat.controller.admin;

import com.beehat.entity.Invoice;
import com.beehat.entity.InvoiceDetail;
import com.beehat.repository.InvoiceDetailRepo;
import com.beehat.repository.InvoiceRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/admin/order")
public class OrderController {
    @Autowired
    InvoiceRepo invoiceRepo;
    @Autowired
    InvoiceDetailRepo invoiceDetailRepo;
    @ModelAttribute("listInvoice")
    List<Invoice> listInvoice() { return invoiceRepo.findAll(); }
    @GetMapping("/index")
    public String order() {

        return "admin/order";
    }
    @GetMapping("/view-invoice/{id}")
    public String viewInvoice(@PathVariable int id, Model model) {
        Invoice invoice = invoiceRepo.findById(id).get();
        List<InvoiceDetail> invoiceDetail = invoiceDetailRepo.findByInvoiceId(id);
        model.addAttribute("invoice", invoice);
        model.addAttribute("invoiceDetail", invoiceDetail);
        return "admin/view-invoice";
    }
}
