package com.beehat.controller.employee;

import com.beehat.entity.Invoice;
import com.beehat.entity.InvoiceDetail;
import com.beehat.entity.InvoiceStatusHistory;
import com.beehat.entity.ProductDetail;
import com.beehat.repository.InvoiceDetailRepo;
import com.beehat.repository.InvoiceHistoryStatusRepo;
import com.beehat.repository.InvoiceRepo;
import com.beehat.repository.ProductDetailRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/employee/order")
public class EmployeeOrderController {
    @Autowired
    InvoiceRepo invoiceRepo;
    @Autowired
    InvoiceDetailRepo invoiceDetailRepo;
    @Autowired
    InvoiceHistoryStatusRepo invoiceHistoryStatusRepo;
    @Autowired
    ProductDetailRepo productDetailRepo;
    @ModelAttribute("listInvoice")
    List<Invoice> listInvoice() {
        Sort sort = Sort.by(Sort.Direction.DESC, "updatedDate");
        return invoiceRepo.findByInvoiceStatus((byte) 1,sort);
    }
    @ModelAttribute("listInvoiceProcessing")
    List<Invoice> listInvoiceProcessing() {return invoiceRepo.findByStatusAndInvoiceStatus((byte) 0,(byte) 1);}
    @ModelAttribute("listInvoiceCanceled")
    List<Invoice> listInvoiceCanceled() {return invoiceRepo.findByStatusAndInvoiceStatus((byte) 1,(byte) 1);}
    @ModelAttribute("listInvoiceCompleted")
    List<Invoice> listInvoiceCompleted() {return invoiceRepo.findByStatusAndInvoiceStatus((byte) 2,(byte) 1);}
    @ModelAttribute("iconTitle")
    String iconTitle() {
        return "ph ph-file-text fs-3";
    }
    @ModelAttribute("pageTitle")
    String pageTitle() {
        return "Xác nhận đơn hàng";
    }
    @GetMapping("/index")
    public String order() {
        return "employee/invoice/invoice";
    }
    @GetMapping("/view-invoice/{id}")
    public String viewInvoice(@PathVariable int id, Model model) {
        Invoice invoice = invoiceRepo.findById(id).get();
        List<InvoiceDetail> invoiceDetail = invoiceDetailRepo.findByInvoiceId(id);
        model.addAttribute("invoice", invoice);
        model.addAttribute("invoiceDetail", invoiceDetail);
        return "employee/invoice/view-invoice";
    }
    @GetMapping("/confirm-invoice/{id}")
    public String confirmInvoice(@PathVariable int id) {
        Invoice invoice = invoiceRepo.findById(id).get();
        invoice.setStatus((byte) 4);
        invoiceRepo.save(invoice);
        InvoiceStatusHistory oldHistory = invoiceHistoryStatusRepo.findByInvoiceIdAndNewStatus(id,(byte) 3);
        InvoiceStatusHistory newHistory = new InvoiceStatusHistory();
        newHistory.setInvoice(invoice);
        newHistory.setPreviousStatus(oldHistory.getNewStatus());
        newHistory.setNewStatus((byte) 4);
        newHistory.setUpdatedAt(LocalDateTime.now());
        invoiceHistoryStatusRepo.save(newHistory);
        return "redirect:/employee/order/view-invoice/" + id;
    }
    @GetMapping("/pick-up/{id}")
    public String pickUpInvoice(@PathVariable int id, Model model) {
        Invoice invoice = invoiceRepo.findById(id).get();
        invoice.setStatus((byte) 5);
        invoiceRepo.save(invoice);
        InvoiceStatusHistory oldHistory = invoiceHistoryStatusRepo.findByInvoiceIdAndNewStatus(id,(byte) 4);
        InvoiceStatusHistory newHistory = new InvoiceStatusHistory();
        newHistory.setInvoice(invoice);
        newHistory.setPreviousStatus(oldHistory.getNewStatus());
        newHistory.setNewStatus((byte) 5);
        newHistory.setUpdatedAt(LocalDateTime.now());
        invoiceHistoryStatusRepo.save(newHistory);
        return "redirect:/employee/order/view-invoice/" + id;

    }
    @GetMapping("/shipping/{id}")
    public String shippingInvoice(@PathVariable int id, Model model) {
        Invoice invoice = invoiceRepo.findById(id).get();
        invoice.setStatus((byte) 6);
        invoiceRepo.save(invoice);
        InvoiceStatusHistory oldHistory = invoiceHistoryStatusRepo.findByInvoiceIdAndNewStatus(id,(byte) 5);
        InvoiceStatusHistory newHistory = new InvoiceStatusHistory();
        newHistory.setInvoice(invoice);
        newHistory.setPreviousStatus(oldHistory.getNewStatus());
        newHistory.setNewStatus((byte) 6);
        newHistory.setUpdatedAt(LocalDateTime.now());
        invoiceHistoryStatusRepo.save(newHistory);
        return "redirect:/employee/order/view-invoice/" + id;

    }
    @GetMapping("/delivery/{id}")
    public String deliveryInvoice(@PathVariable int id, Model model) {
        Invoice invoice = invoiceRepo.findById(id).get();
        invoice.setStatus((byte) 7);
        invoiceRepo.save(invoice);
        InvoiceStatusHistory oldHistory = invoiceHistoryStatusRepo.findByInvoiceIdAndNewStatus(id,(byte) 6);
        InvoiceStatusHistory newHistory = new InvoiceStatusHistory();
        newHistory.setInvoice(invoice);
        newHistory.setPreviousStatus(oldHistory.getNewStatus());
        newHistory.setNewStatus((byte) 7);
        newHistory.setUpdatedAt(LocalDateTime.now());
        invoiceHistoryStatusRepo.save(newHistory);
        return "redirect:/employee/order/view-invoice/" + id;

    }
    @GetMapping("/completed/{id}")
    public String completedInvoice(@PathVariable int id, Model model) {
        Invoice invoice = invoiceRepo.findById(id).get();
        invoice.setStatus((byte) 8);
        invoiceRepo.save(invoice);
        InvoiceStatusHistory oldHistory = invoiceHistoryStatusRepo.findByInvoiceIdAndNewStatus(id,(byte) 7);
        InvoiceStatusHistory newHistory = new InvoiceStatusHistory();
        newHistory.setInvoice(invoice);
        newHistory.setPreviousStatus(oldHistory.getNewStatus());
        newHistory.setNewStatus((byte) 8);
        newHistory.setUpdatedAt(LocalDateTime.now());
        invoiceHistoryStatusRepo.save(newHistory);
        return "redirect:/employee/order/view-invoice/" + id;

    }
    @GetMapping("/canceled-invoice/{id}")
    public String canceledInvoice(@PathVariable int id) {
        Invoice invoice = invoiceRepo.findById(id).get();
        invoice.setStatus((byte) 1);
        invoiceRepo.save(invoice);
        List<InvoiceDetail> invoiceDetails = invoiceDetailRepo.findByInvoiceId(id);
        for (InvoiceDetail invoiceDetail : invoiceDetails) {
            ProductDetail productDetail = invoiceDetail.getProductDetail();
            productDetail.setStock(productDetail.getStock() + invoiceDetail.getQuantity());
            productDetailRepo.save(productDetail);
        }
        return "redirect:/employee/order/view-invoice/" + id;
    }

}
