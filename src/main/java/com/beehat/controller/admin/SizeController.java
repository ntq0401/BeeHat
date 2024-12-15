package com.beehat.controller.admin;

import com.beehat.entity.Size;
import com.beehat.repository.SizeRepo;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/size")
public class SizeController {
    @Autowired
    SizeRepo sizeRepo;
    @ModelAttribute("iconTitle")
    String iconTitle() {
        return "ph ph-arrows-out-simple fs-3";
    }
    @ModelAttribute("pageTitle")
    String pageTitle() {
        return "Kích cỡ";
    }

    @ModelAttribute("listSize")
    List<Size> listSize(@RequestParam(required = false) String name) {
        if (name == null || name.isBlank()) {
            // Nếu không có tham số tìm kiếm, trả về toàn bộ danh sách
            return sizeRepo.findAll();
        }
        // Tìm kiếm theo tên
        return sizeRepo.findByNameContainingIgnoreCase(name);
    }

    @GetMapping("/index")
    public String index(Model model) {
        model.addAttribute("s", new Size());
        return "admin/size/size";
    }

    @PostMapping("/index")
    public String add(@Valid @ModelAttribute("s") Size size, BindingResult rs) {
        if (rs.hasErrors()) {
            return "admin/size/size";
        }
        sizeRepo.save(size);
        return "redirect:/admin/size/index";
    }

    @GetMapping("/update/{id}")
    public String update(@PathVariable("id") int id, Model model) {
        model.addAttribute("s", sizeRepo.findById(id).orElse(null));
        return "admin/size/form-size";
    }

    @PostMapping("/update/{id}")
    public String updateS(@Valid @ModelAttribute("s") Size size, BindingResult rs) {
        if (rs.hasErrors()) {
            return "admin/size/form-size";
        }
        sizeRepo.save(size);
        return "redirect:/admin/size/index";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable("id") int id) {
        Size size = sizeRepo.findById(id).orElse(null);
        size.setStatus((byte) 0);
        sizeRepo.save(size);
        return "redirect:/admin/size/index";
    }
    @GetMapping("/on/{id}")
    public String turnOn(@PathVariable("id") int id) {
        Size size = sizeRepo.findById(id).orElse(null);
        size.setStatus((byte) 1);
        sizeRepo.save(size);
        return "redirect:/admin/size/index";
    }
}
