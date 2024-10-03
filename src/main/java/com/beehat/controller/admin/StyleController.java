package com.beehat.controller.admin;

import com.beehat.entity.Style;
import com.beehat.repository.StyleRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/style")
public class StyleController {
    @Autowired
    StyleRepo styleRepo;
    @GetMapping("/index")
    public String index(Model model) {
        model.addAttribute("style", styleRepo.findAll());
        model.addAttribute("s",new Style());
        return "admin/style";
    }
    @PostMapping("/add")
    public String add(@ModelAttribute Style style) {
        styleRepo.save(style);
        return "redirect:/admin/style/index";
    }
    @GetMapping("/update/{id}")
    public String update(@PathVariable int id, Model model) {
        model.addAttribute("style", styleRepo.findById(id).orElse(null));
        return "admin/form-style";
    }
    @PostMapping("/update/{id}")
    public String update(@ModelAttribute Style style){
        styleRepo.save(style);
        return "redirect:/admin/style/index";
    }
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable int id) {
        styleRepo.deleteById(id);
        return "redirect:/admin/style/index";
    }
}
