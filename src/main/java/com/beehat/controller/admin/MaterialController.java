package com.beehat.controller.admin;

import com.beehat.entity.Material;
import com.beehat.repository.MaterialRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/material")
public class MaterialController {
    @Autowired
    MaterialRepo materialRepo;
    @GetMapping("/index")
    public String index(Model model) {
        model.addAttribute("material", materialRepo.findAll());
        model.addAttribute("m",new Material());
        return "admin/material";
    }
    @PostMapping("/add")
    public String add(@ModelAttribute("m") Material material) {
        materialRepo.save(material);
        return "redirect:/admin/material/index";
    }
    @GetMapping("/update/{id}")
    public String update(@PathVariable int id, Model model) {
        model.addAttribute("material", materialRepo.findById(id).orElse(null));
        return "admin/form-material";
    }
    @PostMapping("/update/{id}")
    public String update(@PathVariable int id, @ModelAttribute("m") Material material) {
        materialRepo.save(material);
        return "redirect:/admin/material/index";
    }
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable int id) {
        materialRepo.deleteById(id);
        return "redirect:/admin/material/index";
    }
}
