package com.beehat.controller.admin;

import com.beehat.entity.Invoice;
import com.beehat.entity.Voucher;
import com.beehat.repository.InvoiceRepo;
import com.beehat.repository.VoucherRepo;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/voucher")
public class VoucherController {
    @Autowired
    private VoucherRepo voucherRepo;
    @Autowired
    private InvoiceRepo invoiceRepo;
    @ModelAttribute("listVoucher")
    public List<Voucher> listVoucher() { return voucherRepo.findAll(); }
    @ModelAttribute("iconTitle")
    String iconTitle() {
        return "ph ph-tag fs-3";
    }
    @ModelAttribute("pageTitle")
    String pageTitle() {
        return "Phiếu giảm giá";
    }
    @GetMapping("/index")
    public String index(Model model) {
        return "admin/voucher/voucher";
    }
    @GetMapping("/add-voucher")
    public String addForm(Model model) {
        model.addAttribute("voucher", new Voucher());
        return "admin/voucher/form-voucher";
    }
    @PostMapping("/add-voucher")
    public String add(@Valid @ModelAttribute("voucher") Voucher voucher, BindingResult rs,
                      Model model) {
        if (rs.hasErrors()) {
            model.addAttribute("voucher", voucher);
            return "admin/voucher/form-voucher";
        }

        // Kiểm tra mã code đã tồn tại
        if (voucherRepo.existsByCode(voucher.getCode())) {
            rs.rejectValue("code", "error.voucher", "Mã voucher đã tồn tại!"); // Thêm lỗi vào code
            return "admin/voucher/form-voucher";
        }

        // Lưu voucher vào cơ sở dữ liệu
        voucherRepo.save(voucher);
        return "redirect:/admin/voucher/index";
    }
    @GetMapping("/update-voucher/{id}")
    public String updateForm(@PathVariable int id, Model model) {
        Voucher voucher = voucherRepo.findById(id).orElse(null);
        model.addAttribute("voucher", voucher);
        return "admin/voucher/form-voucher";
    }
    @PostMapping("/update-voucher")
    public String updateVoucher(@Valid @ModelAttribute("voucher") Voucher voucher, BindingResult rs,Model model) {
        if (rs.hasErrors()) {
            model.addAttribute("voucher", voucher);
            return "admin/voucher/form-voucher";
        }
        // Kiểm tra mã code đã tồn tại
        if (voucherRepo.existsByCodeAndIdNot(voucher.getCode(), voucher.getId())) {
            rs.rejectValue("code", "error.voucher", "Mã voucher đã tồn tại!"); // Thêm lỗi vào code
            return "admin/voucher/form-voucher";
        }

        voucherRepo.save(voucher);
        return "redirect:/admin/voucher/index";
    }
    @GetMapping("/detail-voucher/{id}")
    public String detailForm(@PathVariable int id, Model model) {
        Voucher voucher = voucherRepo.findById(id).orElse(null);
        List<Invoice> invoice = invoiceRepo.findInvoiceByVoucher(id);
        model.addAttribute("voucher", voucher);
        model.addAttribute("invoice", invoice);
        return "admin/voucher/voucher-detail";
    }
}
