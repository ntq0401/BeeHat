package com.beehat.controller.admin;

import com.beehat.entity.Lining;
import com.beehat.repository.LiningRepo;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/lining")
public class LiningController {
    @Autowired
    LiningRepo liningRepo;

    @ModelAttribute("iconTitle")
    String iconTitle() {
        return "ph ph-parallelogram fs-3";
    }
    @ModelAttribute("pageTitle")
    String pageTitle() {
        return "Vải lót";
    }
    @ModelAttribute("listLining")
    List<Lining> linings(@RequestParam(required = false) String name) {
        if (name == null || name.isBlank()) {
            // Nếu không có tham số tìm kiếm, trả về toàn bộ danh sách
            return liningRepo.findAll();
        }
        // Tìm kiếm theo tên
        return liningRepo.findByNameContainingIgnoreCase(name);
    }

    @GetMapping("/index")
    public String index(Model model) {
        model.addAttribute("l", new Lining());
        return "admin/lining/lining";
    }

    @PostMapping("/index")
    public String add(@Valid @ModelAttribute("l") Lining lining, BindingResult rs) {
        if (rs.hasErrors()) {
            return "admin/lining/lining";
        }
        liningRepo.save(lining);
        return "redirect:/admin/lining/index";
    }

    @GetMapping("/update/{id}")
    public String update(@PathVariable("id") int id, Model model) {
        model.addAttribute("l", liningRepo.findById(id).orElse(null));
        return "admin/lining/form-lining";
    }

    @PostMapping("/update/{id}")
    public String update(@Valid @ModelAttribute("l") Lining lining, BindingResult rs) {
        if (rs.hasErrors()) {
            return "admin/lining/form-lining";
        }
        liningRepo.save(lining);
        return "redirect:/admin/lining/index";

    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable("id") int id) {
        Lining lining = liningRepo.findById(id).orElse(null);
        lining.setStatus((byte) 0);
        liningRepo.save(lining);
        return "redirect:/admin/lining/index";
    }
    @GetMapping("/on/{id}")
    public String turnOn(@PathVariable("id") int id) {
        Lining lining = liningRepo.findById(id).orElse(null);
        lining.setStatus((byte) 1);
        liningRepo.save(lining);
        return "redirect:/admin/lining/index";
    }
}
