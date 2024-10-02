package com.beehat.controller.admin;

import com.beehat.entity.Color;
import com.beehat.repository.ColorRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/color")
public class ColorController {
    @Autowired
    ColorRepo colorRepo;
    @GetMapping("/index")
    public String index(Model model) {
        model.addAttribute("color", colorRepo.findAll());
        model.addAttribute("c", new Color());
        return "admin/color";
    }
    @PostMapping("/add")
    public String add(@ModelAttribute Color color) {
        colorRepo.save(color);
        return "redirect:/admin/color/index";
    }
    @GetMapping("/update/{id}")
    public String update(@PathVariable int id, Model model) {
        model.addAttribute("color", colorRepo.findById(id).orElse(null));
        return "admin/form-color";
    }
    @PostMapping("/update/{id}")
    public String update(@PathVariable int id, @ModelAttribute Color color) {
        colorRepo.save(color);
        return "redirect:/admin/color/index";
    }
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable int id) {
        colorRepo.deleteById(id);
        return "redirect:/admin/color/index";
    }
}
