package com.beehat.controller.admin;

import com.beehat.entity.Invoice;
import com.beehat.repository.InvoiceRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/admin/order")
public class OrderController {
    @Autowired
    InvoiceRepo invoiceRepo;
    @ModelAttribute("listInvoice")
    List<Invoice> listInvoice() { return invoiceRepo.findAll(); }
    @GetMapping("/index")
    public String order() {

        return "admin/order";
    }
}
