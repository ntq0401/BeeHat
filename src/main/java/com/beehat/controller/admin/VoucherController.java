package com.beehat.controller.admin;

import com.beehat.entity.Voucher;
import com.beehat.repository.VoucherRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/voucher")
public class VoucherController {
    @Autowired
    private VoucherRepo voucherRepo;
    @ModelAttribute("listVoucher")
    public List<Voucher> listVoucher() { return voucherRepo.findAll(); }
    @GetMapping("/index")
    public String index(Model model) {
        return "admin/voucher";
    }
    @GetMapping("/add-voucher")
    public String addForm(Model model) {
        model.addAttribute("voucher", new Voucher());
        return "admin/form-voucher";
    }
    @PostMapping("/add-voucher")
    public String add(@ModelAttribute("voucher") Voucher voucher) {
        voucherRepo.save(voucher);
        return "redirect:/admin/voucher/index";
    }

}
