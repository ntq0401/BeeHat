package com.beehat.controller.admin;

import com.beehat.entity.Invoice;
import com.beehat.entity.InvoiceDetail;
import com.beehat.repository.InvoiceDetailRepo;
import com.beehat.repository.InvoiceRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
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
    List<Invoice> listInvoice() {
        return invoiceRepo.findAll(Sort.by(Sort.Direction.DESC, "updatedDate"));
    }
    @ModelAttribute("listInvoiceProcessing")
    List<Invoice> listInvoiceProcessing() {return invoiceRepo.findByStatusAndInvoiceStatus((byte) 0,(byte) 0);}
    @ModelAttribute("listInvoiceCanceled")
    List<Invoice> listInvoiceCanceled() {return invoiceRepo.findByStatusAndInvoiceStatus((byte) 1,(byte) 0);}
    @ModelAttribute("listInvoiceCompleted")
    List<Invoice> listInvoiceCompleted() {return invoiceRepo.findByStatusAndInvoiceStatus((byte) 2,(byte) 0);}
    @ModelAttribute("iconTitle")
    String iconTitle() {
        return "ph ph-file-text fs-3";
    }
    @ModelAttribute("pageTitle")
    String pageTitle() {
        return "Hoá đơn";
    }
    @GetMapping("/index")
    public String order() {
        return "admin/invoice/invoice";
    }
    @GetMapping("/view-invoice/{id}")
    public String viewInvoice(@PathVariable int id, Model model) {
        Invoice invoice = invoiceRepo.findById(id).get();
        List<InvoiceDetail> invoiceDetail = invoiceDetailRepo.findByInvoiceId(id);
        model.addAttribute("invoice", invoice);
        model.addAttribute("invoiceDetail", invoiceDetail);
        return "admin/invoice/view-invoice";
    }
}
