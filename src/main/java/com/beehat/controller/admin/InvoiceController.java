package com.beehat.controller.admin;

import com.beehat.entity.*;
import com.beehat.repository.*;
import com.beehat.service.AddressService;
import com.beehat.service.InvoiceService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
    @Autowired
    VoucherRepo voucherRepo;
    @Autowired
    AddressService addressService;
    @ModelAttribute("listInvoice")
    Page<Invoice> listInvoice(@RequestParam(defaultValue = "0") int page,
                              @RequestParam(required = false) String searchTerm,
                              @RequestParam(required = false) Byte invoiceType,
                              @RequestParam(required = false) LocalDateTime startDate,
                              @RequestParam(required = false) LocalDateTime endDate,
                              Model model) {
        Pageable pageable = PageRequest.of(page, 10, Sort.by("createdDate").descending());
        Page<Invoice> invoicePage = invoiceRepo.searchInvoices(searchTerm,invoiceType,startDate,endDate,pageable);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", invoicePage.getTotalPages());
        model.addAttribute("totalItems", invoicePage.getTotalElements());
        return invoicePage;
    }
    @ModelAttribute("listInvoiceProcessing")
    List<Invoice> listInvoiceProcessing() {return invoiceRepo.findByStatusAndInvoiceStatus((byte) 0,(byte) 0);}
    @ModelAttribute("listInvoiceCanceled")
    List<Invoice> listInvoiceCanceled() {return invoiceRepo.findByStatus((byte) 1);}
    @ModelAttribute("listInvoiceCompleted")
    List<Invoice> listInvoiceCompleted() {return invoiceRepo.findByStatusAndInvoiceStatus((byte) 2,(byte) 0);}
    @ModelAttribute("listInvoicePreConfirm")
    List<Invoice> listInvoicePreConfirm() {return invoiceRepo.findByStatusAndInvoiceStatus((byte) 3,(byte) 1);}
    @ModelAttribute("listInvoiceConfirm")
    List<Invoice> listInvoiceConfirm() {return invoiceRepo.findByStatusAndInvoiceStatus((byte) 4,(byte) 1);}
    @ModelAttribute("listInvoicePickUp")
    List<Invoice> listInvoicePickUp() {return invoiceRepo.findByStatusAndInvoiceStatus((byte) 5,(byte) 1);}
    @ModelAttribute("listInvoiceShipping")
    List<Invoice> listInvoiceShipping() {return invoiceRepo.findByStatusAndInvoiceStatus((byte) 6,(byte) 1);}
    @ModelAttribute("listInvoiceDelivery")
    List<Invoice> listInvoiceDelivery() {return invoiceRepo.findByStatusAndInvoiceStatus((byte) 7,(byte) 1);}
    @ModelAttribute("listInvoiceOk")
    List<Invoice> listInvoiceOk() {return invoiceRepo.findByStatusAndInvoiceStatus((byte) 8,(byte) 1);}
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
        String addressC = addressService.getWardNameByCode(invoice.getShippingWard())+ ' '+ addressService.getDistrictNameByCode(invoice.getShippingDistrict())+' '+ addressService.getProvinceNameByCode(invoice.getShippingCity());
        model.addAttribute("addressC", addressC);
        model.addAttribute("invoiceDetail", invoiceDetail);
        return "admin/invoice/view-invoice";
    }
    @Transactional
    @PostMapping("/confirm-invoice")
    public String confirmInvoice(@RequestParam("idINV") int id, @RequestParam("description") String description, RedirectAttributes redirectAttributes) {

        List<InvoiceDetail> invoiceDetailList = invoiceDetailRepo.findByInvoiceId(id);

        for (InvoiceDetail invoiceDetail : invoiceDetailList) {
            ProductDetail productDetail = invoiceDetail.getProductDetail();

            // Kiểm tra số lượng sản phẩm trong kho
            if (productDetail.getStock() < invoiceDetail.getQuantity()) {
                redirectAttributes.addFlashAttribute("error", "Sản phẩm " + productDetail.getProduct().getName() + " không đủ số lượng trong kho.");
                return "redirect:/admin/order/view-invoice/" + id;
            }
            updateInvoiceStatus(id, (byte) 4, description, (byte) 3);
            // Trừ số lượng sản phẩm trong kho
            productDetail.setStock(productDetail.getStock() - invoiceDetail.getQuantity());
            productDetailRepo.save(productDetail);
        }

        redirectAttributes.addFlashAttribute("success", "Đơn hàng đã được xác nhận thành công.");
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
        // nếu như là hoá đơn tại quầy hoặc là hoá đơn chờ lấy hàng
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
        // Nếu hoá đơn có voucher đã áp dụng, hoàn lại số lượng voucher
        if (invoice.getVoucher() != null) {
            Voucher appliedVoucher = invoice.getVoucher();
            appliedVoucher.setQuantity(appliedVoucher.getQuantity() + 1); // Hoàn lại số lượng voucher
            voucherRepo.save(appliedVoucher);
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
