package com.beehat.controller.admin;

import com.beehat.entity.*;
import com.beehat.repository.InvoiceDetailRepo;
import com.beehat.repository.InvoiceHistoryStatusRepo;
import com.beehat.repository.InvoiceRepo;
import com.beehat.repository.ProductDetailRepo;
import com.beehat.service.InvoiceService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.OutputStream;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/admin/order")
public class InvoiceController {
    @Autowired
    InvoiceRepo invoiceRepo;
    @Autowired
    InvoiceDetailRepo invoiceDetailRepo;
    @Autowired
    InvoiceHistoryStatusRepo invoiceHistoryStatusRepo;
    @Autowired
    ProductDetailRepo productDetailRepo;
    @Autowired
    InvoiceService invoiceService;
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
    @PostMapping("/confirm-invoice")
    public String confirmInvoice(@RequestParam("idINV") int id,@RequestParam("description") String description) {
        updateInvoiceStatus(id, (byte) 4, description, (byte) 3);
        List<InvoiceDetail> invoiceDetail = invoiceDetailRepo.findByInvoiceId(id);
        for(InvoiceDetail invoiceDetail1 : invoiceDetail) {
            // Cập nhật số lượng trong kho
            ProductDetail productDetail = invoiceDetail1.getProductDetail();
            productDetail.setStock(productDetail.getStock() - invoiceDetail1.getQuantity());
            productDetailRepo.save(productDetail);
        }
        return "redirect:/admin/order/view-invoice/" + id;
    }
    @PostMapping("/pick-up")
    public String pickUpInvoice(@RequestParam("idINV") int id,@RequestParam("description") String description) {
        updateInvoiceStatus(id, (byte) 5, description, (byte) 4);
        return "redirect:/admin/order/view-invoice/" + id;

    }
    @PostMapping("/shipping")
    public String shippingInvoice(@RequestParam("idINV") int id,@RequestParam("description") String description) {
        updateInvoiceStatus(id, (byte) 6, description, (byte) 5);
        return "redirect:/admin/order/view-invoice/" + id;

    }
    @PostMapping("/delivery")
    public String deliveryInvoice(@RequestParam("idINV") int id,@RequestParam("description") String description) {
        updateInvoiceStatus(id, (byte) 7, description, (byte) 6);
        return "redirect:/admin/order/view-invoice/" + id;

    }
    @PostMapping("/completed")
    public String completedInvoice(@RequestParam("idINV") int id,@RequestParam("description") String description) {
        updateInvoiceStatus(id, (byte) 8, description, (byte) 7);
        return "redirect:/admin/order/view-invoice/" + id;

    }
    @PostMapping("/cancel-invoice")
    public String canceledInvoice(@RequestParam("idINV") int id,@RequestParam("cancelDescription") String description) {
        Invoice invoice = invoiceRepo.findById(id).orElseThrow(() -> new RuntimeException("Invoice not found"));
        if(invoice.getInvoiceStatus() == (byte) 0 || invoice.getStatus() == (byte) 4) {
            List<InvoiceDetail> invoiceDetails = invoiceDetailRepo.findByInvoiceId(id);
            if (!invoiceDetails.isEmpty()) {
                for (InvoiceDetail invoiceDetail : invoiceDetails) {
                    ProductDetail productDetail = invoiceDetail.getProductDetail();
                    productDetail.setStock(productDetail.getStock() + invoiceDetail.getQuantity());
                    productDetailRepo.save(productDetail);
                }
            }
        }
        if (invoice.getInvoiceStatus() == (byte) 1) {
            InvoiceStatusHistory oldHistory = invoiceHistoryStatusRepo.findByInvoiceIdAndNewStatus(id,invoice.getStatus());
            InvoiceStatusHistory newHistory = new InvoiceStatusHistory();
            newHistory.setInvoice(invoice);
            newHistory.setPreviousStatus(oldHistory.getNewStatus());
            newHistory.setNewStatus((byte) 1);
            newHistory.setUpdatedAt(LocalDateTime.now());
            newHistory.setNote(description);
            invoiceHistoryStatusRepo.save(newHistory);
        }
        invoice.setStatus((byte) 1);
        invoiceRepo.save(invoice);

        return "redirect:/admin/order/view-invoice/" + id;
    }
    private void updateInvoiceStatus(int id, byte newStatus, String description, byte previousStatus) {
        Invoice invoice = invoiceRepo.findById(id).orElseThrow(() -> new RuntimeException("Invoice not found"));
        invoice.setStatus(newStatus);
        invoiceRepo.save(invoice);

        InvoiceStatusHistory oldHistory = invoiceHistoryStatusRepo.findByInvoiceIdAndNewStatus(id, previousStatus);
        InvoiceStatusHistory newHistory = new InvoiceStatusHistory();
        newHistory.setInvoice(invoice);
        newHistory.setPreviousStatus(oldHistory.getNewStatus());
        newHistory.setNewStatus(newStatus);
        newHistory.setUpdatedAt(LocalDateTime.now());
        newHistory.setNote(description);
        invoiceHistoryStatusRepo.save(newHistory);
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

}
