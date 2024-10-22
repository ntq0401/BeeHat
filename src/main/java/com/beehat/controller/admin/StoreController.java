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
@RequestMapping("/admin/store")
public class StoreController {
    @Autowired
    InvoiceRepo invoiceRepo;
    @Autowired
    InvoiceDetailRepo invoiceDetailRepo;

    @ModelAttribute("listInvoice")
    List<Invoice> listOrder() {
        return invoiceRepo.findAll();
    }

    @GetMapping("/index")
    public String store() {
        return "admin/store/index";
    }

    @GetMapping("/invoice-detail/{id}")
    public String invoiceDetail(@PathVariable int id, Model model) {
        model.addAttribute("infoInvoice", invoiceRepo.findById(id).orElse(null));
        List<InvoiceDetail> listInvoiceDetail = invoiceDetailRepo.findByInvoiceId(id);
        model.addAttribute("listInvoiceDetail", listInvoiceDetail);
        return "admin/store/index";
    }
}
