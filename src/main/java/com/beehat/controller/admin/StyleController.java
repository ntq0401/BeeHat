package com.beehat.controller.admin;

import com.beehat.entity.Style;
import com.beehat.repository.StyleRepo;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/style")
public class StyleController {
    @ModelAttribute("iconTitle")
    String iconTitle() {
        return "ph ph-smiley-wink fs-3";
    }
    @ModelAttribute("pageTitle")
    String pageTitle() {
        return "Kiểu dáng";
    }
    @Autowired
    StyleRepo styleRepo;

    @ModelAttribute("listStyle")
    List<Style> listStyle(@RequestParam(required = false) String name) {
        if (name == null || name.isBlank()) {
            // Nếu không có tham số tìm kiếm, trả về toàn bộ danh sách
            return styleRepo.findAll();
        }
        // Tìm kiếm theo tên
        return styleRepo.findByNameContainingIgnoreCase(name);
    }

    @GetMapping("/index")
    public String index(Model model) {
        model.addAttribute("s", new Style());
        return "admin/style/index";
    }

    @PostMapping("/index")
    public String add(@Valid @ModelAttribute("s") Style style, BindingResult result) {
        if (result.hasErrors()) {
            return "admin/style/index";
        }
        styleRepo.save(style);
        return "redirect:/admin/style/index";
    }

    @GetMapping("/update/{id}")
    public String update(@PathVariable int id, Model model) {
        model.addAttribute("s", styleRepo.findById(id).orElse(null));
        return "admin/style/form-style";
    }

    @PostMapping("/update/{id}")
    public String update(@Valid @ModelAttribute("s") Style style, BindingResult result) {
        if (result.hasErrors()) {
            return "admin/style/form-style";
        }
        styleRepo.save(style);
        return "redirect:/admin/style/index";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable int id) {
        Style style = styleRepo.findById(id).orElse(null);
        style.setStatus((byte) 0);
        styleRepo.save(style);
        return "redirect:/admin/style/index";
    }
    @GetMapping("/on/{id}")
    public String turnOn(@PathVariable int id) {
        Style style = styleRepo.findById(id).orElse(null);
        style.setStatus((byte) 1);
        styleRepo.save(style);
        return "redirect:/admin/style/index";
    }
}
